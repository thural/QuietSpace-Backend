package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;


    @Version
    private Integer version;


    @CreatedBy
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(length = 36, columnDefinition = "varchar(36)", insertable = false)
    private UUID updatedBy;


    @NotNull
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private OffsetDateTime createDate;

    @NotNull
    @LastModifiedDate
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
