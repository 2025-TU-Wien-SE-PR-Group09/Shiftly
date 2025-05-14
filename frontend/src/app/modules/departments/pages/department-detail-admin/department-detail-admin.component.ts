import { Component } from '@angular/core';
import {DepartmentDetailRestDto, DepartmentCreateRestDto, DepartmentService} from "../../../../rest_client";
import {ButtonComponent} from "../../../../shared/components/button/button.component";
import {FormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-department-detail-admin',
  imports: [ButtonComponent, FormsModule, CommonModule],
  templateUrl: './department-detail-admin.component.html',
  styleUrl: './department-detail-admin.component.css'
})

export class DepartmentDetailAdminComponent {
  protected showForm: boolean | undefined;
  protected newDepartment: DepartmentCreateRestDto = {
    name: '',
    supervisorEmail: ''
  };
  protected departments: DepartmentDetailRestDto[] = [];

  constructor(private departmentService: DepartmentService) {}

  toggleForm(): void {
    this.showForm = !this.showForm;
  }

  submitDepartment(): void {
    this.departmentService.createDepartment(this.newDepartment).subscribe({
      next: () => {
        this.newDepartment = { name: '', supervisorEmail: '' };
        this.showForm = false;
        this.loadDepartments(); // neu laden
      },
      error: err => {
        console.error('Failed to create department', err);
      }
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.departmentService.getAllDepartments().subscribe({
      next: data => this.departments = data,
      error: err => console.error('Error loading departments', err)
    });
  }
}
