import { Component, OnInit } from '@angular/core';
import {
  DepartmentDetailRestResponseDto,
  DepartmentService,
  DepartmentShiftplanCalendarResponse, UserEndpointService, UserProfileRestDto, ICalService,
} from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CalendarEvent, CalendarModule, CalendarView } from 'angular-calendar';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpContext, HttpClient } from '@angular/common/http';
import { SKIP_EXCEPTION_INTERCEPTOR } from '../../../../core/interceptor/skip-exception-interceptor';
import { environment } from '../../../../../environments/environment';



@Component({
  selector: 'app-employee-home',
  imports: [CommonModule, CalendarModule, ButtonComponent, FormsModule],

  templateUrl: './employee-home.component.html',
  styleUrl: './employee-home.component.css',
})
export class EmployeeHomeComponent implements OnInit{
  constructor(
    private _userService: UserEndpointService,
    private _departmentService: DepartmentService,
    private readonly _toastr: ToastrService,
    private _icalService: ICalService,
    private _httpClient: HttpClient,
  ) {}

  protected selectedDepartment: DepartmentDetailRestResponseDto | undefined;
  protected shiftPlan: DepartmentShiftplanCalendarResponse | undefined;
  code: string = 'test';
  protected currentUser: UserProfileRestDto | undefined;
  protected subscriptionUrl: string = '';
  protected showSubscriptionUrl: boolean = false;

  view: CalendarView = CalendarView.Week;
  CalendarView = CalendarView; // Für Template-Zugriff
  viewDate: Date = new Date();
  events: CalendarEvent[] = [];

  selectedEvent: CalendarEvent | null = null;

  handleEventClick(event: { event: CalendarEvent }): void {
    if (this.selectedEvent === event.event) {
      this.selectedEvent = null;
    } else {
      this.selectedEvent = event.event;
    }
  }

  setView(view: CalendarView) {
    this.view = view;
  }

  activeDayIsOpen: boolean = true;
  closeOpenMonthViewDay() {
    this.activeDayIsOpen = false;
  }

  ngOnInit(): void {
    this.load();
  }

  //fetching department and loading shifts
  load() {
    this._userService.getCurrentUserProfile().subscribe({
      next: (value) => {
        this.currentUser = value;
        var departmentName = value.department;
        if (departmentName) {
          console.log('department name: ', departmentName);
          this._departmentService.getDepartmentByName(departmentName).subscribe({
            next: (data) => {
              this.selectedDepartment = data;
              this.loadScheduledShifts();
            },
            error: (err) => {
              console.log('error', err);
            },
          });
        }
      },
    });
  }

  private shiftColors = new Map<string, { primary: string; secondary: string }>();
  private colorPalette = [
    { primary: '#1e90ff', secondary: '#D1E8FF' }, // Blau
    { primary: '#ff6b6b', secondary: '#ffd4d4' }, // Rot
    { primary: '#32CD32', secondary: '#d4ffd4' }, // Grün
    { primary: '#ffd700', secondary: '#fff4b3' }, // Gold
    { primary: '#9370db', secondary: '#e6d5ff' }, // Lila
    { primary: '#ff8c00', secondary: '#ffe4b3' }, // Orange
    { primary: '#20b2aa', secondary: '#b3e6e4' }, // Türkis
  ];

  private getColorForShiftType(shiftType: string): { primary: string; secondary: string } {
    if (!this.shiftColors.has(shiftType)) {
      const colorIndex = this.shiftColors.size % this.colorPalette.length;
      this.shiftColors.set(shiftType, this.colorPalette[colorIndex]);
    }
    return this.shiftColors.get(shiftType)!;
  }

  calculateEvents() {
    if (!this.shiftPlan || !this.shiftPlan.shifts) {
      return;
    }

    this.events = [];

    for (const shift of this.shiftPlan.shifts) {
      console.log(shift.workers)
      if (!shift.day?.start || !shift.day?.end) {
        continue;
      }

      const startDate = new Date(shift.day.start);
      const endDate = new Date(shift.day.end);
      const shiftTitle = shift.shiftDescription ?? 'Schicht';

      this.events.push({
        title: shiftTitle,
        start: startDate,
        end: endDate,
        color: this.getColorForShiftType(shiftTitle),
        meta: {
          //todo: workers should become entity "worker" e.g. including the role
          //todo: 'springer' should be displayed visible in the calendar
          workers: shift.workers,
        },
      });
    }
  }

  loadScheduledShifts() {
    console.log('loadschedule');
    if (this.selectedDepartment?.name) {
      console.log('fetching plan');
      this._departmentService.getConcreteShiftplans(this.selectedDepartment.name!, 'body', false, {
        context: new HttpContext().set(SKIP_EXCEPTION_INTERCEPTOR, true)
      }).subscribe({
        next: (data) => {
          if (data.shifts) {
            this.shiftPlan = data;
            this.calculateEvents();
          }
        },
      });
    }
  }

  /**
   * Downloads the employee's shifts as an iCal file
   */
  downloadMyShifts(): void {
    this._icalService.downloadEmployeeShiftsIcal('response').subscribe({
      next: (response) => {
        if (!response.body) {
          this._toastr.error('Empty response received', 'Error');
          return;
        }

        // Convert the response body to a blob
        const blob = new Blob([response.body], { type: 'text/calendar' });

        // Create a URL for the blob
        const url = window.URL.createObjectURL(blob);

        // Create a temporary anchor element
        const a = document.createElement('a');
        a.href = url;
        a.download = 'my_shifts.ics';

        // Append to the document, click it, and remove it
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        this._toastr.success('My shifts downloaded successfully', 'Success');
      },
      error: (err) => {
        console.error('Error downloading my shifts', err);
        this._toastr.error('Failed to download my shifts', 'Error');
      }
    });
  }

  /**
   * Generates and displays the subscription URL for the employee's shifts calendar
   */
  getSubscriptionUrl(): void {
    this._icalService.generateSubscriptionUrl().subscribe({
      next: (response: string) => {
        this.subscriptionUrl = response;
        this.showSubscriptionUrl = true;
        this._toastr.success('Subscription URL generated successfully', 'Success');
      },
      error: (err: any) => {
        console.error('Error generating subscription URL', err);
        this._toastr.error('Failed to generate subscription URL', 'Error');
      }
    });
  }

  /**
   * Regenerates the subscription URL with a new token
   */
  regenerateSubscriptionUrl(): void {
    this._icalService.regenerateSubscriptionUrl().subscribe({
      next: (response: string) => {
        this.subscriptionUrl = response;
        this.showSubscriptionUrl = true;
        this._toastr.success('New subscription URL generated successfully', 'Success');
      },
      error: (err: any) => {
        console.error('Error regenerating subscription URL', err);
        this._toastr.error('Failed to regenerate subscription URL', 'Error');
      }
    });
  }

  /**
   * Copies the subscription URL to the clipboard
   */
  copySubscriptionUrl(): void {
    navigator.clipboard.writeText(this.subscriptionUrl).then(
      () => {
        this._toastr.success('Subscription URL copied to clipboard', 'Success');
      },
      (err) => {
        console.error('Could not copy text: ', err);
        this._toastr.error('Failed to copy subscription URL', 'Error');
      }
    );
  }

  /**
   * Hides the subscription URL
   */
  hideSubscriptionUrl(): void {
    this.showSubscriptionUrl = false;
  }
}
