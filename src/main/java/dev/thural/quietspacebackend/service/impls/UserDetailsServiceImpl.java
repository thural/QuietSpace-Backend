package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(email);

        UserEntity user = optionalUser.orElseThrow(
                () -> new UsernameNotFoundException("user not found with the email"));

        List<GrantedAuthority> authorityList = Arrays.stream(user.getRole().split(","))
                .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                .collect(Collectors.toList());

        return new User(user.getEmail(),user.getPassword(),authorityList);
    }
}
