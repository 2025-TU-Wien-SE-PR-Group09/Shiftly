import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentService } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
import { DepartmentConcreteShiftplanComponent } from '../department-concrete-shiftplan/department-concrete-shiftplan.component';
@Component({
  selector: 'app-department-shiftplan',
  imports: [CommonModule, DepartmentConcreteShiftplanComponent],
  templateUrl: './department-shiftplan.component.html',
  styleUrl: './department-shiftplan.component.css',
})
export class DepartmentShiftplanComponent {
  shiftplans: any[] = [];
  isLoading = true;
  error: string | null = null;
  departmentName: string | null = null;

  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  getDayForWeekday(days: any[], weekday: string): any | null {
    return days.find((d) => d.day === weekday) ?? null;
  }
  constructor(
    private departmentService: DepartmentService,
    private route: ActivatedRoute,
    private toastrService: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.departmentName = this.route.snapshot.paramMap.get('name');

    if (!this.departmentName) {
      this.error = 'Kein Department angegeben';
      this.toastrService.error(this.error);
      this.isLoading = false;
      return;
    }

    this.departmentService.getShiftplanBlueprints(this.departmentName).subscribe({
      next: (data) => {
        this.shiftplans = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        console.log(err);

        if (err.status == 404) {
          this.toastrService.error('Department nicht gefunden.');
          this.router.navigate(['/departments']);
          return;
        }

        if (err?.error?.validation_errors) {
          err.error.validation_errors.forEach((msg: string) => {
            this.toastrService.error(msg);
          });
          return;
        }

        this.toastrService.error(err.error);
      },
    });
  }

  getHourMinute(time: string): string {
    return time?.slice(0, 5);
  }
  formatDuration(isoDuration: string): string {
    const match = isoDuration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);

    if (!match) return isoDuration;

    const hours = match[1] ? parseInt(match[1], 10) : 0;
    const minutes = match[2] ? parseInt(match[2], 10) : 0;

    const parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);

    return parts.join(' ') || '0min';
  }
}
