package me.yuge.springwebflux.core.model;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    private String id;
    private String userId;
    private String username;

    @Builder.Default()
    private String[] roles = new String[]{};

}
