import { Component, OnInit } from '@angular/core';
import {
  AdminEndpointService,
  DepartmentDetailRestResponseDto,
  DepartmentService,
  DepartmentShiftplanCalendarResponse,
} from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { CalendarEvent, CalendarModule, CalendarView } from 'angular-calendar';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { FormsModule } from '@angular/forms';
import { HttpContext } from '@angular/common/http';
import { SKIP_EXCEPTION_INTERCEPTOR } from '../../../../core/interceptor/skip-exception-interceptor';

@Component({
  selector: 'app-admin-home',
  imports: [CommonModule, CalendarModule, ButtonComponent, FormsModule],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css',

})

export class AdminHomeComponent implements OnInit {
  constructor(
    private _adminService: AdminEndpointService,
    private _departmentService: DepartmentService,
    private readonly _toastr: ToastrService,
  ) { }

  protected departments: DepartmentDetailRestResponseDto[] = [];
  protected selectedDepartment: DepartmentDetailRestResponseDto | undefined;
  protected shiftPlan: DepartmentShiftplanCalendarResponse | undefined;
  code: string = 'test';

  view: CalendarView = CalendarView.Month;
  CalendarView = CalendarView; // Für Template-Zugriff
  viewDate: Date = new Date();

  events: CalendarEvent[] = [
    {
      start: new Date(2025, 4, 21, 22, 0, 0),
      end: new Date(2025, 4, 22, 6, 0, 0),
      title: 'Nachschicht Gruppe_0',
      color: { primary: '#cc99ff', secondary: '#ccccff' },
    },
    {
      start: new Date(2025, 4, 22, 6, 0, 0),
      end: new Date(2025, 4, 22, 14, 0, 0),
      title: 'Frühschicht Gruppe_1',
      color: { primary: '#1e90ff', secondary: '#D1E8FF' },
    },
    {
      start: new Date(2025, 4, 22, 14, 0, 0),
      end: new Date(2025, 4, 22, 22, 0, 0),
      title: 'Spätschicht Gruppe_2',
      color: { primary: '#e3bc08', secondary: '#FDF1BA' },
    },
    {
      start: new Date(2025, 4, 22, 22, 0, 0),
      end: new Date(2025, 4, 23, 6, 0, 0),
      title: 'Nachtschicht Gruppe_3',
      color: { primary: '#cc99ff', secondary: '#ccccff' },
    },
  ];

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
    this.loadDepartments();
    this.loadAuthCode();
    if (this.selectedDepartment) {
      this.loadScheduledShifts();
    }
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
          workers: shift.workers,
          manpower: shift.manpower
        }
      });
    }

  }

  loadDepartments() {
    this._departmentService.getAllDepartments().subscribe({
      next: (data) => {
        this.departments = data;

        if (this.departments.length > 0) {
          this.selectedDepartment = this.departments[0];
          this.loadScheduledShifts(true);
        }
      },
    });
  }

  loadAuthCode() {
    this._adminService.authCode().subscribe({
      next: (data) => {
        this.code = data.code!;
      },
    });
  }

  loadScheduledShifts(skipException: boolean = false) {
    this.shiftPlan = undefined;

    if (this.selectedDepartment?.name) {
      let req = this._departmentService.getConcreteShiftplans(this.selectedDepartment.name);
      if (skipException) {
        req =  this._departmentService.getConcreteShiftplans(this.selectedDepartment.name, 'body', false, {
          context: new HttpContext().set(SKIP_EXCEPTION_INTERCEPTOR, true)
        })
      }
      req.subscribe({
        next: (data) => {
          if (data.shifts) {
            this.shiftPlan = data;
            this.calculateEvents();
          }
        },
      })
    }
  }

  onDepartmentChanged() {
    this.loadScheduledShifts();
  }
}
