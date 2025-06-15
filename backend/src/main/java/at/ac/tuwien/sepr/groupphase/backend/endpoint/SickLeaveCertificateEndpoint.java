package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave.SickLeaveCertificateUploadDatesDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave.SickLeaveCertificateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.SickLeaveCertificateMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.SickLeaveCertificateService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateUploadDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * REST endpoint for managing sick leave certificate uploads and access.
 */
@RestController
@RequestMapping("/api/sick-leave")
@ApiResponse(responseCode = "403", description = "Access denied")
@ApiResponse(responseCode = "404", description = "Given resource not found")
@ApiResponse(responseCode = "400", description = "Invalid request data")
@ApiResponse(responseCode = "409", description = "Conflict with existing data")
public class SickLeaveCertificateEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SickLeaveCertificateService certificateService;
    private final AuthService authService;
    private final SickLeaveCertificateMapper mapper;

    public SickLeaveCertificateEndpoint(SickLeaveCertificateService certificateService,
                                        AuthService authService,
                                        SickLeaveCertificateMapper mapper) {
        this.certificateService = certificateService;
        this.authService = authService;
        this.mapper = mapper;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Upload a sick leave certificate",
        description = "Uploads a sick leave certificate file for the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Sick leave certificate uploaded successfully")
    public ResponseEntity<SickLeaveCertificateRestDto> upload(@RequestParam(name = "file") MultipartFile file,
                                                              @RequestPart(name = "uploadDto") @Valid SickLeaveCertificateUploadDatesDto dto) {
        LOGGER.trace("upload({}, {})", file, dto);

        ApplicationUser currentUser = authService.getCurrentUser();
        validateFile(file);
        var serviceDto = certificateService.upload(new SickLeaveCertificateUploadDto(
            file, currentUser.getEmail(), dto.getStartDate(), dto.getEndDate()
        ));
        return ResponseEntity.ok(mapper.toRest(serviceDto));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get a sick leave certificate by ID",
        description = "Returns the metadata of a sick leave certificate by its ID. Only the owner or an admin can access it."
    )
    @ApiResponse(responseCode = "200", description = "Sick leave certificate retrieved successfully")
    public ResponseEntity<SickLeaveCertificateRestDto> getById(@PathVariable(name = "id") Long id) {
        LOGGER.trace("getById({})", id);

        ApplicationUser currentUser = authService.getCurrentUser();
        var cert = certificateService.findById(id);

        if (!canAccess(currentUser, cert.getEmployee())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var serviceDto = new SickLeaveCertificateDto(
            cert.getId(),
            cert.getFileName(),
            cert.getFileType(),
            cert.getUploadedAt(),
            cert.getEmployee().getEmail(),
            cert.getStartDate(),
            cert.getEndDate()
        );

        return ResponseEntity.ok(mapper.toRest(serviceDto));
    }

    @GetMapping(path = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get all sick leave certificates for the current user",
        description = "Returns a list of sick leave certificates belonging to the currently authenticated user."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of sick leave certificates for the current user"
    )
    public ResponseEntity<List<SickLeaveCertificateRestDto>> getMyCertificates() {
        LOGGER.trace("getMyCertificates()");

        var serviceDtos = certificateService.getAllForCurrentUser();
        var restDtos = serviceDtos.stream().map(mapper::toRest).toList();
        return ResponseEntity.ok(restDtos);
    }


    @Operation(
        summary = "Get certificates for the current user",
        description = "Returns a list of sick leave certificates belonging to the currently authenticated user."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of sick leave certificates"
    )
    @GetMapping(path = "/admin/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<List<SickLeaveCertificateRestDto>> getAllCertificates() {
        LOGGER.trace("getAllCertificates()");

        ApplicationUser currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var serviceDtos = certificateService.getAllForAdmin();
        var restDtos = serviceDtos.stream().map(mapper::toRest).toList();
        return ResponseEntity.ok(restDtos);
    }

    @GetMapping(path = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RolesAllowed({"EMPLOYEE", "ADMIN"})
    @Operation(
        summary = "Download a sick leave certificate file",
        description = "Downloads the actual file content of a sick leave certificate by its ID. "
            + "Only the owner or an admin can access it."
    )
    @ApiResponse(responseCode = "200", description = "Sick leave certificate file downloaded successfully")
    public ResponseEntity<byte[]> download(@PathVariable(name = "id") Long id) {
        LOGGER.trace("download({})", id);

        ApplicationUser currentUser = authService.getCurrentUser();
        var cert = certificateService.findById(id);

        if (!canAccess(currentUser, cert.getEmployee())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] data = cert.getData();
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("The file content is missing or corrupted");
        }

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + cert.getFileName() + "\"")
            .header("Content-Type", cert.getFileType())
            .body(data);
    }

    /**
     * Checks if the current user can access the sick leave certificate.
     * Access is granted if the user is the owner of the certificate or has an admin role.
     *
     * @param user  the current user
     * @param owner the owner of the certificate
     * @return true if access is allowed, false otherwise
     */
    private boolean canAccess(ApplicationUser user, ApplicationUser owner) {
        LOGGER.trace("canAccess({}, {})", user, owner);

        boolean isOwner = user.getEmail().equals(owner.getEmail());
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"));
        return isOwner || isAdmin;
    }

    /**
     * Validates the uploaded file for size, type, and name.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if the file is invalid
     */
    private void validateFile(MultipartFile file) {
        LOGGER.trace("validateFile({})", file);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File must be smaller than 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("application/pdf")
            || contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }
    }

    @DeleteMapping("/{id}")
    @RolesAllowed({"ADMIN", "EMPLOYEE"})
    @Operation(
        summary = "Delete a sick leave certificate",
        description = "Deletes a sick leave certificate by its ID. "
            + "Only the owner or an admin can perform this operation."
    )
    @ApiResponse(responseCode = "204", description = "Sick leave certificate deleted successfully")
    public ResponseEntity<Void> deleteSickLeaveCertificate(@PathVariable(name = "id") Long id) {
        LOGGER.trace("deleteSickLeaveCertificate({})", id);

        ApplicationUser currentUser = authService.getCurrentUser();
        SickLeaveCertificate cert = certificateService.findById(id);

        if (!canAccess(currentUser, cert.getEmployee())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        certificateService.deleteSickLeaveCertificate(id);
        return ResponseEntity.noContent().build();
    }

}
