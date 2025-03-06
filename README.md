# Config Service del progetto TrasparenzAI
## Config Service - REST Services

[![Supported JVM Versions](https://img.shields.io/badge/JVM-21-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.java.net/install/)

Config Service è parte della suite di servizi per la verifica delle informazioni sulla
Trasparenza dei siti web delle Pubbliche amministrazioni italiane.

## Config Service

Config Service è il componente che si occupa di archiviare e distribuire alcune informazioni di configurazione dei 
servizi che compongono lo stack del progetto TrasparenzAI.

Config Service mantiene nel proprio datastore locale le configurazioni che sono fornite agli altri microservizi.
Le configurazioni possono essere inserite/aggiornate tramite gli appositi endopoint REST presenti in questo servizio.

Le configurazioni disponibili sono fornite sia sotto forma di endopoint REST con le relative CRUD, che nel formato
utilizzato da *Spring Cloud Config* attraverso il path **/config**, inserendo il nome del servizio e il profilo richiesto 
nell'url, come per esempio:

```
$ http GET :8888/config/task-scheduler/default

{
    "label": null,
    "name": "task-scheduler",
    "profiles": [
        "default"
    ],
    "propertySources": [
        {
            "name": "task-scheduler-default",
            "source": {
                "tasks.fake.cron.expression": "0 46 15 * * ?",
                "test.property1": "testme"
            }
        }
    ],
    "state": null,
    "version": null
}
```

I microservizi Spring che vogliono utilizzare questo servizio di configurazione centralizzato possono farlo
specificando nella propria configurazione tre parametri tipo:

```
spring.config.import=optional:configserver:http://@localhost:8888/config
spring.security.user.name=config-service-user
spring.security.user.password=PASSWORD_DA_IMPOSTARE_E_CONDIVIDERE_CON_I_CLIENT
```

Dove naturalmente va impostato il corretto URL a cui risponde questo servizio.

I servizi REST per l'aggiornamento della configurazioni sono documentati tramite OpenAPI consultabile
all'indirizzo /swagger-ui/index.html. 
L'OpenAPI del servizio di devel è disponibile all'indirizzo https://dica33.ba.cnr.it/config-service/swagger-ui/index.html.

### Sicurezza

L'accesso in lettura alla configurazione di tipo _Spring Cloud Config_ disponibile al path */config* 
è protetto con autenticazione di tipo *Basic Auth*, i microservizi che vogliono utilizzare questo path per ottenere la configurazione devono utilizzare l'utente e la password sepcificati tramite i parametri *spring.security.user.name* e *spring.security.user.password* , i quali possono essere specificati nel *docker-compose.yml* come nell'esempio seguente:

```
    - spring.security.user.name=config-service-user
    - spring.security.user.password=PASSWORD_DA_IMPOSTARE_E_CONDIVIDERE_CON_I_CLIENT
```

Invece gli endpoint REST di questo servizio disponibili al path _/properties_ sono protetti tramite autenticazione
OAuth con Bearer Token.
È necessario configurare l'IDP da utilizzare per validare i token OAuth tramite le due proprietà impostabili nel
*docker-compose.yml* come nell'esempio seguente:

```
    - spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dica33.ba.cnr.it/keycloak/realms/trasparenzai
    - spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://dica33.ba.cnr.it/keycloak/realms/trasparenzai/protocol/openid-connect/certs
```

Per l'accesso in HTTP GET all'API è sufficiente essere autenticati, per gli endpoint accessibili
con PUT/POST/DELETE è necessario oltre che essere autenticati che il token OAuth contenga un 
role ADMIN o SUPERUSER.

# <img src="https://www.docker.com/wp-content/uploads/2021/10/Moby-logo-sm.png" width=80> Startup

#### _Per avviare una istanza del config-service con postgres locale_

Il config-service può essere facilmente installato via docker compose su server Linux utilizzando il file 
docker-compose.yml presente in questo repository.

Accertati di aver installato docker e il plugin di docker `compose` dove vuoi installare il config-service e in seguito
esegui il comando successivo per un setup di esempio.

```
curl -fsSL https://raw.githubusercontent.com/cnr-anac/config-service/main/first-setup.sh -o first-setup.sh && sh first-setup.sh
```

Utilizzando lo script *first_setup.sh* viene generata una password per l'accesso allo Spring Cloud Config
da parte dei client, questa password può essere modificata e deve comunque essere inserita anche nei 
Spring Cloud Config client che utilizzano la configurazione remota tramite il config-service.

Collegarsi a http://localhost:8888/properties per visualizzare le proprietà predefinite presenti nel servizio, fornendo
nella richiesta http il token JWT.

Un esempio per ottenere un token OAuth:

```
TOKEN=$(curl 'https://dica33.ba.cnr.it/keycloak/realms/trasparenzai/protocol/openid-connect/token' -H 'accept: application/json, text/plain, */*' --data 'grant_type=client_credentials&client_id=result-aggregator&client_secret=PASSWORD_DA_SPECIFICARE'| jq -r '.access_token')
```

Per leggere le properties via REST utilizzando *httpie*:

```
http GET :8888/properties "Authorization: Bearer $TOKEN"
```

## Backups

Il servizio mantiene le informazioni relative alla configurazione nel db postgres, quindi è opportuno fare il backup
del database a scadenza regolare. Nel repository è presente un file di esempio [backups.sh](https://github.com/cnr-anac/config-service/blob/main/backups.sh) per effettuare i backup.

All'interno dello script backups.sh è necessario impostare il corretto path dove si trova il docker-compose.yml del progetto, tramite la variabile `SERVICE_DIR`.


## 👏 Come Contribuire 

E' possibile contribuire a questo progetto utilizzando le modalità standard della comunità opensource 
(issue + pull request) e siamo grati alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

Config Service è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova nel file [LICENSE][l].

[l]: https://github.com/cnr-anac/config-service/blob/master/LICENSE
