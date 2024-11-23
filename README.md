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
| APP_DATASOURCE_MONGODB_URL | [MongoDB URI](https://www.mongodb.com/docs/manual/reference/connection-string/) | Databa uri connection string                 |
| APP_ENVIRONMENT            | String                                                                          | Environment name to use with the healthcheck |
| APP_JWT_SECRET             | String                                                                          | Secret to use in the tokens checking step    |
| APP_LOGGING_LEVEL          | INFO or DEBUG                                                                   | Application logging level                    |
| APP_PORT                   | Number                                                                          | Application's port                           |

### Minimal requirements (optimal) ###
| Component         | RAM         | CPU       |
|:------------------|:------------|:----------|
| Microservice      | 256MB (1GB) | 0.5 (1.0) |
| Database          | 1GB   (2GB) | 1.0 (2.0) |

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

### Getting Started
...

### Main challenges
IMHO there were four main challenges to take into account in order to provide a suitable solution for this contest:
- **Duplication detection**: Currency conversions should be rejected within 20s in they are duplicated. Key fact: Fast responses for non valid requests.
- **Code assigning**: Each code assigned for completed transactions should be unique based on an incremental suffix. Key fact: Concurrency access to shared resources.
- **Input validation**: Ensuring the implementation is secure and compliant with -functional or not- requirements. Key fact: Security and domain definition affects all the application transversaly
- **Data consistency**: Domain should be modeled in a way it meet not only the business rules but also specific conditions . Key fact:

among other features maybe not as critical as the mentioned ones in terms of implementation but necessary at best.

### Project structure
...

### Tech stack selection
...
