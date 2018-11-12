[![CircleCI](https://circleci.com/gh/asarturas/streaming-app-example/tree/master.svg?style=svg)](https://circleci.com/gh/asarturas/streaming-app-example/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/asarturas/streaming-app-example/badge.svg?branch=master)](https://coveralls.io/github/asarturas/streaming-app-example?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dc332b1fac29482bb1812a04a7dbc49c)](https://app.codacy.com/app/asarturas/streaming-app-example?utm_source=github.com&utm_medium=referral&utm_content=asarturas/streaming-app-example&utm_campaign=Badge_Grade_Settings)

# Sample streaming app

### Brief description of the solution

There is a list of visit events produced in time series.
For each visit there is create event and series of update events.
Only most recent update is relevant and visit is tracked up to 1 hour.

Example events:
```
{
  "messageType": "VisitCreate",
  "visit": {
    "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
    "userId": "dc0ad841-0b89-4411-a033-d3f174e8d0ad",
    "documentId": "7b2bc74e-f529-4f5d-885b-4377c424211d",
    "createdAt": "2015-04-22T11:42:07.602Z"
  }
}
```
```
{
  "messageType": "VisitUpdate",
  "visit": {
    "id": "82abce83-3892-48ee-9f1b-d34c4746ace7",
    "engagedTime": 25,
    "completion": 0.4,
    "updatedAt": "2015-04-22T11:42:35.122Z"
  }
}
```

The goal of this application is to read a stream of these events
and functionally aggregate it into a list of statistics.

Example statistics:

|document                            | start time      |end time         |visits|uniques|time|completion|
|------------------------------------|-----------------|-----------------|------|-------|----|----------|
|62e09c7d-714d-40a6-9e6e-fdc525a90d59|2015-01-01T11:00Z|2015-01-01T12:00Z|1     |1      |0.75|1         |
|a8e5010b-aa0a-44c3-b8b2-6865ca0bac90|2015-01-01T10:00Z|2015-01-01T11:00Z|2     |1      |0.0 |0         |
|a8e5010b-aa0a-44c3-b8b2-6865ca0bac90|2015-01-01T11:00Z|2015-01-01T12:00Z|1     |1      |0.0 |0         |    
|62e09c7d-714d-40a6-9e6e-fdc525a90d59|2015-01-01T12:00Z|2015-01-01T13:00Z|1     |1      |0.5 |0         |

Note: time is in hours.

### Tests:

- `sbt cucumber` to run integration tests
- `sbt test` to run the unit tests

### Features

Current implementation takes ~90 seconds to process and aggregate ~1 million of messages on a single core,
which adds up to ~1 billion messages per day, which could be enough for many use cases.

#### What is implemented:

- Stream data from file: [features/data_acquisition.feature](src/test/resources/features/data_acquisition.feature);
- Aggregate data into analytics: [features/data_processing.feature](src/test/resources/features/data_processing.feature).

#### What is missing:

- There are unhandled edge cases and code is not at it's cleanest in buffer implementation, emphasis was on having it working for basic cases;
- Many things in main app object are hardcoded and not automatically tested at the moment;
- There could be more generic stream acquisition, not necessary from file;
- The output is just going to stdout, this should be piping into database;
- Analytics persistence and querying are missing;

#### Notable limitations and edge cases:

- If visit spans two hours (starts at 11:50, ends at 12:20), then it will be considered only towards the first hour from 11:00 to 12:00, but the stats will include the later hour from 12:00 to 13:00;
- Single incorrect message would kill the stream;
- Analytics calculation is not parallel;
- Dates are not handled cleanly - some places use ZonedDateTime, others converts it to long back and forth;
- Timeout is scattered across the code base, it could be concentrated in buffer instead;
- Only success path is considered in many places;
