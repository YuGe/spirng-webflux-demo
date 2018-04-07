package me.yuge.springwebflux.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;


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

    private ZonedDateTime createdTime;
    private ZonedDateTime modifiedTime;

    public Tweet(String text) {
        this.text = text;
        this.createdTime = ZonedDateTime.now();
        this.modifiedTime = ZonedDateTime.now();
    }
}
