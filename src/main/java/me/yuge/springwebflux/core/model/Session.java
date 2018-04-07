package me.yuge.springwebflux.core.model;

import lombok.*;
import org.springframework.data.annotation.Id;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    private String id;
    private String userId;
    private String username;

    @Builder.Default()
    private String[] roles = new String[]{};

}
