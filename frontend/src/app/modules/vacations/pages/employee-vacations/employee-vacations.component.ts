import {Component, OnInit} from '@angular/core';
import {
  VacationEndpointService,
  VacationRequestResponseRestDto,
  VacationRequestRestDto
} from "../../../../rest_client";
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-employee-vacations',
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './employee-vacations.component.html',
  styleUrl: './employee-vacations.component.css'
})
export class EmployeeVacationsComponent implements OnInit {
  startDate: string = '';
  endDate: string = '';
  vacationRequests: VacationRequestResponseRestDto[] = [];

  constructor(
    private vacationService: VacationEndpointService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadVacationRequests();
  }

  submitVacationRequest(): void {
    if (!this.startDate || !this.endDate) {
      this.toastr.error('Please select both start and end dates.');
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
}
