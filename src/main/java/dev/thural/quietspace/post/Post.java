package dev.thural.quietspace.post;
import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.user.User;
import dev.thural.quietspace.shared.entity.BaseEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Post extends BaseEntity implements Serializable {

    private String title;
    private String repostText;
    private String repostId;

    @Length(min = 1, max = 999)
    private String text;

    @JsonIgnore
    private UUID photoId;

    @NotNull
    @ManyToOne
    @JsonBackReference
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Poll poll;

    @JsonManagedReference
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @ManyToMany(mappedBy = "savedPosts")
    private List<User> savedByUsers;

}
