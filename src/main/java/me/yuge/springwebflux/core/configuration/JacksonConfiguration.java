package me.yuge.springwebflux.core.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomSerialization() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            jacksonObjectMapperBuilder.featuresToEnable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);

            jacksonObjectMapperBuilder.serializerByType(Duration.class, new DurationSerializer());
            jacksonObjectMapperBuilder.serializerByType(ZonedDateTime.class, new ZonedDateTimeSerializer());

            jacksonObjectMapperBuilder.deserializerByType(Duration.class, new DurationDeserializer());
            jacksonObjectMapperBuilder.deserializerByType(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        };
    }

    class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {
        private static final long serialVersionUID = 5990693242833921457L;

        ZonedDateTimeSerializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(DateTimeFormatter.ISO_INSTANT.format(value));
        }
    }

    public class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {
        private static final long serialVersionUID = 821949036695831435L;

        ZonedDateTimeDeserializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return ZonedDateTime.parse(p.getValueAsString()).withZoneSameInstant(ZoneId.systemDefault());
        }
    }

    class DurationSerializer extends StdSerializer<Duration> {

        private static final long serialVersionUID = -2503379922016963229L;

        DurationSerializer() {
            super(Duration.class);
        }

        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(value.toNanos());
        }
    }

    class DurationDeserializer extends StdDeserializer<Duration> {

        private static final long serialVersionUID = 3749670507765516154L;

        DurationDeserializer() {
            super(Duration.class);
        }

        @Override
        public Duration deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return Duration.ofNanos(p.getValueAsLong());
        }
    }
}
