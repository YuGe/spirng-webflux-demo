package me.yuge.springwebflux.core.configuration;

import com.google.common.collect.Lists;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;


@Component
public class WebFluxConfig implements WebFluxConfigurer {
    @Override
    public void configureContentTypeResolver(RequestedContentTypeResolverBuilder builder) {
        builder.resolver(new HeaderContentTypeResolver());
    }

    private class HeaderContentTypeResolver implements RequestedContentTypeResolver {
        @Override
        @NonNull
        public List<MediaType> resolveMediaTypes(@NonNull ServerWebExchange exchange) {
            HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

            List<MediaType> mediaTypes = requestHeaders.getAccept();
            if (mediaTypes.size() < 1) {
                return Lists.newArrayList(MediaType.ALL);
            }
            MediaType.sortBySpecificityAndQuality(mediaTypes);

            // TODO: 2018/05/01 implement Custom Media Type
            for (MediaType mediaType: mediaTypes) {
                if (mediaType.getSubtype().startsWith("vnd")) {
                    if (mediaType.getSubtype().endsWith("json")) {
                        responseHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
                    }
                }
                responseHeaders.set("X-Demo-Media-Type", "demo.v3; param=full; format=json");
            }

            return mediaTypes;
        }
    }
}
