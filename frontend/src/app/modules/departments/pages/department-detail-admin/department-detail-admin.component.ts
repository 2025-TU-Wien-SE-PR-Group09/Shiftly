import {Component, OnInit} from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  DepartmentDetailRestDto,
  DepartmentCreateRestDto,
  DepartmentService,
  ApplicationUserResponseDto, DepartmentEditRestDto
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
  protected showFormCreate: boolean | undefined;
  protected showFormEdit: boolean | undefined;
  protected newDepartment: DepartmentCreateRestDto = {
    name: '',
    supervisorEmail: '',
  };
  protected editedDepartment: DepartmentEditRestDto = {
    oldName: '',
    newName: '',
    supervisorEmail: '',
  }
  protected departments: DepartmentDetailRestDto[] = [];
  protected supervisors: ApplicationUserResponseDto[] = [];

  constructor(private departmentService: DepartmentService, private toastr: ToastrService) {}

  toggleFormCreate(): void {
    this.showFormCreate = !this.showFormCreate;
  }

  openFormEdit(oldName: string | undefined): void {
    const dept = this.departments.find(d => d.name === oldName);

    this.editedDepartment.oldName = oldName as string;
    this.editedDepartment.newName = oldName as string;
    if (dept?.supervisorEmail) {
      this.editedDepartment.supervisorEmail = dept.supervisorEmail;
    } else {
      this.editedDepartment.supervisorEmail = '';
    }

    this.showFormEdit = !this.showFormEdit;
  }

  closeFormEdit(): void {
    this.editedDepartment = { oldName: '', newName: '', supervisorEmail: '' };
    this.showFormEdit = !this.showFormEdit;
  }

  submitDepartmentCreate(): void {
    if (this.newDepartment.supervisorEmail === '') {
      this.toastr.error('No supervisor selected.', 'Creating department failed.')
      return;
    }
    this.departmentService.createDepartment(this.newDepartment).subscribe({
      next: () => {
        this.newDepartment = { name: '', supervisorEmail: '' };
        this.showFormCreate = false;
        this.loadDepartments();
        this.toastr.success('Department created successfully', 'Success');
      }
    });
  }

  submitDepartmentEdit(): void {
    this.departmentService.editDepartment(this.editedDepartment).subscribe({
      next: () => {
        this.editedDepartment = { oldName: '', newName: '', supervisorEmail: '' };
        this.showFormEdit = false;
        this.loadDepartments();
        this.toastr.success('Department edited successfully', 'Success');
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

  getAvailableSupervisors(): ApplicationUserResponseDto[] {
    const usedEmails = this.departments
      .filter(dept => dept.name !== this.editedDepartment.oldName)
      .map(dept => dept.supervisorEmail);

    return this.supervisors.filter(sup => !usedEmails.includes(<string>sup.email));
  }
}
