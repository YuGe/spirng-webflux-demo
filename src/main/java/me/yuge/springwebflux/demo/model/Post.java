package me.yuge.springwebflux.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Post {

    @Id
    private String id;
    private String title;
    private String content;

}
