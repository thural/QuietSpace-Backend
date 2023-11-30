package dev.thural.quietspacebackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private ObjectId id;
    private OffsetDateTime date = OffsetDateTime.now();
    @DocumentReference
    private User userId;
    private String text;
    @DocumentReference
    private List<User> likes;
}
