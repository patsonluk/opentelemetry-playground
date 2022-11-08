# opentelemetry-playground

## Setup
Prereq : need to have npm, java and maven installed

### Run an OT instrumented node js app
This starts a test nodejs app on localhost:1234 with the opentelemetry browser instrumentation. For now we are only testing auto w3c trace context injection for xml http request. (auto injection works, but it only injects `traceparent` header, what we want is our FS uid/page url as `tracestate` header. Currenly only hard coded in the javascript that makes the call)

1. `git clone https://github.com/patsonluk/opentelemetry-playground.git`
2. `cd opentelemetry-playground`
3. `npm install --save @opentelemetry/api @opentelemetry/sdk-trace-web @opentelemetry/instrumentation-document-load @opentelemetry/context-zone @opentelemetry/instrumentation-xml-http-request`
4. `npx parcel index.html` 

### Compile a test agent exporter
This is NOT going to be our final approach, but adding a java agent exporter for quick test. We probably want to setup an Otel collector and provide our exporter implementation so we are not tied to any server language/agent. https://opentelemetry.io/docs/collector/

1. `cd fs-opentelemetry-exporter`
2. `mvn clean package`
3. This builds our exporter in `target/fs-opentelemetry-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Run an OT instrumented java webapp
1. `cd jetty-helloworld-webapp`
2. `MAVEN_OPTS="-javaagent:../opentelemetry-javaagent.jar -Dotel.traces.exporter=fullstory -Dotel.javaagent.extensions=../fs-opentelemetry-exporter/target/fs-opentelemetry-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar -Dotel.exporter.fullstory.api-key=<staging API key>" mvn jetty:run-war`
3. A java app server should be bound to localhost:8080. There's only one endpoint that would trigger a DB read and a test exception `localhost:8080/test-servlet`


