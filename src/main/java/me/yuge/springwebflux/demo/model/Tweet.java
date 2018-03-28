package me.yuge.springwebflux.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@Document(collection = "tweets")
public class Tweet {

    @Id
    private String id;

    @NotBlank
    @Size(max = 140)
    private String text;

    @NotNull
    @Field("created_at")
    private ZonedDateTime createdAt = ZonedDateTime.now();

    public Tweet(String text) {
        this.text = text;
    }
}
