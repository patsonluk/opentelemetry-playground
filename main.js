const opentelemetry = require("@opentelemetry/api");


export function ajaxCall(url) {
  var uid = "test-user-1"
  FS.identify(uid);
  var tracestate = 'fs='+ uid + ":" + FS.getCurrentSessionURL();

    const tracer = opentelemetry.trace.getTracer(
        'my-service-tracer'
    );
    console.log(tracer)
    tracer.startActiveSpan('main', span => {
        const traceId = span.spanContext().traceId

        $.ajax({
            type: 'GET',
            url: url,
            headers: { "tracestate" : tracestate, "x-b3-traceid" : traceId},
            success: function(data, status) {
                alert("Data: " + data + "\nStatus: " + status);
                // Be sure to end the span!
                span.end();
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert("error: " + xhr.responseText);
                // Be sure to end the span!
                span.end();
            }
        });

    });




}
