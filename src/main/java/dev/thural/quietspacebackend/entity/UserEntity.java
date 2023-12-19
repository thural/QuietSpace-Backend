package dev.thural.quietspacebackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Integer version;

    @NotNull
    @NotBlank
    private String role;

    @NotNull
    @NotBlank
    @Size(max = 32)
    @Column(length = 32)
    private String username;

    @NotNull
    @NotBlank
    private String password;

    @OneToMany(mappedBy = "user")
    private List<PostEntity> posts;

    @OneToMany(mappedBy = "user")
    private List<PostLikeEntity> postLikes;

    @OneToMany(mappedBy = "user")
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "user")
    private List<CommentLikeEntity> commentLikes;

    @NotNull
    @Column(updatable = false)
    private OffsetDateTime createDate;

    @NotNull
    private OffsetDateTime updateDate;

    @PrePersist
    private void onCreate() {
        createDate = OffsetDateTime.now();
        updateDate = OffsetDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updateDate = OffsetDateTime.now();
    }

}