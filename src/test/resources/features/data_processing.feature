Feature: data processing

  In order to be able to provide usage statistics to business owners
  As a developer
  I need to aggregate and store the data from source files

  Background: reset the state
    Given there is a clean state

  Scenario: analysis of consecutive messages
    Given there is a message stream from file "empty.data":
      """
      {"messageType": "VisitCreate","visit": {"id": "6e066d95-7c11-4b3c-ab4f-406f82953c4e","userId": "20879fe7-a946-4ca1-b0f6-f8f10e753fba","documentId": "a8e5010b-aa0a-44c3-b8b2-6865ca0bac90","createdAt": "2015-01-01T10:24:33.000Z"}}
      {"messageType": "VisitCreate","visit": {"id": "3eaacf15-c9a3-4d39-a8c9-90f8d29b8bd1","userId": "20879fe7-a946-4ca1-b0f6-f8f10e753fba","documentId": "a8e5010b-aa0a-44c3-b8b2-6865ca0bac90","createdAt": "2015-01-01T10:34:33.000Z"}}
      {"messageType": "VisitCreate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","userId": "c9c5f9a7-cc2f-47a5-b089-06e1b7aef129","documentId": "62e09c7d-714d-40a6-9e6e-fdc525a90d59","createdAt": "2015-01-01T11:22:33.000Z"}}
      {"messageType": "VisitUpdate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","engagedTime": 900,"completion": 0.1,"updatedAt": "2015-01-01T11:23:33.000Z"}}
      {"messageType": "VisitCreate","visit": {"id": "c63bd06d-dec0-465f-bba5-a2b61f438275","userId": "20879fe7-a946-4ca1-b0f6-f8f10e753fba","documentId": "a8e5010b-aa0a-44c3-b8b2-6865ca0bac90","createdAt": "2015-01-01T11:33:44.000Z"}}
      {"messageType": "VisitUpdate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","engagedTime": 1800,"completion": 0.3,"updatedAt": "2015-01-01T11:43:44.000Z"}}
      {"messageType": "VisitUpdate","visit": {"id": "6f200a5d-628c-4311-aec9-50b8cb481bf5","engagedTime": 2700,"completion": 1.0,"updatedAt": "2015-01-01T11:53:44.000Z"}}
      {"messageType": "VisitCreate","visit": {"id": "345c28ce-d618-484b-ad07-d8b08722e58c","userId": "bad408f8-eab7-4aae-a3ea-36ad0d2f7809","documentId": "62e09c7d-714d-40a6-9e6e-fdc525a90d59","createdAt": "2015-01-01T12:33:44.000Z"}}
      {"messageType": "VisitUpdate","visit": {"id": "345c28ce-d618-484b-ad07-d8b08722e58c","engagedTime": 900,"completion": 0.4,"updatedAt": "2015-01-01T12:34:44.000Z"}}
      {"messageType": "VisitUpdate","visit": {"id": "345c28ce-d618-484b-ad07-d8b08722e58c","engagedTime": 1800,"completion": 0.4,"updatedAt": "2015-01-01T12:35:44.000Z"}}
      """
    When I run analysis of the message stream
    Then the generated analysis should match the expectation:
      # document|start time|end time|visits|uniques|time|completion
      """
      a8e5010b-aa0a-44c3-b8b2-6865ca0bac90|2015-01-01T10:00Z|2015-01-01T11:00Z|2|1|0.0|0
      a8e5010b-aa0a-44c3-b8b2-6865ca0bac90|2015-01-01T11:00Z|2015-01-01T12:00Z|1|1|0.0|0
      62e09c7d-714d-40a6-9e6e-fdc525a90d59|2015-01-01T11:00Z|2015-01-01T12:00Z|1|1|0.75|1
      62e09c7d-714d-40a6-9e6e-fdc525a90d59|2015-01-01T12:00Z|2015-01-01T13:00Z|1|1|0.5|0
      """
