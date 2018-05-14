package me.yuge.springwebflux.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tweets")
public class Tweet {

    @Id
    private String id;

    @NotBlank
    @Size(max = 140)
    private String text;

    @Builder.Default
    private Instant createdTime = Instant.now();

    @Builder.Default
    private Instant modifiedTime = Instant.now();

    public Tweet(@NotBlank @Size(max = 140) String text) {
        this.text = text;
    }

}
