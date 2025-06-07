package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SickLeaveCertificateUploadDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave.SickLeaveCertificateRestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.SickLeaveCertificateMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.SickLeaveCertificateService;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
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

import java.util.List;

/**
 * REST endpoint for managing sick leave certificate uploads and access.
 */
@RestController
@RequestMapping("/api/sick-leave")
public class SickLeaveCertificateEndpoint {

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

    /**
     * Upload a sick leave certificate for the current user.
     *
     * @param file the file to upload
     * @return metadata of the uploaded certificate
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SickLeaveCertificateRestDto> upload(@RequestParam(name = "file") MultipartFile file,
                                                              @RequestPart(name = "uploadDto") @Valid SickLeaveCertificateUploadDto dto) {

        ApplicationUser currentUser = authService.getCurrentUser();
        validateFile(file);
        var serviceDto = certificateService.upload(file, currentUser.getEmail(), dto.getStartDate(), dto.getEndDate());
        return ResponseEntity.ok(mapper.toRest(serviceDto));
    }

    /**
     * Get a certificate by ID – only allowed for the owner or an admin.
     *
     * @param id the ID of the certificate
     * @return metadata of the certificate if access is permitted, 403 otherwise
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SickLeaveCertificateRestDto> getById(@PathVariable(name = "id") Long id) {
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

    /**
     * Returns all certificates for the currently authenticated user.
     *
     * @return list of certificate DTOs
     */
    @GetMapping(path = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SickLeaveCertificateRestDto>> getMyCertificates() {
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
        description = "List of sick leave certificates",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = SickLeaveCertificateRestDto.class))
        )
    )
    @GetMapping(path = "/admin/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SickLeaveCertificateRestDto>> getAllCertificates() {
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

    /**
     * Download the actual file content of a sick leave certificate.
     * Only the owner or an admin is allowed to download the file.
     *
     * @param id the ID of the certificate to download
     * @return the file content with proper headers, or 403 if access is denied
     */
    @GetMapping(path = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RolesAllowed({"EMPLOYEE", "ADMIN"})
    public ResponseEntity<byte[]> download(@PathVariable(name = "id") Long id) {
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

    private boolean canAccess(ApplicationUser user, ApplicationUser owner) {
        boolean isOwner = user.getEmail().equals(owner.getEmail());
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> role.getName().equalsIgnoreCase("ADMIN"));
        return isOwner || isAdmin;
    }

    private void validateFile(MultipartFile file) {
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

    /**
     * Deletes a sick leave certificate by its ID.
     * Only the owner or an admin can perform this operation.
     *
     * @param id the ID of the certificate to delete
     * @return 204 No Content if deletion is successful, 403 if forbidden
     */
    @DeleteMapping("/{id}")
    @RolesAllowed({"ADMIN", "EMPLOYEE"})
    public ResponseEntity<Void> deleteSickLeaveCertificate(@PathVariable(name = "id") Long id) {
        ApplicationUser currentUser = authService.getCurrentUser();
        SickLeaveCertificate cert = certificateService.findById(id);

        if (!canAccess(currentUser, cert.getEmployee())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        certificateService.deleteSickLeaveCertificate(id);
        return ResponseEntity.noContent().build();
    }

}
