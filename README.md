# sapbw-enrich-booking microservice

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
java -jar fat-sapbw-enrich-booking-*.jar
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

- `SAPBW_URL` - SAP-BW url. Defaults to `https://testwebservices.hec.thomascook.com/sap/opu/odata/SAP/`.
- `SAPBW_USERNAME` - SAP-BW user name. Defaults to `TCIP1`.
- `SAPBW_PASSWORD` - SAP-BW password. Defaults to `Init123!`.

You can adjust other options in `application.conf`.

## Message formats

Example of a message that is published to the bus:

```json
{
  "id": "c9d20236-733e-4ea9-99f5-53aaddc6c84a",
  "correlationId": "d616c966-9cc0-460e-af32-a2bb244cf5dd",
  "tags": [],
  "topics": {
    "to": "booking:enrich:event",
    "response": "booking:enrich:event:response:52d4b186-dd71-4b5c-9ae1-21cc58f6826c",
    "routingKey": ""
  },
  "meta": {
    "createdAt": "2017-01-26T10:44:58.355Z",
    "publishedAt": "2017-01-26T10:44:58.355Z",
    "durationMs": 0,
    "serviceDetails": {
      "name": "customer-sapbw-enrich-canonical-booking-microservice",
      "version": "1.0.0",
      "instanceId": "52d4b186-dd71-4b5c-9ae1-21cc58f6826c",
      "hostname": "rdro-ws",
      "ip": "127.0.1.1",
      "pid": 21123
    }
  },
  "payload": {
    "booking": {
      "identifier": {
        "bookingNumber": "1223374",
        "bookingVersionOnTour": "2",
        "bookingVersionTourOperator": "2",
        "bookingUpdateDateOnTour": "30112016",
        "bookingUpdateDateTourOperator": "27072016"
      },
      "general": {
        "bookingStatus": "Booked",
        "bookingDate": "2016-05-01",
        "departureDate": "18122016",
        "returnDate": "02012017",
        "destination": "",
        "toCode": "33",
        "brand": "BUC",
        "brochureCode": "BUC",
        "isLateBooking": "N"
      },
      "travelParticipant": [
        {
          "firstName": "MARCEL",
          "lastName": "HIOB",
          "age": "28",
          "gender": "Male",
          "relation": "Participant",
          "travelParticipantIdOnTour": "4461293",
          "language": "GER",
          "birthDate": ""
        },
        {
          "firstName": "SABRINA",
          "lastName": "SCHAPPEL",
          "age": "24",
          "gender": "Female",
          "relation": "Participant",
          "travelParticipantIdOnTour": "4461294",
          "language": "GER",
          "birthDate": ""
        },
        {
          "firstName": "DARIO",
          "lastName": "SCHAPPEL",
          "age": "3",
          "gender": "Unknown",
          "relation": "Child",
          "travelParticipantIdOnTour": "4461295",
          "language": "GER",
          "birthDate": ""
        },
        {
          "firstName": "MADLIN",
          "lastName": "SCHAPPEL",
          "age": "5",
          "gender": "Unknown",
          "relation": "Child",
          "travelParticipantIdOnTour": "4461296",
          "language": "GER",
          "birthDate": ""
        }
      ],
      "services": {
        "accommodation": [
          {
            "accommodationCode": "40867",
            "accommodationDescription": "TRENDY VERBENA BEACH",
            "order": "489933",
            "startDate": "18122016",
            "startTime": "0000",
            "endDate": "02012017",
            "endTime": "0000",
            "roomType": "A2D",
            "boardType": "AI",
            "status": "RQ",
            "hasSharedRoom": "N",
            "numberOfParticipants": "4",
            "numberOfRooms": "1",
            "withTransfer": "G",
            "isExternalService": "I",
            "notificationRequired": "J",
            "needsTourGuideAssignment": "N",
            "isExternalTransfer": "",
            "travelParticipantAssignment": [
              {
                "travelParticipantId": "4461293"
              },
              {
                "travelParticipantId": "4461294"
              },
              {
                "travelParticipantId": "4461295"
              },
              {
                "travelParticipantId": "4461296"
              }
            ],
            "remark": [
              {
                "type": "A",
                "text": "EKMS Hotel Code: 40165"
              },
              {
                "type": "A",
                "text": "EKMS Room Code: A2B"
              },
              {
                "type": "A",
                "text": "GIATA Code: 135744"
              }
            ]
          }
        ],
        "transport": [
          {
            "transportCode": "XQ0141",
            "transportDescription": "XQ0141",
            "order": "1",
            "startDate": "18122016",
            "startTime": "1840",
            "endDate": "18122016",
            "endTime": "2305",
            "transferType": "IN",
            "departureAirport": "FRA",
            "arrivalAirport": "AYT",
            "carrierCode": "XQ",
            "flightNumber": "0141",
            "flightIdentifier": "XQ0141FRAAYT",
            "numberOfParticipants": "4",
            "travelParticipantAssignment": [
              {
                "travelParticipantId": "4461293"
              },
              {
                "travelParticipantId": "4461294"
              },
              {
                "travelParticipantId": "4461295"
              },
              {
                "travelParticipantId": "4461296"
              }
            ]
          },
          {
            "transportCode": "XQ0140",
            "transportDescription": "XQ0140",
            "order": "2",
            "startDate": "02012017",
            "startTime": "1555",
            "endDate": "02012017",
            "endTime": "1745",
            "transferType": "OUT",
            "departureAirport": "AYT",
            "arrivalAirport": "FRA",
            "carrierCode": "XQ",
            "flightNumber": "0140",
            "flightIdentifier": "XQ0140AYTFRA",
            "numberOfParticipants": "4",
            "travelParticipantAssignment": [
              {
                "travelParticipantId": "4461293"
              },
              {
                "travelParticipantId": "4461294"
              },
              {
                "travelParticipantId": "4461295"
              },
              {
                "travelParticipantId": "4461296"
              }
            ]
          }
        ]
      },
      "sourceSystem": "33",
      "bookingNumber": "",
      "businessArea": "<NO TEXT>",
      "agent": {
        "shopCode": "Tour Vital Touristik GmbH"
      },
      "hasComplaint": "",
      "travelAmount": 1,
      "numberOfParticipants": 2,
      "numberOfAdults": 2,
      "numberOfChildren": 0,
      "numberOfInfants": 0,
      "currency": "EUR",
      "duration": 9,
      "customerId": "115005070",
      "booker": {
        "mobile": "",
        "bookerEmail": "",
        "phone": "",
        "emergencyNumber": ""
      }
    }
  }
}
```
## DevOps instructions

## Product Owner
 - Product Owner - Frank Vreys
 - TDM - Simon Vos
 - Development team - [Umber Hulks Team](https://tc-jira.atlassian.net/wiki/display/UHT/Integration+Team+-+Umber+Hulks+Team)
 
