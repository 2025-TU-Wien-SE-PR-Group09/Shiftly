import { Component , OnInit} from '@angular/core';
import { AdminEndpointService } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { CalendarEvent, CalendarModule, CalendarView } from 'angular-calendar';
import { ButtonComponent } from '../../../../shared/components/button/button.component';

@Component({
  selector: 'app-admin-home',
  imports: [CommonModule, CalendarModule, ButtonComponent],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css',
})
export class AdminHomeComponent implements OnInit {
  constructor(private _adminService: AdminEndpointService, private readonly _toastr: ToastrService) {}

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
    this._adminService.authCode().subscribe({
      next: (data) => {
        this.code = data.code!;
      }
    })
  }
}
