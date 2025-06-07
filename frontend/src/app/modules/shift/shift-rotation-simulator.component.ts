import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { parseISO, format } from 'date-fns';
import { DepartmentShiftplanCalendarResponse, ScheduledShift } from 'src/app/rest_client';
// sortByName.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
@Pipe({ name: 'sortByName', standalone: true })
export class SortByNamePipe implements PipeTransform {
  transform(value: string[]): string[] {
    return value.slice().sort();
  }
}

interface ShiftDay {
  start: string;
  end: string;
}

interface ShiftEntry {
  shiftDescription: string;
  day: ShiftDay;
  workers: string[];
}

interface EnrichedShift extends ShiftEntry {
  date: string;
  shortDate: string;
}

interface ShiftWeek {
  week: string;
  shifts: EnrichedShift[];
}

@Component({
  selector: 'app-shift-rotation-simulator',
  templateUrl: './shift-rotation-simulator.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule, SortByNamePipe]
})
export class ShiftRotationSimulatorComponent implements OnChanges {
  @Input() shiftPlan: DepartmentShiftplanCalendarResponse | undefined;

  weekIndex = 0;
  weeks: ShiftWeek[] = [];
  weekdays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
  workerColorMap = new Map<string, string>();
  availableColors = ['#EF4444', '#F59E0B', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899', '#14B8A6', '#F97316'];

  ngOnChanges(changes: SimpleChanges): void {
    if (this.shiftPlan?.shifts?.length) {
      this.processShifts(this.shiftPlan.shifts);
    }
  }

  processShifts(shifts: ScheduledShift[]): void {
    const grouped: Record<string, EnrichedShift[]> = {};
    this.workerColorMap.clear();

    for (const shift of shifts) {
      shift.workers?.sort();
      const date = parseISO(shift.day?.start ?? "");
      const week = format(date, "yyyy-'W'II");
      if (!grouped[week]) grouped[week] = [];

      grouped[week].push({
        ...shift as any,
        date: format(date, 'EEEE'),
        shortDate: format(date, 'MM/dd')
      });
    }

    this.weeks = Object.entries(grouped).sort().map(([week, shifts]) => ({ week, shifts }));
    this.weekIndex = 0;

    const firstWeek = this.weeks[0];
    if (firstWeek) {
      const groupSets: string[][] = [];
      for (const shift of firstWeek.shifts) {
        const existing = groupSets.find(set => arraysEqual(set, shift.workers));
        if (!existing) {
          groupSets.push(shift.workers.slice());
        }
      }
      let colorIndex = 0;
      for (const group of groupSets) {
        const color = this.availableColors[colorIndex % this.availableColors.length];
        for (const worker of group) {
          this.workerColorMap.set(worker, color);
        }
        colorIndex++;
      }
    }
  }

  get currentWeek(): ShiftWeek | undefined {
    return this.weeks[this.weekIndex];
  }

  prevWeek() {
    if (this.weekIndex > 0) this.weekIndex--;
  }

  nextWeek() {
    if (this.weekIndex < this.weeks.length - 1) this.weekIndex++;
  }

  getShiftsForDay(day: string): EnrichedShift[] {
    return this.currentWeek?.shifts.filter(s => s.date === day) || [];
  }

  getWorkerColor(worker: string): string {
    return this.workerColorMap.get(worker) || '#000000';
  }
}

function arraysEqual(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}


