package com.fullstory.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FullstorySpanExporter implements SpanExporter {
    private final String apiKey;
//    private final HttpClient client;

    public FullstorySpanExporter(String apiKey) {
        this.apiKey = apiKey;

        try {
            System.out.println(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

//        System.out.println("Thread CL" + Thread.currentThread().getContextClassLoader());
//        System.out.println("This class loader " + getClass().getClassLoader());
//
//        ClassLoader walker = getClass().getClassLoader();
//        while (walker != null) {
//            for (Package definedPackage : walker.getDefinedPackages()) {
//                System.out.println("==>" + definedPackage);
//            }
//            walker = walker.getParent();
//        }
//
//        tryLoadClass("java.net.http.HttpClient");
//        tryLoadClass("java.net.URLConnection");
//        tryLoadClass("java.net.InetAddress");
//        this.client = HttpClient.newHttpClient();
    }

    private void tryLoadClass(String className) {
        tryLoadClass(className, Thread.currentThread().getContextClassLoader());
        tryLoadClass(className, getClass().getClassLoader());
    }
    private void tryLoadClass(String className, ClassLoader cl) {
        try {
            Class clazz = cl.loadClass(className);
            System.out.println(clazz + " found from " + cl);
            //System.out.println("src " + clazz.getProtectionDomain().getCodeSource().getLocation());
            System.out.println("loader " + clazz.getClassLoader());

        } catch (ClassNotFoundException e) {
            System.out.println(className + " not found from " + cl);
        }
    }

    private final String getBasicAuthenticationHeader() {
        return "Basic " + apiKey;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> collection) {
        System.out.println("FULLLLLSTORRRYYY! API KEY " + apiKey);
        for (SpanData spanData : collection) {
            System.out.println("TRACE STATE: " + spanData.getSpanContext().getTraceState());
            System.out.println(spanData);
            if (spanData.getSpanContext().getTraceState().get("fs") != null) {
                String fsValue = spanData.getSpanContext().getTraceState().get("fs");
                String uid = fsValue.split(":", 2)[0];
                String sessionUrl = fsValue.split(":", 2)[1];

                try {



                //String requestBody = "{\"event_details\":\"" + spanData.getName() + "\"}";
//                    String requestBody = "{" +
//                            "\"event\": {" +
//                                "\"event_name\":\"OpenTelemetry Server Event\"" +
//                                //"\"event_data\": {\"tada\" : \"123\"}, " +
//                                //"\"timestamp\":\"" + Instant.ofEpochMilli( spanData.getStartEpochNanos() / 1_000_000) + "\"" +
//                                //"\"event_details_start\":\"" + spanData.getStartEpochNanos() + "\"" +
//                            "}" +
//                            "}";
                  JSONObject json = new JSONObject();
                  JSONObject eventJson = new JSONObject();
                  json.put("event", eventJson);
                  eventJson.put("event_name", "OT server start");
                  Map<String, Object> dataMap = new HashMap<>();
                    for (Map.Entry<AttributeKey<?>, Object> entry : spanData.getAttributes().asMap().entrySet()) {
                        String key = entry.getKey().getKey();
                        if (entry.getKey().getType() == AttributeType.STRING) {
                            dataMap.put(key + "_str", entry.getValue());
                        } else if (entry.getKey().getType() == AttributeType.LONG) {
                            dataMap.put(key + "_int", entry.getValue());
                        } else {
                            System.err.println("No handled data type " + entry.getKey().getType() + " for key " + key);
                        }
                    }
                  eventJson.put("event_data", dataMap);
                    eventJson.put("timestamp", Instant.ofEpochMilli(spanData.getStartEpochNanos() / 1_000_000).toString());
                    eventJson.put("session_url", sessionUrl);

                  String requestBody = json.toJSONString();

//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(uri))
//                        .header("Authorization", getBasicAuthenticationHeader())
//                        .header("Content-type", "application/json")
//                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                        .build();
//
//
//                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    //                    System.out.println("RESPONSE: " + response);


                    sendToFs(uid, requestBody);

                    for (EventData event : spanData.getEvents()) {
                        json = new JSONObject();
                        eventJson = new JSONObject();
                        json.put("event", eventJson);
                        eventJson.put("event_name", "OT server event");
                        eventJson.put("timestamp", (Instant.ofEpochMilli(event.getEpochNanos() / 1_000_000)).toString());
                        eventJson.put("session_url", sessionUrl);

                        dataMap = new HashMap<>();
                        for (Map.Entry<AttributeKey<?>, Object> entry : event.getAttributes().asMap().entrySet()) {
                            String key = entry.getKey().getKey();
                            if (entry.getKey().getType() == AttributeType.STRING) {
                                dataMap.put(key + "_str", entry.getValue());
                            } else if (entry.getKey().getType() == AttributeType.LONG) {
                                dataMap.put(key + "_int", entry.getValue());
                            } else {
                                System.err.println("No handled data type " + entry.getKey().getType() + " for key " + key);
                            }
                        }
                        eventJson.put("event_data", dataMap);
                        requestBody = json.toJSONString();
                        sendToFs(uid, requestBody);

                    }


                    json = new JSONObject();
                    eventJson = new JSONObject();
                    json.put("event", eventJson);
                    eventJson.put("event_name", "OT server end");
                    eventJson.put("timestamp", (Instant.ofEpochMilli(spanData.getEndEpochNanos() / 1_000_000)).toString());
                    eventJson.put("session_url", sessionUrl);

                    requestBody = json.toJSONString();
                    sendToFs(uid, requestBody);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //curl -X POST \ https://api.staging.fullstory.com/users/v1/individual/{uid}/customvars \
                // -H 'content-type: application/json' \ -H 'Authorization: Basic {YOUR_API_KEY}'
                // -d '{ "displayName":"Daniel Falko", "email" : "daniel.falko@example.com", "pricingPlan_str" : "free", "popupHelp_bool" : true, "totalSpent_real" : 14.50 }'
            }
        }


        return CompletableResultCode.ofSuccess();
    }

    private void sendToFs(String uid, String requestBody) throws IOException {
        String uri = "https://api.staging.fullstory.com/users/v1/individual/" + uid + "/customevent";
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-type", "application/json");
        con.setRequestProperty("Authorization", getBasicAuthenticationHeader());
        con.setDoOutput(true);

        try(OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("status code " + con.getResponseCode() + " response => " + response.toString());
        }
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}
