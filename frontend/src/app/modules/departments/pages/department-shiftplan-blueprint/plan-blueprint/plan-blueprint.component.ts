import { Component, Input, Output, EventEmitter } from '@angular/core';
import { EditorComponent } from '../editor/editor.component';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentDetailRestResponseDto, PlanBlueprintResponse } from 'src/app/rest_client';
import { CommonModule } from '@angular/common';
import { ButtonComponent } from '../../../../../shared/components/button/button.component';
@Component({
  selector: 'app-plan-blueprint',
  imports: [EditorComponent, CommonModule],
  templateUrl: './plan-blueprint.component.html',
  styleUrl: './plan-blueprint.component.css',
})
export class PlanBlueprintComponent {
  @Input() department!: DepartmentDetailRestResponseDto;
  @Input() plan!: PlanBlueprintResponse;
  @Output() planChanged = new EventEmitter<PlanBlueprintResponse>();
  planFinalized: boolean = false;
  @Input() index: number = 0;
  distinctColors = [
    'rgba(230, 25, 75, 0.6)',   // Red
    'rgba(60, 180, 75, 0.6)',   // Green
    'rgba(255, 225, 25, 0.6)',  // Yellow
    'rgba(0, 130, 200, 0.6)',   // Blue
    'rgba(245, 130, 48, 0.6)',  // Orange
    'rgba(145, 30, 180, 0.6)',  // Purple
    'rgba(70, 240, 240, 0.6)',  // Cyan
    'rgba(240, 50, 230, 0.6)',  // Magenta
    'rgba(210, 245, 60, 0.6)',  // Lime
    'rgba(250, 190, 190, 0.6)', // Light Pink
    'rgba(0, 128, 128, 0.6)',   // Teal
    'rgba(230, 190, 255, 0.6)', // Lavender
    'rgba(170, 110, 40, 0.6)',  // Brown
    'rgba(128, 0, 0, 0.6)',     // Maroon
    'rgba(128, 128, 0, 0.6)'    // Olive
  ];

  constructor(private route: ActivatedRoute, private router: Router) {}

  onEditorChange(newPlan: PlanBlueprintResponse) {
    this.plan = newPlan;
    this.planChanged.emit(newPlan);
  }

  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  getDayForWeekday(days: any[], weekday: string): any | null {
    return days.find((d) => d.day === weekday) ?? null;
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

  onFinalized(event: boolean) {
    console.log(event);
    this.planFinalized = event;
  }
}
