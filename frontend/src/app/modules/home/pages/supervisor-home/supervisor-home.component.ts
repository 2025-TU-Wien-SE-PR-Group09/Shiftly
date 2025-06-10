import { Component, OnInit } from '@angular/core';
import {
  DepartmentDetailRestResponseDto,
  DepartmentService,
  DepartmentShiftplanCalendarResponse, UserEndpointService,
} from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { CalendarEvent, CalendarModule, CalendarView } from 'angular-calendar';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-supervisor-home',
  imports: [CommonModule, CalendarModule, ButtonComponent, FormsModule],
  templateUrl: './supervisor-home.component.html',
  styleUrl: './supervisor-home.component.css'
})
export class SupervisorHomeComponent implements OnInit{

  constructor(
    private _userService: UserEndpointService,
    private _departmentService: DepartmentService,
    private readonly _toastr: ToastrService,
  ) {}

  protected selectedDepartment: DepartmentDetailRestResponseDto | undefined;
  protected shiftPlan: DepartmentShiftplanCalendarResponse | undefined;
  code: string = 'test';

  view: CalendarView = CalendarView.Month;
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
      next: value => {
        var departmentName = value.department;
        if (departmentName) {
          console.log("department name: ", departmentName)
          this._departmentService.getDepartmentByName(departmentName).subscribe({
            next: data => {
              this.selectedDepartment = data;
              this.loadScheduledShifts();
            },
            error: err => {
              console.log("error",err);
            }
          })
        }
      }
    })
  }

  private shiftColors = new Map<string, { primary: string; secondary: string }>();
  private colorPalette = [
    { primary: '#1e90ff', secondary: '#D1E8FF' }, // Blau
    { primary: '#ff6b6b', secondary: '#ffd4d4' }, // Rot
    { primary: '#32CD32', secondary: '#d4ffd4' }, // Grün
    { primary: '#ffd700', secondary: '#fff4b3' }, // Gold
    { primary: '#9370db', secondary: '#e6d5ff' }, // Lila
    { primary: '#ff8c00', secondary: '#ffe4b3' }, // Orange
    { primary: '#20b2aa', secondary: '#b3e6e4' }  // Türkis
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
          workers: shift.workers
        }
      });
    }

  }

  loadScheduledShifts() {
    console.log("loadschedule");
    if (this.selectedDepartment?.id) {
      console.log("fetching plan");
      this._departmentService.getConcreteShiftplan(this.selectedDepartment.id).subscribe({
        next: (data) => {
          if (data.shifts) {
            this.shiftPlan = data;
            this.calculateEvents();
          }
        },
      })
    }
  }
}
