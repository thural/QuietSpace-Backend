package dev.thural.quietspacebackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {
    @Id
    private ObjectId id;
    private OffsetDateTime date = OffsetDateTime.now();
    private String username;
    private String text;
    @DocumentReference
    private List<User> likes;
    @DocumentReference
    private List<Comment> comments;
}
