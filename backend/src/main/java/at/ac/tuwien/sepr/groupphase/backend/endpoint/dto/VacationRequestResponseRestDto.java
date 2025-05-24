package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestDto;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.VacationRequestResponseDto;
import java.time.LocalDate;

public class VacationRequestResponseRestDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static VacationRequestResponseRestDto from(VacationRequestResponseDto dto) {
        VacationRequestResponseRestDto response = new VacationRequestResponseRestDto();
        response.setId(dto.getId());
        response.setStartDate(dto.getStartDate());
        response.setEndDate(dto.getEndDate());
        response.setStatus(dto.getStatus());
        return response;
    }
}
