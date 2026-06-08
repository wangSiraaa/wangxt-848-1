package com.forest.patrol.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER_OUTPUT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter[] TIME_FORMATTERS_INPUT = {
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm")
    };

    public static class CustomLocalTimeDeserializer extends JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText().trim();
            for (DateTimeFormatter formatter : TIME_FORMATTERS_INPUT) {
                try {
                    return LocalTime.parse(text, formatter);
                } catch (DateTimeParseException e) {
                }
            }
            throw new IOException("无法解析时间: " + text + "，支持格式: HH:mm:ss 或 HH:mm");
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateSerializer(DATE_FORMATTER));
            builder.deserializers(new LocalDateDeserializer(DATE_FORMATTER));
            builder.serializers(new LocalTimeSerializer(TIME_FORMATTER_OUTPUT));
            builder.deserializers(new CustomLocalTimeDeserializer());
        };
    }
}
