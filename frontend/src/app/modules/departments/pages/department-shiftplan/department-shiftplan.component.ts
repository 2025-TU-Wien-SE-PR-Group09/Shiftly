import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DepartmentService } from 'src/app/rest_client';

@Component({
  selector: 'app-department-shiftplan',
  imports: [CommonModule],
  templateUrl: './department-shiftplan.component.html',
  styleUrl: './department-shiftplan.component.css',
})
export class DepartmentShiftplanComponent {
  shiftplans: any[] = [];
  isLoading = true;
  error: string | null = null;
  departmentName: string | null = null;

  constructor(private departmentService: DepartmentService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.departmentName = this.route.snapshot.paramMap.get('name');

    if (!this.departmentName) {
      console.log(this.departmentName);
      this.error = 'Kein Department angegeben';
      this.isLoading = false;
      return;
    }
    console.log(this.departmentName);

    this.departmentService.getShiftplan(this.departmentName).subscribe({
      next: (data) => {
        this.shiftplans = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Fehler beim Laden des Shiftplans';
        console.error(err);
        this.isLoading = false;
      },
    });
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
}
