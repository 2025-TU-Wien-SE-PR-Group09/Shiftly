import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddShiftToPlanBlueprintDto, DepartmentService, PlanBlueprintResponse } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ButtonComponent } from '../../../../../shared/components/button/button.component';
@Component({
  selector: 'app-editor',
  imports: [CommonModule, ReactiveFormsModule, ButtonComponent],
  templateUrl: './editor.component.html',
})
export class EditorComponent {
  @Input() departmentName!: string;
  @Input() planId!: number;
  @Output() shiftAdded = new EventEmitter<PlanBlueprintResponse>();
  blueprintForm: FormGroup;
  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  distinctColors = [
    'rgba(230, 25, 75, 0.6)', // Red
    'rgba(60, 180, 75, 0.6)', // Green
    'rgba(255, 225, 25, 0.6)', // Yellow
    'rgba(0, 130, 200, 0.6)', // Blue
    'rgba(245, 130, 48, 0.6)', // Orange
    'rgba(145, 30, 180, 0.6)', // Purple
    'rgba(70, 240, 240, 0.6)', // Cyan
    'rgba(240, 50, 230, 0.6)', // Magenta
    'rgba(210, 245, 60, 0.6)', // Lime
    'rgba(250, 190, 190, 0.6)', // Light Pink
    'rgba(0, 128, 128, 0.6)', // Teal
    'rgba(230, 190, 255, 0.6)', // Lavender
    'rgba(170, 110, 40, 0.6)', // Brown
    'rgba(128, 0, 0, 0.6)', // Maroon
    'rgba(128, 128, 0, 0.6)', // Olive
  ];
  @Input() amountShiftWeeks: number = 1;

  constructor(private fb: FormBuilder, private departmentService: DepartmentService, private toastr: ToastrService) {
    this.blueprintForm = this.fb.group({
      shifts: this.fb.array([]),
    });
  }

  get shifts(): FormArray {
    return this.blueprintForm.get('shifts') as FormArray;
  }

  minLengthFormArray(min: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (control instanceof FormArray && control.length < min) {
        return { minLengthArray: { requiredLength: min, actualLength: control.length } };
      }
      return null;
    };
  }

  addShift() {
    const newShift = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(255)]],

      manPower: [1, [Validators.required, Validators.min(1)]],
      shiftWeeks: this.fb.array([], this.minLengthFormArray(1)),
    });

    this.shifts.push(newShift);

    console.log(this.amountShiftWeeks);

    for (let i = 0; i < this.amountShiftWeeks; i++) {
      this.addWeek(this.shifts.length - 1);
    }
  }

  removeShift(i: number) {
    this.shifts.removeAt(i);
  }

  getWeeks(shiftIndex: number): FormArray {
    return this.shifts.at(shiftIndex).get('shiftWeeks') as FormArray;
  }

  addWeek(shiftIndex: number) {
    this.getWeeks(shiftIndex).push(
      this.fb.group({
        shiftDays: this.fb.array([], [this.minLengthFormArray(1), this.exactWeeklyHoursValidator(40)]),
      }),
    );
  }

  hasFixedWeekCount(): boolean {
    return this.amountShiftWeeks !== 1;
  }

  removeWeek(shiftIndex: number, weekIndex: number) {
    this.getWeeks(shiftIndex).removeAt(weekIndex);
  }

  getDays(shiftIndex: number, weekIndex: number): FormArray {
    return this.getWeeks(shiftIndex).at(weekIndex).get('shiftDays') as FormArray;
  }

  addDay(shiftIndex: number, weekIndex: number, weekday: string) {
    this.getDays(shiftIndex, weekIndex).push(
      this.fb.group({
        day: [weekday, Validators.required],
        startTime: ['', Validators.required],
        endTime: ['', Validators.required],
      }),
    );
  }

  removeDay(shiftIndex: number, weekIndex: number, dayIndex: number) {
    this.getDays(shiftIndex, weekIndex).removeAt(dayIndex);
  }

  getDayForWeekForm(shiftIndex: number, weekIndex: number, weekday: string): FormGroup | null {
    const days = this.getDays(shiftIndex, weekIndex);
    return (days.controls.find((d) => d.value.day === weekday) as FormGroup) ?? null;
  }

  getDayIndex(shiftIndex: number, weekIndex: number, weekday: string): number {
    const days = this.getDays(shiftIndex, weekIndex);
    return days.controls.findIndex((d) => d.value.day === weekday);
  }

  trackByIndex(index: number): number {
    return index;
  }

  resetFormCompletely() {
    this.shifts.clear();

    this.blueprintForm.reset();
  }

  submit() {
    if (this.blueprintForm.invalid) {
      this.blueprintForm.markAllAsTouched();
      return;
    }

    const dto = this.blueprintForm.value;
    const body: AddShiftToPlanBlueprintDto = { ...dto, planId: this.planId };
    console.log(body);

    this.departmentService.addShiftToPlanBlueprint(this.departmentName!, body).subscribe({
      next: (data: PlanBlueprintResponse) => {
        this.toastr.success('Plan gespeichert!');
        this.shiftAdded.emit(data);
        this.resetFormCompletely();
      },
    });
  }

  exactWeeklyHoursValidator(expectedHours: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!(control instanceof FormArray)) return null;

      let totalMinutes = 0;

      for (const dayCtrl of control.controls) {
        const start = dayCtrl.get('startTime')?.value;
        const end = dayCtrl.get('endTime')?.value;

        if (start && end) {
          totalMinutes += this.calculateDurationMinutes(start, end);
        }
      }

      const totalHours = totalMinutes / 60;
      if (totalHours !== expectedHours) {
        return { exactWeeklyHours: totalHours };
      }

      return null;
    };
  }

  private calculateDurationMinutes(startTime: string, endTime: string): number {
    const [sh, sm] = startTime.split(':').map(Number);
    const [eh, em] = endTime.split(':').map(Number);

    const startMinutes = sh * 60 + sm;
    const endMinutes = eh * 60 + em;

    if (endMinutes <= startMinutes) {
      // über Mitternacht
      const startToMidnight = 23 * 60 + 59 - startMinutes + 1; // bis 00:00
      const midnightToEnd = endMinutes; // ab 00:00 bis Ende
      return startToMidnight + midnightToEnd;
    } else {
      return endMinutes - startMinutes;
    }
  }

  copyDayToOthers(shiftIndex: number, weekIndex: number, weekday: string) {
    const otherWeekdays = this.daysOfWeek.filter((d) => d !== weekday);
    const weekDaysData = this.getDays(shiftIndex, weekIndex).value;
    let startTime = '';
    let endTime = '';

    for (let weekDayData of weekDaysData) {
      if (weekDayData.day === weekday) {
        startTime = weekDayData.startTime;
        endTime = weekDayData.endTime;
      }
    }

    if (startTime === '' || endTime === '') {
      this.toastr.error('Cannot copy time to other days because startTime and endTime is empty.', 'Error occurred');
      return;
    }

    for (let otherWeekday of otherWeekdays) {
      const dayIndex = this.getDayIndex(shiftIndex, weekIndex, otherWeekday);
      if (dayIndex >= 0) {
        this.removeDay(shiftIndex, weekIndex, this.getDayIndex(shiftIndex, weekIndex, otherWeekday));
      }

      this.getDays(shiftIndex, weekIndex).push(
        this.fb.group({
          day: [otherWeekday, Validators.required],
          startTime: [startTime, Validators.required],
          endTime: [endTime, Validators.required],
        }),
      );
    }
  }
}
