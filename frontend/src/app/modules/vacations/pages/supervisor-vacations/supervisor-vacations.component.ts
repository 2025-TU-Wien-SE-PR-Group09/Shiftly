import { Component, OnInit } from '@angular/core';
import { VacationEndpointService, VacationRequestResponseRestDto } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { DatePipe } from "@angular/common";
import { ButtonComponent } from "../../../../shared/components/button/button.component";
import { CommonModule } from '@angular/common';
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-supervisor-vacations',
  templateUrl: './supervisor-vacations.component.html',
  imports: [
    CommonModule,
    DatePipe,
    ButtonComponent,
    FormsModule
  ],
  styleUrl: './supervisor-vacations.component.css'
})
export class SupervisorVacationsComponent implements OnInit {

  vacationRequests: VacationRequestResponseRestDto[] = [];
  finishedRequests: VacationRequestResponseRestDto[] = [];

  searchEmailPending: string = '';
  searchEmailFinished: string = '';

  constructor(
    private vacationService: VacationEndpointService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadPendingRequests();
    this.loadFinishedRequests();
  }

  loadPendingRequests(): void {
    this.vacationService.getAllPendingRequests().subscribe({
      next: data => this.vacationRequests = data,
      error: () => this.toastr.error('Failed to load pending vacation requests')
    });
  }

  loadFinishedRequests(): void {
    this.vacationService.getRejectedRequests().subscribe({
      next: rejected => {
        this.vacationService.getApprovedRequests().subscribe({
          next: approved => {
            this.finishedRequests = [...approved, ...rejected];
          },
          error: () => this.toastr.error('Failed to load approved requests')
        });
      },
      error: () => this.toastr.error('Failed to load rejected requests')
    });
  }

  updateStatus(id: number, status: 'APPROVED' | 'REJECTED'): void {
    this.vacationService.updateVacationRequestStatus(id, status as VacationRequestResponseRestDto.StatusEnum).subscribe({
      next: () => {
        this.toastr.success(`Request ${status.toLowerCase()} successfully`);
        this.loadPendingRequests();
        this.loadFinishedRequests();
      },
      error: () => this.toastr.error(`Could not ${status.toLowerCase()} request`)
    });
  }

  get filteredPendingRequests(): VacationRequestResponseRestDto[] {
    return this.vacationRequests.filter(r =>
      !this.searchEmailPending || r.employeeEmail?.toLowerCase().includes(this.searchEmailPending.toLowerCase())
    );
  }

  get filteredFinishedRequests(): VacationRequestResponseRestDto[] {
    return this.finishedRequests.filter(r =>
      !this.searchEmailFinished || r.employeeEmail?.toLowerCase().includes(this.searchEmailFinished.toLowerCase())
    );
  }
}
