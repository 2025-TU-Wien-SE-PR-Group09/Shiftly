import {Component, OnInit} from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  DepartmentDetailRestDto,
  DepartmentCreateRestDto,
  DepartmentService,
  ApplicationUserResponseDto
} from '../../../../rest_client';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-department-detail-admin',
  imports: [ButtonComponent, FormsModule, CommonModule, RouterLink],
  templateUrl: './department-detail-admin.component.html',
  styleUrl: './department-detail-admin.component.css',
})
export class DepartmentDetailAdminComponent implements OnInit{
  protected showForm: boolean | undefined;
  protected newDepartment: DepartmentCreateRestDto = {
    name: '',
    supervisorEmail: '',
  };
  protected departments: DepartmentDetailRestDto[] = [];
  protected supervisors: ApplicationUserResponseDto[] = [];

  constructor(private departmentService: DepartmentService, private toastr: ToastrService) {}

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  submitDepartment(): void {
    this.departmentService.createDepartment(this.newDepartment).subscribe({
      next: () => {
        this.newDepartment = { name: '', supervisorEmail: '' };
        this.showForm = false;
        this.loadDepartments();
        this.toastr.success('Department created successfully', 'Success');
      }
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
    this.loadSupervisors();
  }

  loadDepartments(): void {
    this.departmentService.getAllDepartments().subscribe({
      next: (data) => (this.departments = data),
    });
  }

  loadSupervisors(): void {
    this.departmentService.getAllSupervisors().subscribe({
      next: (data) => this.supervisors = data,
      error: (err) => console.error('Fehler beim Laden der Supervisoren', err)
    });
  }

  editDepartment(name: string | undefined) {
    
  }
}
