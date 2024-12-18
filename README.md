
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
   ```

3. **Construir e Iniciar os Serviços**:
   Execute o script de inicialização:
   ```bash
   ./init.sh
   ```
   Este script compilará os microsserviços e iniciará todos os containers Docker necessários.

## Uso

Após a instalação, a aplicação estará acessível através do **API Gateway**. Consulte a documentação específica de cada microsserviço para detalhes sobre os endpoints disponíveis e suas funcionalidades.  

### Exemplo de Requisição

Para criar um novo cliente, envie uma requisição **POST** para o endpoint `/clientes`:  

```bash
curl -X POST http://localhost:<PORTA_GATEWAY>/clientes -H "Content-Type: application/json" -d '{
    "nome": "João da Silva",
    "email": "joao.silva@email.com",
    "telefone": "(11) 91234-5678"
}'
```

Certifique-se de substituir `<PORTA_GATEWAY>` pela porta configurada no **API Gateway**.  

## Contribuição

Contribuições são bem-vindas! Siga os passos abaixo para contribuir:

1. Faça um fork deste repositório.
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`).
3. Commit suas alterações (`git commit -m 'Adiciona nova feature'`).
4. Faça push para a branch (`git push origin feature/nova-feature`).
5. Abra um Pull Request.

Antes de contribuir, verifique se suas alterações seguem o estilo e as práticas já estabelecidas no projeto.

## Licença

Este projeto está licenciado sob a **Licença MIT**. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.

## Contato

Para mais informações ou suporte, entre em contato com o mantenedor do projeto:

- **GitHub**: [nooneknowws](https://github.com/nooneknowws)
- **Email**: [thalysonbruck@hotmail.com](mailto:thalysonbruck@hotmail.com)

---

Caso tenha dúvidas ou sugestões, sinta-se à vontade para abrir uma **Issue** neste repositório.
