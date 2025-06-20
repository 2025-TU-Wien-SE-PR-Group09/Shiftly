import {Component, OnInit} from '@angular/core';
import {
  VacationEndpointService,
  VacationRequestResponseRestDto,
  VacationRequestRestDto
} from "../../../../rest_client";
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {ButtonComponent} from "../../../../shared/components/button/button.component";


@Component({
  selector: 'app-employee-vacations',
  imports: [
    CommonModule,
    FormsModule,
    ButtonComponent
  ],
  templateUrl: './employee-vacations.component.html',
  styleUrl: './employee-vacations.component.css'
})
export class EmployeeVacationsComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  vacationRequests: VacationRequestResponseRestDto[] = [];
  showForm: boolean = false;
  toDelete: VacationRequestResponseRestDto | null = null;

  constructor(
    private vacationService: VacationEndpointService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadVacationRequests();
  }

  submitVacationRequest(): void {
    if (!this.startDate || !this.endDate) {
      this.toastr.warning('Please provide both start and end dates and make sure the days exist.');
      return;
    }

    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    const maxDate = new Date('9999-12-31T23:59:59Z');

    if (start > maxDate || end > maxDate) {
      this.toastr.warning('Dates cannot be later than 31.12.9999.');
      return;
    }

    const request: VacationRequestRestDto = {
      startDate: this.startDate,
      endDate: this.endDate
    };

    this.vacationService.createVacationRequest(request).subscribe({
      next: () => {
        this.toastr.success('Vacation request submitted successfully.');
        this.startDate = '';
        this.endDate = '';
        this.loadVacationRequests();
      }
    });
  }

  loadVacationRequests(): void {
    this.vacationService.getOwnVacationRequests().subscribe({
      next: (data) => {
        console.log('Received vacation requests:', data);
        this.vacationRequests = data;
      },
      error: (err) => {
        this.toastr.error('Failed to load vacation requests.');
        console.error(err);
      }
    });
  }

  deleteRequest(): void {
    if (this.toDelete === null) {
      return;
    }

    this.vacationService.deleteVacationRequest(this.toDelete.id!).subscribe({
      next: () => {
        this.toastr.success('Vacation request deleted.');
        this.loadVacationRequests();
        this.showForm = false;
      }
    });



  }

  deleteButton(toDeleteId: VacationRequestResponseRestDto): void {
    this.toDelete = toDeleteId
    this.showForm = true
  }
}
