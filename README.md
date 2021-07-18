# events-analyser


### Requirements
* scala 2.13
* sbt

### Run the tests
```
sbt clean test
```

### Run the app 

Build the jar file (generated in `target/scala-2.13`)
```
sbt clean compile assembly
```

Run the app

```
./blackbox | scala target/scala-2.13/events-analyser-assembly-0.1.0-SNAPSHOT.jar 
```

Query the endpoint to see the latest windowed word count grouped by event type

```
curl "http://localhost:8080/metrics"
```