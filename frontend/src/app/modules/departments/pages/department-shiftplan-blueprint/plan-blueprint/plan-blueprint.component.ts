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
