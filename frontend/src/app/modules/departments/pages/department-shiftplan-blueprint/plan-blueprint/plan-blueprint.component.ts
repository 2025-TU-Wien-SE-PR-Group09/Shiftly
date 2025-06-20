import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { EditorComponent } from '../editor/editor.component';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentDetailRestResponseDto, PlanBlueprintResponse } from 'src/app/rest_client';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-plan-blueprint',
  imports: [EditorComponent, CommonModule],
  templateUrl: './plan-blueprint.component.html',
  styleUrl: './plan-blueprint.component.css',
})
export class PlanBlueprintComponent implements OnInit {
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
  formatDuration(startDate:string, isoDuration: string): string {
    const match = isoDuration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);

    if (!match) return isoDuration;

    const hours = match[1] ? parseInt(match[1], 10) : 0;
    const minutes = match[2] ? parseInt(match[2], 10) : 0;

    const [hoursSt, minutesSt] = startDate.split(':').map(Number);

    const sum = hoursSt*60*60*1000 + minutesSt*60*1000 + hours*60*60*1000 + minutes*60*1000 - 60*60*1000

    const date = new Date(sum);
    const hoursS = date.getHours().toString().padStart(2, '0');   // 0–23
    const minutesS = date.getMinutes().toString().padStart(2, '0');

    return `${hoursS}:${minutesS}`;
  }

  onFinalized(event: boolean) {
    console.log(event);
    this.planFinalized = event;
  }

  ngOnInit(): void {
  }

  getAmountShiftweeks() {
    return this.plan.shifts && this.plan.shifts.length > 0 && this.plan.shifts[0].shiftWeeks && this.plan.shifts[0].shiftWeeks.length > 0 ? this.plan.shifts[0].shiftWeeks!.length! : 1;
  }
}
