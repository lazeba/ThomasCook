Feature: SapBw microservice

  Scenario Outline: SapBw microservice enriches canonical message

    Given SapBw mock responds with status 200 and body /odata-output/ZBOOKING_SRV_metadata.xml for metadata
    And SapBw mock responds with status 200 and body /odata-output/BOOKING('12233742016050133').xml for booking key <BOOKING_KEY> and:
      | //d:Bookingno | <BOOKING_ID> |
    And SapBw mock responds with status 200 and body /odata-output/sapbw-customer-metadata.xml for customer metadata
    And SapBw mock responds with status 200 and body /odata-output/sapbw-customer-content.xml for customer key <CUSTOMER_KEY> and business area 0 and:
      | //d:Customerno | <CUSTOMER_KEY> |
    When Booking message /json/in-payload-example.json has been sent for booking <BOOKING_ID> with:
      | /booking/identifier/bookingNumber | <BOOKING_ID> |

    Then Enriched message is published with:
      | /booking/identifier/bookingNumber | <BOOKING_ID> |
      | /booking/general/customerId       | 115005070    |

  Examples:
    | BOOKING_ID | BOOKING_KEY     | CUSTOMER_KEY|
    | 12345      | 123452016050133 | 070184007         |
