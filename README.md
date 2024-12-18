# EmpresaAerea

Este repositório contém o backend de uma aplicação web para gerenciamento de operações de uma companhia aérea, implementado com uma arquitetura de microsserviços utilizando Spring Boot (Java). O padrão de projeto SAGA é empregado para a comunicação entre os microsserviços, e um API Gateway foi desenvolvido em Node.js.

## Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Instalação](#instalação)
- [Uso](#uso)
- [Contribuição](#contribuição)
- [Licença](#licença)
- [Contato](#contato)

## Visão Geral

A aplicação "EmpresaAerea" é projetada para gerenciar operações de uma companhia aérea, incluindo funcionalidades como:

- **Gerenciamento de Voos**: Criação, atualização e cancelamento de voos.
- **Reservas**: Processamento de reservas de passagens aéreas.
- **Clientes**: Gerenciamento de informações dos clientes.
- **Funcionários**: Administração dos dados dos funcionários.
- **Autenticação e Autorização**: Controle de acesso seguro para usuários e funcionários.

## Arquitetura

A aplicação adota uma arquitetura de microsserviços, onde cada serviço é responsável por um domínio específico:

- **MS Voos**: Gerencia operações relacionadas a voos.
- **MS Reserva**: Lida com reservas de passagens.
- **MS Cliente**: Gerencia informações dos clientes.
- **MS Funcionários**: Administra dados dos funcionários.
- **MS Auth**: Responsável por autenticação e autorização.

A comunicação entre os microsserviços é coordenada pelo padrão de projeto SAGA, garantindo consistência em operações distribuídas. O API Gateway, desenvolvido em Node.js, atua como ponto de entrada unificado para as requisições, roteando-as para os serviços apropriados.

## Tecnologias Utilizadas

- **Backend**:
  - Java com Spring Boot
  - Node.js
- **Padrões de Projeto**:
  - SAGA para coordenação de transações
- **Banco de Dados**:
  - PostgreSQL
- **Gerenciamento de Dependências**:
  - Maven
  - npm
- **Containerização**:
  - Docker
- **Orquestração**:
  - Docker Compose
- **Mensageria**:
  - RabbitMQ para comunicação entre microsserviços

## Instalação

1. **Pré-requisitos**:
   - [Docker](https://www.docker.com/get-started)
   - [Docker Compose](https://docs.docker.com/compose/install/)

2. **Clonar o Repositório**:
   ```bash
   git clone https://github.com/nooneknowws/EmpresaAerea.git
   cd EmpresaAerea
