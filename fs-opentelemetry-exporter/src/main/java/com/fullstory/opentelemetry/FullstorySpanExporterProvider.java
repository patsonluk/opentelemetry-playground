package com.fullstory.opentelemetry;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

@AutoService(ConfigurableSpanExporterProvider.class)
public class FullstorySpanExporterProvider implements ConfigurableSpanExporterProvider {
    @Override
    public SpanExporter createExporter(ConfigProperties config) {
        return new com.fullstory.opentelemetry.FullstorySpanExporter(config.getString("otel.exporter.fullstory.api.key"));
    }

    @Override
    public String getName() {
        return "fullstory";
    }
}
