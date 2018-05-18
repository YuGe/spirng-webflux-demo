package me.yuge.springwebflux.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.yuge.springwebflux.core.model.User;

import java.beans.Transient;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    public enum Type {CHAT_MESSAGE, USER_JOINED, USER_STATS, USER_LEFT}

    private int id;
    private Instant time;
    private Type type;
    private Payload payload;

    @Transient
    public User getUser() {
        return getPayload() == null ? null : getPayload().getUser();
    }
}
