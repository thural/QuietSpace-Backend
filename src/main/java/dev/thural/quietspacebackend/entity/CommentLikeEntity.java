package dev.thural.quietspacebackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment_like")
public class CommentLikeEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @Version
    private Integer version;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private UserEntity user;

    @NotNull
    @ManyToOne
    @JsonIgnore
    private CommentEntity comment;

    @NotNull
    @Column(updatable = false)
    private OffsetDateTime createDate;

    @JsonIgnore
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
