package me.yuge.springwebflux.core.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;


@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureContentTypeResolver(RequestedContentTypeResolverBuilder builder) {
        builder.resolver(new HeaderContentTypeResolver());
    }

    private class HeaderContentTypeResolver implements RequestedContentTypeResolver {

        @Override
        public List<MediaType> resolveMediaTypes(ServerWebExchange exchange) {

            final HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
            final HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

            try {
                List<MediaType> mediaTypes = requestHeaders.getAccept();
                MediaType.sortBySpecificityAndQuality(mediaTypes);
                System.out.println(mediaTypes);

                responseHeaders.set("X-Demo-Media-Type", "demo.v3; param=full; format=json");

                return mediaTypes;
            } catch (InvalidMediaTypeException ex) {
                final String value = requestHeaders.getFirst(HttpHeaders.ACCEPT);
                throw new NotAcceptableStatusException(
                        "Could not parse 'Accept' header [" + value + "]: " + ex.getMessage());
            }
        }
    }

}
