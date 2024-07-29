package dev.thural.quietspace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class Token {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @NotBlank
    @Column(unique = true)
    private String token;

    @NotNull
    @NotBlank
    @Email
    private String email;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @NotNull
    private OffsetDateTime createDate;
    private OffsetDateTime expireDate;
    private OffsetDateTime validateDate;

    @PrePersist
    private void onCreate() {
        createDate = OffsetDateTime.now();
    }

}