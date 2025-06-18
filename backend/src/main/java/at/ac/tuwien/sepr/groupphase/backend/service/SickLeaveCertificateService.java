package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.sickleave.SickLeaveCertificateUploadDatesDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.SickLeaveCertificate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.dto.sickleave.SickLeaveCertificateUploadDto;

import java.util.List;

public interface SickLeaveCertificateService {
    /**
     * Uploads a new sick leave certificate for the given user.
     *
     * @param uploadDto the DTO containing the file and metadata for the upload
     * @return the uploaded SickLeaveCertificate as a DTO
     * @throws NotFoundException if the user with the given email does not exist
     * @throws ConflictException if a certificate for the same date range already exists
     */
    SickLeaveCertificateDto upload(SickLeaveCertificateUploadDto uploadDto) throws NotFoundException, ConflictException;

    /**
     * Finds a sick leave certificate by its ID.
     *
     * @param id the ID of the sick leave certificate
     * @return the SickLeaveCertificate with the given ID
     * @throws NotFoundException if no certificate with the given ID exists
     */
    SickLeaveCertificate findById(Long id) throws NotFoundException;

    /**
     * Retrieves all sick leave certificates for the current user.
     *
     * @return a list of SickLeaveCertificateDto for the current user
     */
    List<SickLeaveCertificateDto> getAllForCurrentUser();

    /**
     * Retrieves all sick leave certificates in the system (admin only).
     *
     * @return a list of SickLeaveCertificateDto for all users
     */
    List<SickLeaveCertificateDto> getAllForAdmin();


    /**
     * Deletes a sick leave certificate by its ID, if the current user
     * is either the owner or has admin privileges.
     *
     * @param id the ID of the certificate to delete
     * @throws NotFoundException if the certificate is not found
     * @throws SecurityException if the user is not allowed to delete it
     */
    void deleteSickLeaveCertificate(Long id);

    /**
     * Retrieves all sick leave certificates of users in the same department as the supervisor.
     *
     * @param supervisorEmail Email of the logged-in supervisor
     * @return list of SickLeaveCertificateDto without file data
     */
    List<SickLeaveCertificateDto> getAllForSupervisor(String supervisorEmail);



}

