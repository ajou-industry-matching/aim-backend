package ajou.aim_be.auth.filter;

import ajou.aim_be.auth.service.FirebaseAuthService;
import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserStatus;
import ajou.aim_be.user.repository.UserRepository;

import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuthService firebaseAuthService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {

                FirebaseToken decoded =
                        firebaseAuthService.verifyToken(token);

                String uid = decoded.getUid();

                User user =
                        userRepository.findByFirebaseUid(uid)
                                .orElse(null);

                if (user != null) {

                    if (user.getUserStatus() == UserStatus.BLOCKED ||
                            user.getUserStatus() == UserStatus.SUSPENDED) {
                        SecurityContextHolder.clearContext();
                        filterChain.doFilter(request, response);
                        return;
                    }

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())
                    );

                    if (user.getAdminRole()== AdminRole.SUPER_ADMIN) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else if (user.getAdminRole()==AdminRole.ADMIN) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception e) {

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}