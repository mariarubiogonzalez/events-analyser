# events-analyser

Events analyser app is analysing events and calculating the latest windowed word count by event type.
The window is a sliding window, defined by the latest event time seen minus the window size (by default is 10 seconds but it can be configured)

### Requirements
* scala 2.13
* sbt

### Run the tests
```
sbt clean test
```

### Run the app

You can parametrise the window size by sending an optional argument when running the app (see below)

```
sbt compile assembly
./blackbox | scala target/scala-2.13/events-analyser-assembly-0.1.0-SNAPSHOT.jar OPTIONAL_WINDOW_SIZE_IN_SECONDS
```

#### Query the endpoint to see the latest windowed word count grouped by event type

```
curl "http://localhost:8080/metrics"
```

Example response
```json
{
  "from": 1626642225,
  "to": 1626642244,
  "metrics": [
    {
      "eventType": "bar",
      "wordCounts": [
        {"count": 3, "word": "ipsum"},
        {"count": 1, "word": "dolor"},
        {"count": 2, "word": "lorem"},
        {"count": 1, "word": "sit"}
      ]
    },
    {
      "eventType": "foo",
      "wordCounts": [
        {"count": 1, "word": "sit"},
        {"count": 2, "word": "amet"},
        {"count": 1, "word": "lorem"}
      ]
    },
    {
      "eventType": "baz",
      "wordCounts": [
        {"count": 2, "word": "sit"},
        {"count": 1, "word": "ipsum"}
      ]
    }
  ]
}
```