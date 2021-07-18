# events-analyser

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