import { Component , OnInit} from '@angular/core';
import {
  AdminEndpointService,
  DepartmentDetailRestDto,
  DepartmentService,
  PlanBlueprintResponse, ScheduledShiftDetailDto, ScheduledShiftResponseDto, ShiftDayDetailDto,
} from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { CalendarEvent, CalendarModule, CalendarView } from 'angular-calendar';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { FormsModule } from '@angular/forms';

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
  ) {}

  protected departments: DepartmentDetailRestDto[] = [];
  protected selectedDepartment: DepartmentDetailRestDto | undefined;
  protected scheduledShift: ScheduledShiftDetailDto[] = [];
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
    if(this.scheduledShift) {
      this.calculateEvents();
    }
  }

  calculateEvents() {
    for (const shift of this.scheduledShift) {
      if (shift.days) {
        for (const day of shift.days) {
          const weekDay = day.day;
          //todo create new event to push on this.events
        }
      }
    }
  }

  addEvent(title: string, startDate: Date, endDate: Date): void {
    this.events = [
      ...this.events,
      {
        title: title,
        start: startDate,
        end: endDate,
        color: { primary: '#cc99ff', secondary: '#ccccff' },
        draggable: true,
        resizable: {
          beforeStart: true,
          afterEnd: true,
        },
      },
    ];
  }

  loadDepartments() {
    this._departmentService.getAllDepartments().subscribe({
      next: (data) => {
        this.departments = data;
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

  loadScheduledShifts() {
    if (this.selectedDepartment?.id) {
      this._departmentService.getDetailedConcretePlan(this.selectedDepartment.id).subscribe({
        next: (data) => {
          this.scheduledShift = data;
        },
      })
    }
  }
}
