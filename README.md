# customer-sapbw-enrich-canonical-booking-microservice

## Overview

The microservice provides enriching of internal canonical booking structure with booking and customer info from SAP BW.

## Development Tooling

To build the project you need:
- JDK (8 or higher)
- Maven (version 3)

## Testing Approach
To perform the build just execute the following command at the project root directory:

```
mvn clean install
```

Build Docker image:
```
mvn clean install -PbuildDocker
```

Push Docker image into a registry:
```
mvn clean install -PbuildDocker -DpushImage
```

The build runs integration tests against a test database instance by default. If you want to skip this kind of testing, run:
```
mvn clean install -P DisableDbITests
```

## Running

The microservice is a standalone Java application:

```
java -jar fat-customer-sapbw-enrich-canonical-booking-microservice-*.jar
```

## Configuration

The microservice is based on `msb-java` and supports the usual set of environment variables:

- `MSB_BROKER_HOST` - host name of AMQP broker (RabbitMQ). Defaults to `127.0.0.1`.
- `MSB_BROKER_PORT`- port number of AMQP broker (RabbitMQ). Defaults to `5672`.
- `MSB_BROKER_USER_NAME` - username to use to connect to the broker.
- `MSB_BROKER_PASSWORD` - password to use to connect to the broker.
- `MSB_BROKER_VIRTUAL_HOST` - virtual host to use on the broker.
- `MSB_BROKER_USE_SSL` - whether to use SSL connection (note that cert check is not performed). Defaults to `false`.

Additionally the microservice supports specific environment variables:

TBD

You can adjust other options in `application.conf`.

## Message formats

## DevOps instructions

## Product Owner
 - Product Owner - Frank Vreys
 - TDM - Simon Vos
 - Development team - [Umber Hulks Team](https://tc-jira.atlassian.net/wiki/display/UHT/Integration+Team+-+Umber+Hulks+Team)
 
