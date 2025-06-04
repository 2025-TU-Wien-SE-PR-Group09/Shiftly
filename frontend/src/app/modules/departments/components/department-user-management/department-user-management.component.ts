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
import { AutocompleteComponent } from '../../../../shared/components/autocomplete/autocomplete.component';

@Component({
  selector: 'app-department-user-management',
  imports: [ButtonComponent, NgForOf, NgIf, ReactiveFormsModule, FormsModule, AutocompleteComponent],
  templateUrl: './department-user-management.component.html',
  styleUrl: './department-user-management.component.css',
})
export class DepartmentUserManagementComponent implements OnChanges, OnInit {
  @Input() departmentName: string = '';
  employees: EmployeeListItemResponseDto[] = [];
  showForm: boolean = false;
  newEmployeeMail: string = '';
  possibleEmployeeMails: string[] = [];

  constructor(
    private departmentService: DepartmentService,
    private toastrService: ToastrService,
    private userService: UserEndpointService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.loadEmployees();
  }

  ngOnInit(): void {
    this.departmentService.getAllAvailableEmployees().subscribe(
      emps => {
        this.possibleEmployeeMails = emps.map(e => e.email!);
      }
    )
  }

  loadEmployees(): void {
    if (this.departmentName === '') {
      return;
    }

    this.departmentService.getEmployeesOfDepartment(this.departmentName).subscribe({
      next: (employees) => {
        this.employees = employees;
      },
    });


  }

  inviteEmployee() {
    if (!this.newEmployeeMail || !this.newEmployeeMail.trim()) {
      this.toastrService.error('Please enter a valid Employee Mail', 'Error occurred');
      return;
    }

    this.departmentService.addEmployeeToDepartment(this.departmentName, this.newEmployeeMail).subscribe({
      next: (employee) => {
        this.toastrService.success('Successfully added ' + employee.email + ' to ' + employee.departmentName + '!');
        this.loadEmployees();
        this.showForm = false;
      },
    });
  }
}
