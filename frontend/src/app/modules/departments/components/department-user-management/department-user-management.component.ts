import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import {
  DepartmentService,
  EmployeeListItemResponseDto,
  UserEndpointService,
  UserProfileRestDto,
} from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { NgForOf, NgIf } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AutocompleteComponent } from '../../../../shared/components/autocomplete/autocomplete.component';

@Component({
  selector: 'app-department-user-management',
  imports: [ButtonComponent, NgForOf, NgIf, ReactiveFormsModule, FormsModule, RouterLink, AutocompleteComponent],
  templateUrl: './department-user-management.component.html',
  styleUrl: './department-user-management.component.css',
})
export class DepartmentUserManagementComponent implements OnChanges, OnInit {
  @Input() departmentName: string = '';
  employees: EmployeeListItemResponseDto[] = [];
  supervisorEmail: String | null = null;
  showFormEmployee: boolean = false;
  showFormJumper: boolean = false;
  newEmployeeMail: string = '';
  newJumperMail: string = '';
  possibleEmployeeMails: string[] = [];
  confirmingRemoveEmployee: EmployeeListItemResponseDto | null = null;

  constructor(
    private departmentService: DepartmentService,
    private toastrService: ToastrService,
    private userService: UserEndpointService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.loadData();
  }

  ngOnInit(): void {
  }

  loadData(): void {
    if (this.departmentName === '') {
      return;
    }

    this.departmentService.getAllAvailableEmployees().subscribe(
      emps => {
        this.possibleEmployeeMails = emps.map(e => e.email!);
      }
    )

    this.departmentService.getEmployeesOfDepartment(this.departmentName).subscribe({
      next: (employees) => {
        this.employees = employees;
      },
    });

    this.departmentService.getDepartmentByName(this.departmentName).subscribe({
      next: (department) => {
        this.supervisorEmail = department.supervisorEmail;
      }
    });
  }

  inviteEmployee() {
    if (!this.newEmployeeMail || !this.newEmployeeMail.trim()) {
      this.toastrService.error('Please enter a valid Employee Mail', 'Error occurred');
      return;
    }

    this.departmentService.addEmployeeToDepartment(this.departmentName, this.newEmployeeMail).subscribe({
      next: (employee) => {
        this.toastrService.success('Successfully added ' + employee.email + ' to ' + employee.departmentName
          + ' as an employee.',
          'Success');
        this.loadData();
        this.showFormEmployee = false;
      },
      error: () => {
        // TODO handle error properly
        this.toastrService.error('Failed to add employee.', 'Error');
      },
    });
  }

  inviteJumper() {
    if (!this.newJumperMail || !this.newJumperMail.trim()) {
      this.toastrService.error('Please enter a valid Jumper Mail', 'Error occurred');
      return;
    }

    this.departmentService.addJumperToDepartment(this.departmentName, this.newJumperMail).subscribe({
      next: (jumper) => {
        this.toastrService.success('Successfully added ' + jumper.email + ' to ' + jumper.departmentName
          + ' as a jumper!',
          'Success');
        this.loadData();
        this.showFormJumper = false;
      },
      error: () => {
        // TODO handle error properly
        this.toastrService.error('Failed to add jumper.', 'Error');
      },
    });
  }

  toggleConfirmRemoveEmployee(emp: EmployeeListItemResponseDto): void {
    if (emp.email === this.supervisorEmail) {
      return;
    }
    this.confirmingRemoveEmployee = emp;
  }

  cancelRemoveEmployee(): void {
    this.confirmingRemoveEmployee = null;
  }

  confirmRemoveEmployee(): void {
    if (this.confirmingRemoveEmployee) {
      this.departmentService.removeEmployeeFromDepartment(
        this.departmentName,
        this.confirmingRemoveEmployee.email!
      ).subscribe({
        next: () => {
          this.toastrService.success(
            `Employee "${this.confirmingRemoveEmployee!.firstName} ${this.confirmingRemoveEmployee!.lastName}" removed successfully`,
            'Success'
          );
          this.employees = this.employees.filter(e => e.email !== this.confirmingRemoveEmployee!.email);
          this.possibleEmployeeMails.push(this.confirmingRemoveEmployee!.email!);
          this.confirmingRemoveEmployee = null;
        },
        error: () => {
          // TODO handle error properly
          this.toastrService.error('Failed to remove employee.', 'Error');
        },
      });
    }
  }

}
