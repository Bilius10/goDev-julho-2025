# LogiTrack API 🚚

![Linguagem](https://img.shields.io/badge/Java-17%2B-blue?style=for-the-badge&logo=java)
![Framework](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)
![Banco de Dados](https://img.shields.io/badge/PostgreSQL-darkblue?style=for-the-badge&logo=postgresql)
![Status](https://img.shields.io/badge/status-concluído-green?style=for-the-badge)

<br>

<p align="center">
  <strong>API robusta para gerenciamento e otimização de operações logísticas, utilizando IA para decisões inteligentes.</strong>
</p>

---

### 📝 Índice

* [Sobre o Projeto](#-sobre-o-projeto)
* [Funcionalidades](#-funcionalidades)
* [Tecnologias e Arquitetura](#️-tecnologias-e-arquitetura)
* [Começando](#-começando)
* [Documentação da API](#-documentação-da-api)
* [Autores](#-autores)

---

### 🧐 Sobre o Projeto

O **LogiTrack API** é uma solução de back-end completa desenvolvida para o desafio GoDev da Senior Sistemas. O projeto simula uma plataforma de gerenciamento logístico, focada em otimizar toda a cadeia de transporte. A API utiliza **Inteligência Artificial** para analisar dados complexos e auxiliar na tomada de decisões estratégicas, como a seleção do caminhão mais eficiente para uma determinada rota e a escolha de cargas de retorno, visando maximizar a eficiência e reduzir custos operacionais.

---

### ✨ Funcionalidades

A plataforma foi projetada com as seguintes funcionalidades em mente:

* **Gestão Completa:** Controle total sobre as entidades do negócio, incluindo Filiais, Produtos, Cargas, Caminhões, Funcionários e Transportes.
* **Otimização Inteligente:** Endpoint dedicado (`/optimize-allocation`) que utiliza IA para sugerir o melhor caminhão para uma entrega, com base em segurança, manobrabilidade e consumo de combustível.
* **Segurança:** Autenticação e autorização baseadas em JWT para proteger os endpoints, com diferentes níveis de acesso (Roles).
* **Relatórios Automatizados:** Geração e envio de relatórios operacionais por e-mail, como a escala semanal de motoristas e o balanço mensal das filiais.
* **Documentação Interativa:** API totalmente documentada com Swagger (OpenAPI 3), permitindo fácil exploração e teste dos endpoints.

---

### 🛠️ Tecnologias e Arquitetura

A API foi construída com um conjunto de tecnologias modernas para garantir performance, segurança e escalabilidade.

| Tecnologia                 | Propósito                                                |
| :------------------------- | :------------------------------------------------------- |
| **Spring Boot** | Framework principal para construção da API REST.         |
| **Java 17+** | Linguagem de programação base.                           |
| **PostgreSQL** | Banco de dados relacional para persistência dos dados.   |
| **Spring Security & JWT** | Controle de autenticação e autorização.                  |
| **Spring AI (OpenAI)** | Integração com IA para otimização logística.             |
| **Flyway** | Ferramenta para versionamento e migração do banco de dados.|
| **ITEXT** | Geração de relatórios em PDF.                            |
| **Thymeleaf & Spring Mail**| Criação de templates e envio de e-mails.                 |
| **OpenRouteService** | API de terceiros para cálculo de rotas e distâncias.     |
| **JUnit 5 & Mockito** | Ferramentas para a escrita de testes.                    |
| **SpringDoc (OpenAPI 3)** | Documentação automática da API.                          |

A arquitetura do projeto segue o padrão de camadas (Controller, Service, Repository) para uma clara separação de responsabilidades.

---

### 🚀 Começando

Para executar este projeto localmente, siga os passos abaixo.

#### **Pré-requisitos**

* Java JDK 17 ou superior
* Maven 3.8+
* PostgreSQL
* Chaves de API para OpenAI e OpenRouteService

#### **Instalação**

1.  **Clone o repositório:**
    ```sh
    git clone [https://github.com/seu-usuario/logitrack-api.git]
    cd logitrack-api
    ```

2.  **Configure as variáveis de ambiente:**
    No arquivo `src/main/resources/application-dev-examples.properties`, preencha as informações de conexão com o banco de dados, chaves de API e segredos,
    apos isso renomeie o arquivo para `application-dev.properties`

    ```properties
    # Database and Flyway properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/transport_logistics
    spring.datasource.username=example-db-username
    spring.datasource.password=example-db-password
    spring.datasource.driver-class-name=org.postgresql.Driver
    
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    
    spring.flyway.enabled=true
    spring.flyway.locations=classpath:db/migration
    
    # Spring Security properties
    security.jwt.secret=example-jwt-secret
    security.jwt.issuer=example-jwt-issuer
    security.jwt.expiration-hours=example-jwt-expiration-hours
    
    # Spring Mail properties
    spring.mail.host=smtp.gmail.com
    spring.mail.port=587
    spring.mail.username=example-email-username
    spring.mail.password=example-email-password
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true
    spring.mail.properties.mail.smtp.starttls.required=true
    
    # External API Keys
    openrouteservice.api.key=example-openrouteservice-api-key
    spring.ai.openai.api-key=example-api-key
    spring.ai.openai.chat.options.model=example-model
    ```

4.  **Execute a aplicação:**
    ```sh
    mvn spring-boot:run
    ```

A API estará disponível em `http://localhost:8080`.

---

### 📖 Documentação da API

Para uma exploração interativa, acesse a UI do Swagger: **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/docs)**

A seguir, a lista detalhada dos endpoints disponíveis:

## Autenticação
| Método HTTP | Endpoint                  | Roles Permitidas |
|-------------|---------------------------|------------------|
| POST        | /api/v1/auth/create        | ADMIN            |
| POST        | /api/v1/auth/sign-in       | PÚBLICO          |
| PATCH       | /api/v1/auth/password      | TODOS            |

## Produtos
| Método HTTP | Endpoint                  | Roles Permitidas |
|-------------|---------------------------|------------------|
| GET         | /api/v1/products           | MANAGER          |
| POST        | /api/v1/products           | MANAGER          |
| PUT         | /api/v1/products/{id}      | MANAGER          |
| DELETE      | /api/v1/products/{id}      | MANAGER          |

## Cargas
| Método HTTP | Endpoint                     | Roles Permitidas |
|-------------|------------------------------|------------------|
| GET         | /api/v1/shipments             | MANAGER          |
| POST        | /api/v1/shipments             | MANAGER          |
| PUT         | /api/v1/shipments/{id}        | MANAGER          |
| DELETE      | /api/v1/shipments/{id}        | MANAGER          |

## Caminhões
| Método HTTP | Endpoint                           | Roles Permitidas |
|-------------|------------------------------------|------------------|
| GET         | /api/v1/trucks                     | DRIVER           |
| GET         | /api/v1/trucks/{code}               | DRIVER           |
| POST        | /api/v1/trucks                     | MANAGER          |
| PATCH       | /api/v1/trucks/{code}/status        | MANAGER          |

## Hubs
| Método HTTP | Endpoint                  | Roles Permitidas |
|-------------|---------------------------|------------------|
| GET         | /api/v1/hubs               | DRIVER           |
| GET         | /api/v1/hubs/{id}          | DRIVER           |
| POST        | /api/v1/hubs               | ADMIN            |
| PUT         | /api/v1/hubs/{id}          | ADMIN            |
| DELETE      | /api/v1/hubs/{id}          | ADMIN            |

## Funcionários
| Método HTTP | Endpoint                        | Roles Permitidas |
|-------------|---------------------------------|------------------|
| GET         | /api/v1/employees                | MANAGER          |
| PUT         | /api/v1/employees/{id}           | MANAGER          |
| DELETE      | /api/v1/employees/{id}           | MANAGER          |
| PATCH       | /api/v1/employees/{id}/role      | ADMIN            |

## Transportes
| Método HTTP | Endpoint                                      | Roles Permitidas |
|-------------|-----------------------------------------------|------------------|
| GET         | /api/v1/transports                            | MANAGER          |
| GET         | /api/v1/transports/hubSummary/{id}             | MANAGER          |
| POST        | /api/v1/transports/optimize-allocation         | MANAGER          |
| PATCH       | /api/v1/transports/confirm-transport/{id}      | MANAGER          |
| PATCH       | /api/v1/transports/update-status/{id}          | DRIVER           |
| POST        | /api/v1/transports/send-weekly-schedule        | MANAGER          |
| POST        | /api/v1/transports/send-month-report           | ADMIN            |
| PUT         | /api/v1/transports/{id}                        | MANAGER          |
| DELETE      | /api/v1/transports/{id}                        | ADMIN            |
---

### 👨‍💻 Autores

Este projeto foi desenvolvido pela equipe:

* **João Vitor da Rosa de Oliveira** - `joao.deoliveira@senior.com.br`
* **Martin Garrote** - `Martin.Garrote@senior.com.br`
* **Nicole Sypriany** - `Nicole.Sypriany@senior.com.br`
