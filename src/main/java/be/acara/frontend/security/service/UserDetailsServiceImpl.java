package be.acara.frontend.security.service;


import be.acara.frontend.security.domain.JwtToken;
import be.acara.frontend.security.domain.User;
import be.acara.frontend.security.repository.UserRepository;
import be.acara.frontend.service.UserFeignClient;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import static be.acara.frontend.security.SecurityConstants.SECRET;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private JwtTokenService tokenService;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        
        ResponseEntity<Void> login = userFeignClient.login(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", user.getUsername(), user.getPassword()));
        if (!login.getHeaders().containsKey("Authorization")) {
            return null;
        }
        String authHeader = login.getHeaders().get("Authorization").get(0);
        DecodedJWT token = JWT.require(HMAC512(SECRET.getBytes()))
                .build()
                .verify(authHeader.replace("Bearer ", ""));
        JwtToken jwtToken = JwtToken.builder()
                .token(token.getToken())
                .username(username)
                .expirationDate(token.getExpiresAt())
                .build();
        tokenService.save(jwtToken);
    
        Set<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        
        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(), grantedAuthorities);
    }
}
