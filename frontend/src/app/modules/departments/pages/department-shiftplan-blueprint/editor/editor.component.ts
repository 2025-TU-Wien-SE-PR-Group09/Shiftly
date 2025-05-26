import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddShiftToPlanBlueprintDto, DepartmentService, PlanBlueprintResponse } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
@Component({
  selector: 'app-editor',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editor.component.html',
})
export class EditorComponent {
  @Input() departmentName!: string;
  @Input() planId!: number;
  @Output() shiftAdded = new EventEmitter<PlanBlueprintResponse>();
  blueprintForm: FormGroup;
  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

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
    this.shifts.push(
      this.fb.group({
        description: ['', Validators.required],
        manPower: [1, [Validators.required, Validators.min(1)]],
        shiftWeeks: this.fb.array([], this.minLengthFormArray(1)),
      }),
    );
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
        shiftDays: this.fb.array([], this.minLengthFormArray(1)),
      }),
    );
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
}
