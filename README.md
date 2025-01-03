# Config Service del progetto TrasparenzAI
## Config Service - REST Services

[![Supported JVM Versions](https://img.shields.io/badge/JVM-21-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.java.net/install/)

Config Service è parte della suite di servizi per la verifica delle informazioni sulla
Trasparenza dei siti web delle Pubbliche amministrazioni italiane.
 
## Config Service

Config Service è il componente che si occupa di archiviare e distribuire alcune informazioni di configurazione dei 
servizi che compongono lo stack del progetto TrasparenzAI.

Config Service mantiene nel proprio datastore locale le configurazioni che sono fornite agli altri microservizio.
Le configurazioni possono essere inserite/aggiornate tramite gli appositi endopoint REST presenti in questo servizio.

Le configurazioni disponibili sono fornite sia sotto forma di endopoint REST con le relative CRUD, che nel formato
utilizzato da *Spring cloud* attraverso il path **/config**, inserendo il nome del servizio e il profilo richiesto 
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
specificando nella propria configurazione un parametro tipo:

```
spring.config.import=optional:configserver:http://@localhost:8888/config
```

Dove naturalmente va impostato il corretto URL a cui risponde questo servizio.

I servizi REST per l'aggiornamento della configurazioni sono documentati tramite OpenAPI consultabile
all'indirizzo /swagger-ui/index.html. 
L'OpenAPI del servizio di devel è disponibile all'indirizzo https://dica33.ba.cnr.it/config-service/swagger-ui/index.html.

# <img src="https://www.docker.com/wp-content/uploads/2021/10/Moby-logo-sm.png" width=80> Startup

#### _Per avviare una istanza del config-service con postgres locale_

Il config-service può essere facilmente installato via docker compose su server Linux utilizzando il file 
docker-compose.yml presente in questo repository.

Accertati di aver installato docker e il plugin di docker `compose` dove vuoi installare il config-service e in seguito
esegui il comando successivo per un setup di esempio.

```
curl -fsSL https://raw.githubusercontent.com/cnr-anac/config-service/main/first-setup.sh -o first-setup.sh && sh first-setup.sh
```

Collegarsi a http://localhost:8888/properties per visualizzare le properietà predefinite presenti nel servizio. 

## Backups

Il servizio mantiene le informazioni relative alla configurazione nel db postgres, quindi è opportuno fare il backup
del database a scadenza regolare. Nel repsitory è presente un file di esempio [backups.sh](https://github.com/cnr-anac/config-service/blob/main/backups.sh) per effettuare i backup.

All'interno dello script backups.sh è necessario impostare il corretto path dove si trova il docker-compose.yml del progetto, tramite la
variabile `SERVICE_DIR`.


## 👏 Come Contribuire 

E' possibile contribuire a questo progetto utilizzando le modalità standard della comunità opensource 
(issue + pull request) e siamo grati alla comunità per ogni contribuito a correggere bug e miglioramenti.

## 📄 Licenza

Config Service è concesso in licenza GNU AFFERO GENERAL PUBLIC LICENSE, come si trova nel file [LICENSE][l].

[l]: https://github.com/cnr-anac/config-service/blob/master/LICENSE
