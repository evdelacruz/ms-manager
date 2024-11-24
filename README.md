<a name="top"></a>
## Concurso Backend DFL 2024
[![JVM](https://img.shields.io/badge/Java-11%2C%2017-512BD4)](https://www.oracle.com/java/technologies/downloads/)
[![language](https://img.shields.io/badge/language-Scala-E0234D)](https://www.scala-lang.org/)
[![toolkit](https://img.shields.io/badge/toolkit-Akka-black)](https://www.akka.io/)
[![OS](https://img.shields.io/badge/OS-linux%2C%20windows%2C%20macOS-0078D4)](https://docs.abblix.com/docs/technical-requirements)
[![CPU](https://img.shields.io/badge/CPU-x86%2C%20x64%2C%20ARM%2C%20ARM64-FF8C00)](https://docs.abblix.com/docs/technical-requirements)

[![Latest Version](https://img.shields.io/badge/Development_Version-0.2.0--SNAPSHOT-blue.svg)](https://github.com/evdelacruz/ms-manager/blob/dev/build.gradle)
[![Latest Stable Version](https://img.shields.io/badge/Latest_Stable_Version-0.1.0-bluegreen.svg)](https://github.com/evdelacruz/ms-manager/blob/master/build.gradle)

⭐ Share your thoughts on social media!

[![Share](https://img.shields.io/badge/share-000000?logo=x&logoColor=white)](https://x.com/evdelacruz)
[![Share](https://img.shields.io/badge/share-1877F2?logo=facebook&logoColor=white)](https://www.facebook.com/evdelacruzcub/)
[![Share](https://img.shields.io/badge/share-0A66C2?logo=linkedin&logoColor=white)](https://www.linkedin.com/in/evdelacruz/)
[![Share](https://img.shields.io/badge/share-0088CC?logo=telegram&logoColor=white)](https://t.me/evdelacruz)

## Table of Contents
- [Deployment](#-deployment)
- [Technologies](#-technologies)
- [How to Build](#-how-to-build)
- [Documentation](#-documentation)

## 🚀 Deployment

### Environment Variables ###

| Variable                   | Format                                                                          | Description                                  |
|:---------------------------|:--------------------------------------------------------------------------------|:---------------------------------------------|
| APP_DATASOURCE_MONGODB_URL | [MongoDB URI](https://www.mongodb.com/docs/manual/reference/connection-string/) | Database uri connection string               |
| APP_ENVIRONMENT            | String                                                                          | Environment name to use with the healthcheck |
| APP_JWT_SECRET             | String                                                                          | Secret to use in the tokens checking step    |
| APP_LOGGING_LEVEL          | INFO or DEBUG                                                                   | Application logging level                    |
| APP_PORT                   | Number                                                                          | Application's port                           |

### Minimal requirements (optimal) ###
| Component         | RAM         | CPU       |
|:------------------|:------------|:----------|
| Microservice      | 256MB (1GB) | 0.5 (1.0) |
| Database          | 1GB   (2GB) | 1.0 (2.0) |

Note: Since I decided to use an in-memory cache implementation (details explained below), the more RAM the service gets, the better.

### Database scripts ###

In order to get the best performance possible some commands should be executed on the database server side

```javascript
/**
 * Ensuring that transactions types name is unique at data store level 
 */
db.transactiontypes.createIndex({ code: 1 }, { unique: true })

/**
 * Ensuring that transaction's code is unique at data store level
 */
db.transactions.createIndex({ transactionCode: 1 }, { unique: true })

/**
 * Trying to optimize queries based on 'transactionType' field 
 */
db.transactions.createIndex({ transactionType: 1 })

/**
 * Trying to optimize queries based on 'createdAt' field
 */
db.transactions.createIndex({ createdAt: 1 })

/**
 * Sets the proper settings records to ensure all the environment is well configured
 */
db.envs.insertOne({
    name: 'exchange',
    createdAt: ISODate('2024-11-20T00:00:00.000Z'),
    updatedAt: ISODate('2024-11-20T00:00:00.000Z')
})
```

## 🎓 Technologies

| Name      | Version | Description                                                                     |
|:----------|:--------|:--------------------------------------------------------------------------------|
| Akka      | 2.6.21  | Platform under which the solution relies many critical challenges               |
| Akka HTTP | 10.2.10 | HTTP server engine                                                              |
| Caffeine  | 2.9.3   | Cache provider for read and isolated operations                                 |
| Scala     | 2.13.10 | Base language selected to provide very descriptive, expressive and concise code |

## 📝 How to Build

To build the packages, follow these steps:

```shell
# Open a terminal (Command Prompt or PowerShell for Windows, Terminal for macOS or Linux)

# Ensure Docker Compose is installed
# Visit https://docs.docker.com/compose/ to download and install if not already installed

# Run the project
docker compose up -d --build
```

### Service health monitoring ###

The service tries to set an internal set up at startup. In order to check if everything is in the right direction you can check the following path **GET** `.../healthcheck/status` and expect as a response: 
```json
{
  "datasource": {
    "mongo": "OK"
  },
  "info": {
    "name": "ms-exchanger",
    "version": "0.2.0"
  },
  "services": {
    "exchange-rate": "OK"
  },
  "status": "OK"
}
```

## 📚 Documentation

### Main challenges
There were three main challenges to take into account in order to provide a suitable solution for this contest:
- **Duplication detection**: Currency conversions should be rejected within 20s in they are duplicated. Key fact: Fast responses for -temporally- duplicated requests while avoiding expensive mutual exclusion techniques in order to achieve this requirement.
- **Code assigning**: Each code assigned for completed transactions should be unique based on an incremental suffix. Key fact: Concurrency access to shared resources.
- **Input validation & Data consistency**: Domain should be modeled in a way it meets not only the business rules but also specific conditions. Key fact: Ensuring the implementation is secure and compliant with -functional or not- requirements by taking into account that domain entities and its aggregations are fundamental for core features.

among other features maybe not as critical as these mentioned ones in terms of implementation but necessary as well.

### Tech stack selection
Selecting the right technology stack is a critical step in the success of any software project. It directly impacts the system's scalability, performance, maintainability, and the development team's efficiency. For this project, the chosen stack leverages Akka, Scala, and Caffeine. Each of these technologies has been carefully selected for its unique capabilities, compatibility, and alignment with the project's requirements.

1. **Akka**: It is a powerful toolkit for building highly concurrent, distributed, and fault-tolerant systems. Its actor-based model enables developers to handle complex, stateful workflows with ease while abstracting the complexities of multithreading and asynchronous programming. Two of the contest challenges are closely related to these characteristics and I leveraged in Akka the resolution of them.
   - Duplication detection: In combination with **Caffeine** I put cache-first (with 20 seconds of TTL per entry) handler at the top of the *conversion* operation in order to let pass or reject requests by calculating the proper key (combination of amount, currencies and transaction type) for each of them. It is important to highlight that an optimistic guarding strategy was selected to achieve this requirement. This approach tries to minimize problem where many requests to a particular cache key arrive before the first one could be set. I think the opposite (special locking guarding techniques) can lead to a poor performance by having a bottle neck in this step. If repeated requests are not too frequently (and by frequent I mean a bunch of then concurrently) the selected approach fits perfectly the business rule. It only fails (by a very small number of errors) when high loads come concurrently. At the end, to switch from one approach to another is a matter of knowing what the most likely behavior is, and that information were not provided.
   - Code assigning: This operation has the particularity of calculates codes by incrementing a number that acts as a suffix. In order to solve this *concurrent-access resource* problem I implemented an actor to encapsulate state and behavior while guaranteeing that interaction occurs exclusively through asynchronous message-passing. This approach is simple, concise, less error-prone and bulletproof.
2. **Scala**: Everything in Scala is an expression so modeling data and domain-specific logic with ADTs (**A**lgebraic **D**ata **T**ypes) align well with functional programming paradigms, such as monads, functors, and folds, enabling higher-order abstractions. This provides a robust way to model data and logic with type safety, clarity, and maintainability by reducing runtime errors and making code more expressive.
3. **MongoDB**: Even when databases are details within an architecture, every selection has its own reasons. MongoDb was chosen because it comes with a robust aggregation framework that supports complex transformations directly on the database server without pulling data into application code. Meeting the stats requirement was easy peasy without having to use complex queries without losing the rest of the advantages this kind of tools offer.

### Project structure
A multilayered architecture was selected in order to organize the internal project components (functions, domain entities, etc). The structure and responsibilities is described as follows:

- **Distribution layer**: Is the presentation layer of the application. Interacts with the outside in this case by exposing REST based routers that receive external requests and provides the proper responses to the callers. The main idea behind it is to create a boundary between the application logic and external systems.
- **Orchestration layer**: A requirement can be often described as a combination of several use cases that may produce a success or failed result depending on the input and the state of the application. This is the area that coordinates and executes the business logic defined in the Domain Layer to fulfill specific use cases or workflows.
- **Domain layer**: Encapsulates the core business rules and logic of the application by having as a cornerstone the domain definition (modeling and operations).

The goal behind this definition is to have a loosely coupling structure where each component has it responsibility under the organization. This abstraction reduces the complexity of adding incoming features and prepares the architecture to minimize the impact of future evolving changes (maybe to a well-defined Hexagonal architecture) if needed.

### Improvements opportunities
There are a couple of areas (among other ones) where with simple changes I can improve the application performance and behavior.

- Cache for searches and stats operations: A cache can be added to improve the responses in transactions searches and stats retrieval operations. There are two things to take into account:
  - Name modifications over transactions types should evict the cache entries since the responses of the mentioned operations includes this field.
  - Date based filters impacts on the cache strategy, especially the `endDate` field. Only past values should be cached since future ones may exclude possible incoming conversions.
- Horizontal scaling: In case of horizontal scaling needs the cache implementation can be leveraged to an external tool like Redis and code assigning strategy extended by adding the Akka Persistence module. All of these changes without touching any key part of the source code.
