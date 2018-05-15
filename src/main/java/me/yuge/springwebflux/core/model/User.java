package me.yuge.springwebflux.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true, sparse = true)
    private String email;
    @Indexed(unique = true, sparse = true)
    private String phone;
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonProperty))
    private String password;
    @Indexed(unique = true, sparse = true)
    private String[] login; // contains username, email, phone
    @Builder.Default()
    private String[] roles = new String[]{};
    @Builder.Default()
    private boolean active = true;
    @Builder.Default()
    private Instant createdTime = Instant.now();
    @Builder.Default()
    private Instant modifiedTime = Instant.now();

    private static final String ROLE_PREFIX = "ROLE_";

    public static Collection<? extends GrantedAuthority> getAuthorities(String[] roles) {
        List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
        for (String role : roles) {
            if (role.startsWith(ROLE_PREFIX)) {
                throw new IllegalArgumentException("Role shouldn't starts with " + ROLE_PREFIX);
            }
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));
        }
        return authorities;
    }
}
