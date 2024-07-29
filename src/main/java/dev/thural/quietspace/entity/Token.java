package dev.thural.quietspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Token extends BaseEntity {

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

    private OffsetDateTime expireDate;
    private OffsetDateTime validateDate;

}