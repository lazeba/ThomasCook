Feature: SapBw microservice

  Scenario Outline: SapBw microservice enriches canonical message

    Given SapBw mock responds with status 200 and body /ZBOOKING_SRV_metadata.xml for metadata
    And SapBw mock responds with status 200 and body /BOOKING('12233742016050133').xml for booking key <BOOKING_KEY> and:
      | //d:Bookingno | <BOOKING_ID> |

    When Booking message /json/in-payload-example.json has been sent for booking <BOOKING_ID> with:
      | /booking/identifier/bookingNumber | <BOOKING_ID>   |
      | /booking/general/bookingDate      | <BOOKING_DATE> |
      | /booking/general/toCode           | <TO_CODE>      |

    Then Enriched message is published with:
      | /booking/identifier/bookingNumber | <BOOKING_ID> |
      | /booking/general/customerId       | 115005070    |

    Examples:
      | BOOKING_ID | BOOKING_KEY     | TO_CODE | BOOKING_DATE |
      | 12345      | 123452016050133 | 34      | 2016-05-01   |
      | 12345      | 12345201604011  | 10      | 2016-04-01   |
      | 12345      | 12345201604011  | 29      | 2016-04-01   |
      | 12345      | 12345201604011  | 48      | 2016-04-01   |





