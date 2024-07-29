package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {

    private UUID parentId;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private User user;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private Post post;

}
