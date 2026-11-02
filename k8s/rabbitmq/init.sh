#!/bin/sh
# Cria o vhost e os usuarios de menor privilegio via API de gerenciamento. Idempotente (PUT).
# Variaveis (vindas do Secret rabbitmq-secret): RABBITMQ_DEFAULT_USER/PASS (admin),
#   RABBITMQ_MECANICA_USERNAME/PASSWORD (fiap-mecanica: so publica), RABBITMQ_ESTOQUE_USERNAME/PASSWORD
#   (fiap-estoque: dono dos recursos estoque.*), RABBITMQ_HOST e RABBITMQ_VHOST.
set -eu

API="http://${RABBITMQ_HOST}:15672/api"
AUTH="${RABBITMQ_DEFAULT_USER}:${RABBITMQ_DEFAULT_PASS}"

put() { # caminho corpo
  curl -sS --fail --retry 30 --retry-delay 2 --retry-connrefused -u "$AUTH" \
    -H 'content-type: application/json' -X PUT "$API/$1" -d "$2" > /dev/null
}

put "vhosts/$RABBITMQ_VHOST" '{}'

# fiap-mecanica: so publica no exchange de comandos do estoque (nao le nenhuma fila).
put "users/$RABBITMQ_MECANICA_USERNAME" "{\"password\":\"$RABBITMQ_MECANICA_PASSWORD\",\"tags\":\"\"}"
put "permissions/$RABBITMQ_VHOST/$RABBITMQ_MECANICA_USERNAME" \
  '{"configure":"^estoque\\.comandos$","write":"^estoque\\.comandos$","read":"^$"}'

# fiap-estoque: dono da fila de comandos, da DLQ e dos exchanges "estoque.*".
put "users/$RABBITMQ_ESTOQUE_USERNAME" "{\"password\":\"$RABBITMQ_ESTOQUE_PASSWORD\",\"tags\":\"\"}"
put "permissions/$RABBITMQ_VHOST/$RABBITMQ_ESTOQUE_USERNAME" \
  '{"configure":"^estoque\\..*","write":"^estoque\\..*","read":"^estoque\\..*"}'

echo "RabbitMQ: vhost $RABBITMQ_VHOST e usuarios configurados"
