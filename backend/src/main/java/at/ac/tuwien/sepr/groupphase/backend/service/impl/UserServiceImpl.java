package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.ApplicationUserResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.RegisterTokenRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.InvitationToken;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.TokenAlreadyUsedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.TokenExpiredException;
import at.ac.tuwien.sepr.groupphase.backend.repository.InvitationTokenRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.department.DepartmentNameDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.ChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDataDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserDepartmentDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserEmailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.user.UserRoleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.sepr.groupphase.backend.config.Constants.ADMIN_EMAIL;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final InvitationTokenRepository invitationTokenRepository;
    private final MailService mailService;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           InvitationTokenRepository invitationTokenRepository,
                           MailService mailService, JwtTokenizer jwtTokenizer) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationTokenRepository = invitationTokenRepository;
        this.mailService = mailService;
        this.jwtTokenizer = jwtTokenizer;
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
            applicationUser.setFirstName(userData.getFirstName());
            applicationUser.setLastName(userData.getLastName());
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
    public void createUser(UserDataDto userData) throws IllegalArgumentException {
        LOGGER.trace("createUser({})", userData);
        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(userData.getEmail());
        if (applicationUserOpt.isPresent()) {
            throw new IllegalArgumentException("User with this email already exists!");
        } else {
            ApplicationUser applicationUser = new ApplicationUser();
            applicationUser.setEmail(userData.getEmail());
            applicationUser.setPasswordHash(passwordEncoder.encode(userData.getPassword()));
            applicationUser.setFirstName(userData.getFirstName());
            applicationUser.setLastName(userData.getLastName());
            userRepository.save(applicationUser);
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

    @Override
    public void changePasswordOfCurrentUser(ChangePasswordDto dto) throws NotFoundException, AccessDeniedException {
        LOGGER.trace("changePasswordOfCurrentUser({})", dto);

        String currentEmail = org.springframework.security.core.context.SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmail(currentEmail);
        if (applicationUserOpt.isEmpty()) {
            throw new NotFoundException("User with email " + currentEmail + " not found.");
        }

        ApplicationUser user = applicationUserOpt.get();

        if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
            throw new org.springframework.security.access.AccessDeniedException("Admin password cannot be changed.");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

    }


    @Override
    public UserProfileDto getCurrentUserProfile() throws NotFoundException {
        LOGGER.trace("getCurrentUserProfile()");

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<ApplicationUser> applicationUserOpt = userRepository.findByEmailWithRoles(currentEmail);
        if (applicationUserOpt.isEmpty()) {
            throw new NotFoundException("User with email " + currentEmail + " not found.");
        }

        ApplicationUser user = applicationUserOpt.get();

        String role = user.getRoles().stream()
            .findFirst()
            .map(ApplicationRole::getName)
            .orElse("UNKNOWN");

        return new UserProfileDto(
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            role,
            user.getDepartment() == null ? "NONE" : user.getDepartment().getName()
        );

    }

    @Override
    public List<ApplicationUserResponseDto> getAllAvailableUsers() {
        LOGGER.trace("getAllAvailableUsers()");

        return userRepository.findAllWithNoRole().stream()
            .map(user -> new ApplicationUserResponseDto(user.getEmail()))
            .collect(Collectors.toList());
    }

    @Override
    public UserDepartmentDto getUserByEmail(UserEmailDto emailDto) throws NotFoundException {
        ApplicationUser user = userRepository.findByEmail(emailDto.email())
            .orElseThrow(() -> new NotFoundException("User with email " + emailDto.email() + " not found."));

        if (user.getDepartment() != null) {
            return new UserDepartmentDto(user.getEmail(), user.getDepartment().getName());
        } else {
            return new UserDepartmentDto(user.getEmail(), "NONE");
        }
    }

    @Override
    public void checkAccessToDepartment(DepartmentNameDto departmentNameDto) throws AccessDeniedException {
        UserProfileDto currentUser = getCurrentUserProfile();

        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getDepartment().equals(departmentNameDto.name())) {
            throw new AccessDeniedException("You do not have access to department " + departmentNameDto.name() + " !");
        }
    }

    @Override
    public List<ApplicationUserResponseDto> getAllSupervisors(boolean hasDepartment) {
        LOGGER.trace("getAllSupervisors({})", hasDepartment);

        Stream<ApplicationUser> users = userRepository.findAllByRoleName("SUPERVISOR").stream();

        if (hasDepartment) {
            users = users.filter(user -> user.getDepartment() != null);
        } else {
            users = users.filter(user -> user.getDepartment() == null);
        }

        return users
            .map(user -> new ApplicationUserResponseDto(user.getEmail()))
            .collect(Collectors.toList());
    }


    // Aktualisiere den Konstruktor entsprechend mit den neuen Abhängigkeiten

    @Override
    public void createInvitation(String email) throws IllegalArgumentException {
        LOGGER.trace("createInvitation({})", email);

        // Überprüfen, ob der Benutzer bereits existiert
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists!");
        }

        // Prüfen, ob bereits ein Token für diese E-Mail existiert
        Optional<InvitationToken> existingToken = invitationTokenRepository.findByEmail(email);
        if (existingToken.isPresent()) {
            // Wenn der existierende Token noch gültig und nicht benutzt ist, diesen verwenden
            InvitationToken token = existingToken.get();
            if (!token.isExpired() && !token.isUsed()) {
                sendInvitationEmail(email, token.getToken());
                return;
            } else {
                // Sonst löschen und einen neuen erstellen
                invitationTokenRepository.delete(token);
            }
        }

        // Token generieren
        String tokenValue = jwtTokenizer.generateInvitationToken(email);

        System.out.println("token: " + tokenValue + " for " + email + " generated.");

        // Token speichern
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24); // Token ist 24 Stunden gültig
        InvitationToken token = new InvitationToken(tokenValue, email, expiryDate);
        invitationTokenRepository.save(token);

        // E-Mail senden
        sendInvitationEmail(email, tokenValue);

    }

    private void sendInvitationEmail(String email, String token) {
        //todo
        String invitationLink = "http://localhost:4200/auth/sign-up?token=" + token;
        String subject = "Einladung zur Registrierung";
        String body = "Hallo,\n\n"
            + "Sie wurden eingeladen, sich bei unserer Anwendung zu registrieren.\n"
            + "Bitte klicken Sie auf den folgenden Link, um die Registrierung abzuschließen:\n\n"
            + invitationLink + "\n\n"
            + "Dieser Link ist 24 Stunden gültig.\n\n"
            + "Mit freundlichen Grüßen,\n"
            + "Ihr Anwendungsteam";

        mailService.sendSimpleEmail(email, subject, body);
    }

    @Override
    public String validateInvitationToken(String token) throws NotFoundException, TokenExpiredException, TokenAlreadyUsedException {
        LOGGER.trace("validateInvitationToken({})", token);

        InvitationToken invitationToken = invitationTokenRepository.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation token not found"));

        if (invitationToken.isExpired()) {
            throw new TokenExpiredException("Invitation token has expired");
        }

        if (invitationToken.isUsed()) {
            throw new TokenAlreadyUsedException("Invitation token has already been used");
        }

        return invitationToken.getEmail();
    }

    @Override
    public void registerUserWithToken(RegisterTokenRequestDto userData, String token)
        throws NotFoundException, TokenExpiredException, TokenAlreadyUsedException, IllegalArgumentException {
        LOGGER.trace("registerUserWithToken({}, {})", userData, token);

        InvitationToken invitationToken = invitationTokenRepository.findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invitation token not found"));

        if (invitationToken.isExpired()) {
            throw new TokenExpiredException("Invitation token has expired");
        }

        if (invitationToken.isUsed()) {
            throw new TokenAlreadyUsedException("Invitation token has already been used");
        }

        // Überprüfe, ob die E-Mail-Adresse mit dem Token übereinstimmt
        if (!invitationToken.getEmail().equals(userData.email())) {
            throw new IllegalArgumentException("Email address does not match the invitation token");
        }

        // Benutzer erstellen
        createUser(new UserDataDto(userData.email(), userData.password(), userData.firstName(), userData.lastName()));

        // Token als verwendet markieren
        invitationToken.setUsed(true);
        invitationTokenRepository.save(invitationToken);
    }
}
