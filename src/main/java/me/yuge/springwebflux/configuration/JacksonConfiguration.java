package me.yuge.springwebflux.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomSerialization() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.serializerByType(ZonedDateTime.class, new ZonedDateTimeSerializer());
            jacksonObjectMapperBuilder.deserializerByType(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            jacksonObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        };
    }

    public class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

        ZonedDateTimeSerializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(DateTimeFormatter.ISO_INSTANT.format(value));
        }
    }

    public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

        ZonedDateTimeDeserializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return ZonedDateTime.parse(p.getValueAsString()).withZoneSameInstant(ZoneId.systemDefault());
        }
    }
}
