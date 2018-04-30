package me.yuge.springwebflux.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    private static final String ROLE_PREFIX = "ROLE_";

    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true, sparse = true)
    private String email;
    @Indexed(unique = true, sparse = true)
    private String phone;
    private String password;
    @Indexed(unique = true, sparse = true)
    private String[] login; // contains username, email, phone
    @Builder.Default()
    private String[] roles = new String[]{};
    @Builder.Default()
    private boolean active = true;

    public static Collection<? extends GrantedAuthority> getAuthorities(String[] roles) {
        List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
        for (String role : roles) {
            if (role.startsWith(ROLE_PREFIX)) {
                throw new IllegalArgumentException("Role shouldn't starts with " + ROLE_PREFIX);
            }
            role = ROLE_PREFIX + role;
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
}
