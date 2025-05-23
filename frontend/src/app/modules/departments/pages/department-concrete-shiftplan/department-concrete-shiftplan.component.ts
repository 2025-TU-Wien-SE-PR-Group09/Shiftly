import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentService, ScheduledShiftDetailDto } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
@Component({
  selector: 'app-department-concrete-shiftplan',
  imports: [CommonModule],
  templateUrl: './department-concrete-shiftplan.component.html',
  styleUrl: './department-concrete-shiftplan.component.css',
})
export class DepartmentConcreteShiftplanComponent {
  @Input() departmentId: number | null = null;
  shiftDetails: any[] = [];
  @Output() hasConcretePlan = new EventEmitter<boolean>();

  isLoading = true;
  error: string | null = null;
  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  constructor(
    private departmentService: DepartmentService,
    private route: ActivatedRoute,
    private toastrService: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.departmentId) {
      return;
    }
    type Weekday = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

    interface ShiftDay {
      day: Weekday;
      startTime: string;
      duration: string;
      assignedUsers: any[];
    }

    this.departmentService.getDetailedConcretePlan(this.departmentId).subscribe({
      next: (data: ScheduledShiftDetailDto[]) => {
        this.shiftDetails = data
          .reduce((acc: any[], entry: any) => {
            const existingWeek = acc.find((w) => w.weekStart === entry.weekStart);

            if (existingWeek) {
              existingWeek.shifts.push({
                description: entry.shiftDescription,
                days: entry.days,
              });
            } else {
              acc.push({
                weekStart: entry.weekStart,
                shifts: [
                  {
                    description: entry.shiftDescription,
                    days: entry.days,
                  },
                ],
              });
            }

            return acc;
          }, [])
          .sort((a, b) => new Date(a.weekStart).getTime() - new Date(b.weekStart).getTime());

        this.isLoading = false;
        if (this.shiftDetails.length !== 0) {
          this.hasConcretePlan.emit(true);
        }
      },
      error: (err) => {
        this.toastrService.error('Fehler beim Laden des konkreten Plans');
        this.isLoading = false;
        this.hasConcretePlan.emit(false);
      },
    });
  }
  readonly weekdays = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  getDayForWeekday(days: any[], weekday: string) {
    return days.find((d) => d.day === weekday);
  }

  formatDuration(isoDuration: string): string {
    const match = isoDuration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
    if (!match) return isoDuration;

    const hours = match[1] ? parseInt(match[1], 10) : 0;
    const minutes = match[2] ? parseInt(match[2], 10) : 0;

    const parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}min`);

    return parts.join(' ') || '0min';
  }

  formatWeekDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('de-AT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  getHourMinute(isoTime: string): string {
    const [hour, minute] = isoTime.split(':');
    return `${hour}:${minute}`;
  }
}
