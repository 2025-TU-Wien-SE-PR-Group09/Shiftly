import { Component, OnInit } from '@angular/core';
import {
  DepartmentUserManagementComponent
} from '../../components/department-user-management/department-user-management.component';
import { UserEndpointService, UserProfileRestDto } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-department-detail-supervisor',
  imports: [DepartmentUserManagementComponent],
  templateUrl: './department-detail-supervisor.component.html',
  styleUrl: './department-detail-supervisor.component.css',
})
export class DepartmentDetailSupervisorComponent implements OnInit {
  departmentName: string = '';

  constructor(private toastrService: ToastrService, private userService: UserEndpointService) {}

  ngOnInit(): void {
    this.userService.getCurrentUserProfile().subscribe({
      next: (data: UserProfileRestDto) => {
        this.departmentName = data.department!;
      },
    });
  }
}
