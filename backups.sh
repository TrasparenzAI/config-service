#!/bin/bash

# Da cambiare in funzione dell'installazione corrente
SERVICE_DIR=/home/anac/config-service

echo "ESECUZIONE delle operazioni di backup databases di config-service"

date=`date +%Y%m%d-%H%M`


BACKUP_DIR=${SERVICE_DIR}/backups

find $BACKUP_DIR -mtime +30 | xargs -r rm
cd $SERVICE_DIR

docker compose -f $SERVICE_DIR/docker-compose.yml exec -T postgres pg_dump -U cnr-anac configs > $BACKUP_DIR/config-service-$date.sql

gzip -9 $BACKUP_DIR/config-service-$date.sql
