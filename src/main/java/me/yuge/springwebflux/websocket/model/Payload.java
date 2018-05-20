package me.yuge.springwebflux.websocket.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import me.yuge.springwebflux.core.model.User;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    private User user;
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    @JsonCreator
    public Payload(@JsonProperty("user") User user) {
        this.user = user;
        this.properties = new HashMap<>();
    }

    @JsonAnyGetter
    private Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    private void setProperties(String name, Object value) {
        properties.put(name, value);
    }
}
