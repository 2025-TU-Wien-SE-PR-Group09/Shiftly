package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.UserRoleDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createOrChangePassword(UserDataDto userData) {
        LOGGER.trace("createOrChangePassword({})", userData);
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(userData.getEmail());
        ApplicationUser applicationUser;

        if (applicationUserOpt.isEmpty()) {
            applicationUser = new ApplicationUser();

            applicationUser.setEmail(userData.getEmail());
            applicationUser.setPasswordHash(passwordEncoder.encode(userData.getPassword()));
            userRepository.save(applicationUser);
        } else {
            applicationUser = applicationUserOpt.get();

            if (!passwordEncoder.matches(userData.getPassword(), applicationUser.getPasswordHash())) {
                applicationUser.setPasswordHash(passwordEncoder.encode(userData.getPassword()));
                userRepository.save(applicationUser);
            }
        }
    }

    @Override
    public void assignRoleToUser(UserRoleDto userRole) throws NotFoundException {
        LOGGER.trace("assignRoleToUser({})", userRole);

        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(userRole.getUserEmail());
        if (applicationUserOpt.isEmpty()) {
            throw new NotFoundException("User with email " + userRole.getUserEmail() + " does not exist!");
        }
        ApplicationUser user = applicationUserOpt.get();

        // if user already has role, return
        if (user.getRoles().stream().anyMatch(a -> a.getName().equals(userRole.getRole().name()))) {
            return;
        }

        Optional<ApplicationRole> applicationRoleOpt = roleRepository.findByName(userRole.getRole().name());

        // Many-To-Many relationships must be set on both sides in JPA, so we need to fetch both the user
        // and the role from the database
        ApplicationRole applicationRole = applicationRoleOpt
            .orElseGet(() -> new ApplicationRole(userRole.getRole().name()));

        applicationRole.getUsers().add(user);
        user.getRoles().add(applicationRole);
        roleRepository.save(applicationRole);
        userRepository.save(user);
    }
}
