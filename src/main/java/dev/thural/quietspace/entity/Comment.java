package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {

    private UUID parentId;

    @NotBlank
    @Column(length = 999)
    private String text;

    @NotNull
    @ManyToOne
    @JsonBackReference
    private User user;

    @NotNull
    @ManyToOne
    @JsonBackReference
    private Post post;

}
