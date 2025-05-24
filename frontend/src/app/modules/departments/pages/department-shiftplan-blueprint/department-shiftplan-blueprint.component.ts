import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentDetailRestDto, DepartmentService } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
import { DepartmentConcreteShiftplanComponent } from '../department-concrete-shiftplan/department-concrete-shiftplan.component';
import { EditorComponent } from './editor/editor.component';

@Component({
  selector: 'app-department-shiftplan',
  imports: [CommonModule, DepartmentConcreteShiftplanComponent, EditorComponent],
  templateUrl: './department-shiftplan-blueprint.component.html',
  styleUrl: './department-shiftplan-blueprint.component.css',
})
export class DepartmentShiftplanBlueprintComponent {
  shiftplans: any[] = [];
  isLoading = true;
  error: string | null = null;
  departmentName: string | null = null;
  departmentId: number | null = null;
  planFinalized: boolean = false;

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

  loadBlueprints(): void {
    this.departmentName = this.route.snapshot.paramMap.get('name');

    if (!this.departmentName) {
      this.error = 'Kein Department angegeben';
      this.isLoading = false;
      return;
    }

    type Department = {
      id: number;
      name: string;
    };

    this.departmentService.getAllDepartments().subscribe({
      next: (data: DepartmentDetailRestDto[]) => (this.departmentId = data.find((x) => x.name === this.departmentName)!.id!),
      error: (err) => {
        this.router.navigate(['/departments']);
      },
    });

    this.departmentService.getShiftplanBlueprints(this.departmentName).subscribe({
      next: (data) => {
        this.shiftplans = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        console.log(err);

        if (err.status == 404) {
          this.router.navigate(['/departments']);
          return;
        }

      },
    });
  }

  onFinalized(event: boolean) {
    console.log(event);
    this.planFinalized = event;
  }

  finalizePlan(): void {
    this.departmentService.generateConcretePlan(this.departmentId!).subscribe({
      next: (data) => {
        this.loadBlueprints();
      },
    });
  }

  ngOnInit(): void {
    this.loadBlueprints();
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
