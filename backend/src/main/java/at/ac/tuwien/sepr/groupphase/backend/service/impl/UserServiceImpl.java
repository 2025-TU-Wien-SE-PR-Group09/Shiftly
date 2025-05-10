package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            String[] roles = applicationUser.getRoles().stream().map(ApplicationRole::getName).toArray(String[]::new);
            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(roles);

            return new User(applicationUser.getEmail(), applicationUser.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(email);
        if (applicationUserOpt.isPresent()) {
            return applicationUserOpt.get();
        }
        throw new NotFoundException(String.format("Could not find the user with the email address %s", email));
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        UserDetails userDetails = loadUserByUsername(userLoginDto.getEmail());
        if (userDetails != null
            && userDetails.isAccountNonExpired()
            && userDetails.isAccountNonLocked()
            && userDetails.isCredentialsNonExpired()
            && passwordEncoder.matches(userLoginDto.getPassword(), userDetails.getPassword())
        ) {
            List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return jwtTokenizer.getAuthToken(userDetails.getUsername(), roles);
        }
        throw new BadCredentialsException("Username or password is incorrect or account is locked");
    }

    @Override
    public ApplicationUser createOrChangePassword(String email, String password) {
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(email);
        ApplicationUser applicationUser;

        if (applicationUserOpt.isEmpty()) {
            applicationUser = new ApplicationUser();

            applicationUser.setEmail(email);
            applicationUser.setPassword(passwordEncoder.encode("password"));
            userRepository.save(applicationUser);
        } else {
            applicationUser = applicationUserOpt.get();

            if (!passwordEncoder.matches(password, applicationUser.getPassword())) {
                applicationUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(applicationUser);
            }
        }

        return applicationUser;
    }

    @Override
    public void assignRoleToUser(String role, ApplicationUser user) throws NotFoundException {
        ApplicationRole applicationRole;
        Optional<ApplicationRole> applicationRoleOpt = roleRepository.findByName(role);

        if (applicationRoleOpt.isPresent()) {
            applicationRole = applicationRoleOpt.get();
            applicationRole.getUsers().add(user);
            user.getRoles().add(applicationRole);
            roleRepository.save(applicationRole);
            userRepository.save(user);
        } else {
            applicationRole = new ApplicationRole(role, Set.of(user));
            user.getRoles().add(applicationRole);
            roleRepository.save(applicationRole);
            userRepository.save(user);
        }
    }
}
