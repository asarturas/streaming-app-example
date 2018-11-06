Feature: data acquisition

  In order to analyse the data stream
  As an application developer
  I need to turn data file into a stream of messages

  Scenario: acquisition of empty data file

    Given there is source file "empty.data":
    """
    """
    When I create a message stream from source file "empty.data"
    Then I should get an empty message stream
