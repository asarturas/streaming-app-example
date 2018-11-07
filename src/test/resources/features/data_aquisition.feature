Feature: data acquisition

  In order to analyse the data stream
  As an application developer
  I need to turn data file into a stream of messages

  Background: reset the state
    Given there is a clean state

  Scenario: acquisition of empty data file

    Given there is source file "empty.data":
      """
      """
    When I create a message stream from source file "empty.data"
    Then I should get an empty message stream

  Scenario: acquisition of file with all valid messages

    Given there is source file "visit-creation-messages.data":
      """
      {"messageType": "VisitCreate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","userId": "c9c5f9a7-cc2f-47a5-b089-06e1b7aef129","documentId": "62e09c7d-714d-40a6-9e6e-fdc525a90d59","createdAt": "2015-01-01T11:22:33.000Z"}}
      {"messageType": "VisitCreate","visit": {"id": "c63bd06d-dec0-465f-bba5-a2b61f438275","userId": "20879fe7-a946-4ca1-b0f6-f8f10e753fba","documentId": "a8e5010b-aa0a-44c3-b8b2-6865ca0bac90","createdAt": "2015-02-02T22:33:44.000Z"}}
      """
    When I create a message stream from source file "visit-creation-messages.data"
    Then I should get a stream with 2 messages
    And all the messages should of type "VisitCreate"