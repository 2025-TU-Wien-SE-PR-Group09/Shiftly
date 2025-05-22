import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DepartmentService } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
@Component({
  selector: 'app-editor',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editor.component.html',
})
export class EditorComponent {
  @Input() departmentName: string = '';
  @Output() blueprintSaved = new EventEmitter<void>();
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

  addShift() {
    this.shifts.push(
      this.fb.group({
        description: ['', Validators.required],
        manPower: [1, [Validators.required, Validators.min(1)]],
        shiftWeeks: this.fb.array([]),
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
        shiftDays: this.fb.array([]),
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

    this.departmentService.createShiftplanBlueprint(dto, this.departmentName).subscribe({
      next: () => {
        this.toastr.success('Plan gespeichert!');
        this.blueprintSaved.emit();
        this.resetFormCompletely();
      },
      error: () => this.toastr.error('Fehler beim Speichern des Plans'),
    });
  }
}
