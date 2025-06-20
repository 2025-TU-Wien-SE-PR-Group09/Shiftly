package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.service.dto.ConcreteShiftPlanIcalDto;

public interface IcalService {

    /**
     * Generates an iCalendar (.ics) file content as a string for a given shift plan.
     *
     * @param shiftPlanDto The concrete shift plan DTO containing all shifts.
     * @return A string representation of the iCalendar file.
     */
    String generateIcal(ConcreteShiftPlanIcalDto shiftPlanDto);

    /**
     * Generates an iCalendar (.ics) file content as a string for a given shift plan, filtered for a specific employee.
     * Only shifts assigned to the specified employee will be included.
     *
     * @param shiftPlanDto The concrete shift plan DTO containing all shifts.
     * @param employeeEmail The email of the employee to filter shifts for.
     * @return A string representation of the iCalendar file containing only the employee's shifts.
     */
    String generateEmployeeIcal(ConcreteShiftPlanIcalDto shiftPlanDto, String employeeEmail);

    /**
     * Generates an iCalendar (.ics) file content specifically for subscription with additional properties.
     * Only shifts assigned to the specified employee will be included.
     *
     * @param shiftPlanDto The concrete shift plan DTO containing all shifts.
     * @param employeeEmail The email of the employee to filter shifts for.
     * @return A string representation of the iCalendar file suitable for subscription.
     */
    String generateEmployeeSubscriptionIcal(ConcreteShiftPlanIcalDto shiftPlanDto, String employeeEmail);
}

