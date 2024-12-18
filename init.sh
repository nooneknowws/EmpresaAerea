#!/bin/bash

subir_bancos() {
  echo "Subindo containers dos bancos..."
  # docker start my-rabbitmq my-mongo dac-postgres dac-pgadmin 2>/dev/null
  docker start my-rabbitmq my-mongo my-postgres 2>/dev/null
  echo "Todos os bancos foram iniciados."
}

gerar_imagens() {
  docker compose up
}

subir_bancos
gerar_imagens

