package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.thural.quietspace.utils.enums.RoleType;
import dev.thural.quietspace.utils.enums.StatusType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails, Principal {

    @NotBlank
    @Column(length = 32, unique = true)
    private String username;

    @NotBlank
    @Column(length = 32, unique = true)
    private String email;

    @NotBlank
    @JsonIgnore
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    referencedColumnName = "id"))
    private List<Chat> chats;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;


    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_followings",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "followings_id")
    )
    private List<User> followings = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "followings")
    private List<User> followers = new ArrayList<>();


    @JsonIgnore
    private String firstname;
    @JsonIgnore
    private String lastname;
    @JsonIgnore
    private OffsetDateTime dateOfBirth;
    @JsonIgnore
    private boolean accountLocked;
    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    private StatusType statusType;


    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    private List<RoleType> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String fullName() {
        return getFirstname() + " " + getLastname();
    }

    @Override
    public String getName() {
        return username;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public void addRole(RoleType role) {
        this.roles.add(role);
    }

    @PrePersist
    void initAccount() {
        setEnabled(true);
        setAccountLocked(false);
        setStatusType(StatusType.ONLINE);
        setRoles(new ArrayList<>(List.of(RoleType.USER)));
    }

}