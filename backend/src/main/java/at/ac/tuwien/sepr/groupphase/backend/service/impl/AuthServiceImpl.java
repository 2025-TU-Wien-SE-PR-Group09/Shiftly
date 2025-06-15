package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDataLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Department;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.LoginResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponseDto login(UserDataLoginDto userLoginDto) throws BadCredentialsException {
        LOGGER.trace("login({})", userLoginDto);
        UserDetails userDetails = loadUserByUsername(userLoginDto.getEmail());
        if (userDetails != null
            && userDetails.isAccountNonExpired()
            && userDetails.isAccountNonLocked()
            && userDetails.isCredentialsNonExpired()
            && passwordEncoder.matches(userLoginDto.getPassword(), userDetails.getPassword())) {

            var department = userRepository.findByEmail(userLoginDto.getEmail()).map(ApplicationUser::getDepartment);

            var depName = department.map(Department::getName);
            List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return new LoginResponseDto(
                jwtTokenizer.getAuthToken(userDetails.getUsername(), roles, depName));
        }
        throw new BadCredentialsException("Username or password is incorrect or account is locked");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.trace("loadUserByUsername({})", email);
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(email);
        if (applicationUserOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        ApplicationUser applicationUser = applicationUserOpt.get();

        String[] roles = applicationUser.getRoles().stream().map(ApplicationRole::getName).toArray(String[]::new);
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(roles);

        return new User(applicationUser.getEmail(), applicationUser.getPasswordHash(), grantedAuthorities);
    }

    @Override
    public ApplicationUser getCurrentUser() throws UsernameNotFoundException {
        LOGGER.trace("getCurrentUser()");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authentication found");
        }

        String email = (String) authentication.getPrincipal();

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

}
