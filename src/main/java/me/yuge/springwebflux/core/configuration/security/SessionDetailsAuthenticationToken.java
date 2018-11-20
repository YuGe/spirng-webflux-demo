package me.yuge.springwebflux.core.configuration.security;

import me.yuge.springwebflux.core.model.Session;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

class SessionDetailsAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -1126325598266222359L;

    private final Object principal;
    private Object credentials;

    /**
     * Creates a code with the supplied array of authorities.
     * {@link #isAuthenticated()} will return <code>false</code>.
     *
     * @param principal   The identity of the principal being authenticated.
     *                    This would be the username.
     * @param credentials The credentials that prove the principal is correct.
     *                    This would be the password (should be encrypted) when basic auth.
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    SessionDetailsAuthenticationToken(Object principal, Object credentials, Session details, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setDetails(details);
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this code to trusted - use constructor which takes a GrantedAuthority list instead"
            );
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        credentials = null;
    }
}
