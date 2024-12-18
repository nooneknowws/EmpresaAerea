#!/bin/bash

## Fazer os build de cada ms
## Pre gerar os arquivos .jar que é necessário no docker compose

gerar_imagens() {
  docker compose up
}

gerar_imagens