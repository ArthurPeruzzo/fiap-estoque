#!/bin/sh
# Cria o vhost e os usuarios de menor privilegio via API de gerenciamento. Idempotente (PUT).
# Variaveis (vindas do Secret rabbitmq-secret): RABBITMQ_DEFAULT_USER/PASS (admin),
#   RABBITMQ_MECANICA_USERNAME/PASSWORD (fiap-mecanica: orquestrador da saga), RABBITMQ_ESTOQUE_USERNAME/PASSWORD
#   (fiap-estoque: dono dos recursos estoque.*), RABBITMQ_COBRANCA_USERNAME/PASSWORD (fiap-cobranca: dono dos recursos
#   cobranca.*; opcional, so e criado se informado), RABBITMQ_HOST e RABBITMQ_VHOST.
set -eu

API="http://${RABBITMQ_HOST}:15672/api"
AUTH="${RABBITMQ_DEFAULT_USER}:${RABBITMQ_DEFAULT_PASS}"

put() { # caminho corpo
  curl -sS --fail --retry 30 --retry-delay 2 --retry-connrefused -u "$AUTH" \
    -H 'content-type: application/json' -X PUT "$API/$1" -d "$2" > /dev/null
}

put "vhosts/$RABBITMQ_VHOST" '{}'

# fiap-mecanica (orquestrador da saga): dono da fila de respostas, da DLQ e dos recursos "ordemservico.*"; publica nos
# exchanges de comandos do estoque e da cobranca (nao le nenhuma fila deles).
put "users/$RABBITMQ_MECANICA_USERNAME" "{\"password\":\"$RABBITMQ_MECANICA_PASSWORD\",\"tags\":\"\"}"
put "permissions/$RABBITMQ_VHOST/$RABBITMQ_MECANICA_USERNAME" \
  '{"configure":"^(estoque\\.comandos|cobranca\\.comandos|ordemservico\\..*)$","write":"^(estoque\\.comandos|cobranca\\.comandos|ordemservico\\..*)$","read":"^ordemservico\\..*"}'

# fiap-estoque: dono da fila de comandos, da DLQ e dos exchanges "estoque.*"; responde no exchange da OS.
put "users/$RABBITMQ_ESTOQUE_USERNAME" "{\"password\":\"$RABBITMQ_ESTOQUE_PASSWORD\",\"tags\":\"\"}"
put "permissions/$RABBITMQ_VHOST/$RABBITMQ_ESTOQUE_USERNAME" \
  '{"configure":"^(estoque\\..*|ordemservico\\.eventos)$","write":"^(estoque\\..*|ordemservico\\.eventos)$","read":"^estoque\\..*"}'

# fiap-cobranca: dono da fila de comandos, da DLQ e dos exchanges "cobranca.*"; responde no exchange da OS.
if [ -n "${RABBITMQ_COBRANCA_USERNAME:-}" ]; then
  put "users/$RABBITMQ_COBRANCA_USERNAME" "{\"password\":\"$RABBITMQ_COBRANCA_PASSWORD\",\"tags\":\"\"}"
  put "permissions/$RABBITMQ_VHOST/$RABBITMQ_COBRANCA_USERNAME" \
    '{"configure":"^(cobranca\\..*|ordemservico\\.eventos)$","write":"^(cobranca\\..*|ordemservico\\.eventos)$","read":"^cobranca\\..*"}'
fi

echo "RabbitMQ: vhost $RABBITMQ_VHOST e usuarios configurados"
