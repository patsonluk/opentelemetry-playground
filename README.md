A sample node js app that sends XHR to an OpenTelemetry instrumented java backend.

## Setup
Prereq : need to have npm, java and maven installed

### Run an OT instrumented node js app
This starts a test nodejs app on localhost:1234 with the opentelemetry browser instrumentation. For now we are only testing auto w3c trace context injection for xml http request. (auto injection works, but it only injects `traceparent` header, what we want is our FS uid/page url as `tracestate` header. Currenly only hard coded in the javascript that makes the call)

1. `git clone https://github.com/patsonluk/opentelemetry-playground.git`
2. `cd opentelemetry-playground`
3. `npm install --save @opentelemetry/api @opentelemetry/sdk-trace-web @opentelemetry/instrumentation-document-load @opentelemetry/context-zone @opentelemetry/instrumentation-xml-http-request`
4. `npx parcel index.html` 


### Run an OT instrumented java webapp
1. In another terminal, `cd jetty-helloworld-webapp`
2. `MAVEN_OPTS="-javaagent:../opentelemetry-javaagent.jar" mvn jetty:run-war -Djetty.port=9999`
3. A java app server should be bound to localhost:9999. There's only one endpoint that would trigger a DB read and a test exception `localhost:9999/test-servlet`

:information_source: If using the test Java agent exporter below, then start the java app with this instead `MAVEN_OPTS="-javaagent:../opentelemetry-javaagent.jar -Dotel.traces.exporter=fullstory -Dotel.javaagent.extensions=../fs-opentelemetry-exporter/target/fs-opentelemetry-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar -Dotel.exporter.fullstory.api-key=<staging API key>" mvn jetty:run-war  -Djetty.port=9999`


### Run the modified OT collector that exports FS server events
Follow this https://github.com/patsonluk/opentelemetry-collector/blob/main/README.md


### Compile a test Java agent exporter (Not needing this anymore - skip!!!)
This is NOT going to be our final approach, but adding a java agent exporter for quick test. We probably want to setup an Otel collector and provide our exporter implementation so we are not tied to any server language/agent. https://opentelemetry.io/docs/collector/

1. `cd fs-opentelemetry-exporter`
2. `mvn clean package`
3. This builds our exporter in `target/fs-opentelemetry-exporter-1.0-SNAPSHOT-jar-with-dependencies.jar`

:information_source: This used to be 8080 but it appears to conflict with some ports for FS service restarts


## Trigger instrumentation
At this point you should have 2 apps running, the localhost:1234 should have both Fullstory recording and OT instrumetnation enabled. And the localhost:9999 should have OT java agent instrumentation.

Visit localhost:1234 with the developer tool in browser, goto the network tab, click on the `Click me` button on the page. You should observe an outbound xml http call to localhost:9999/test-servlet. Inspect the request header, you should see the `traceparent` being injected by the OT instrumentation, and `tracestate` (not injected by OT now, but ideally it should!) with FS identities
![image](https://user-images.githubusercontent.com/2895902/200657394-f34675c7-915b-45eb-ab6b-7f694f3947d8.png)

It should have 500 response code, as localhost:9999/test-servlet simulates Db reads and unhandled exception.

Now goto the corresponding FS account on staging, we should be able to see the FS session and some server events in there. **there could be time drift and throttling/quota issue**

![image](https://user-images.githubusercontent.com/2895902/200657802-2c4f59aa-1a24-4be8-8452-02cdf42cbea8.png)


