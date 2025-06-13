import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PlanBlueprintResponse } from 'src/app/rest_client';
import { AutocompleteComponent } from 'src/app/shared/components/autocomplete/autocomplete.component';
import { DepartmentService } from 'src/app/rest_client';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '../../../../../shared/components/button/button.component';

@Component({
  selector: 'app-create-concrete-plan',
  imports: [FormsModule, CommonModule, AutocompleteComponent, ButtonComponent],
  templateUrl: './create-concrete-plan.component.html',
  styleUrl: './create-concrete-plan.component.css',
})
export class CreateConcretePlanComponent {
  @Input() plans!: PlanBlueprintResponse[];
  shiftPlanDescriptions!: string[];
  selectedMonth: string | null = null;
  selectedPlan: PlanBlueprintResponse | null = null;
  showForm: boolean = true;
  @Output() onCancel = new EventEmitter();

  constructor(
    private departmentService: DepartmentService,
    private route: ActivatedRoute,
    private toastrService: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.shiftPlanDescriptions = this.plans.map((p) => p.description!);
  }

  onShiftplanSelected(event: string): void {
    this.selectedPlan = this.plans.find((p) => p.description === event)!;
    console.log(this.selectedPlan);
  }

  generatePlan(): void {
    if (!this.selectedMonth || !this.selectedPlan) return;

    const dto = { startDate: this.selectedMonth };

    this.departmentService.generateConcretePlan(this.selectedPlan.id!, dto).subscribe({
      next: (data) => {
        this.toastrService.success('Plan generated successfully!');
        this.onCancel.emit();
        },
    });
  }

  getEffectiveStartDate(selectedMonth: string): string {
    const [year, month] = selectedMonth.split('-').map(Number); // month is 1-based
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    const selected = new Date(year, month - 1); // JS month is 0-based

    if (today.getFullYear() === selected.getFullYear() && today.getMonth() === selected.getMonth()) {
      console.log('dieses monat');
      const t = this.toLocalDateString(today);
      console.log(t);
      return t; // heute
    } else {
      console.log('Nicht dieses monat');
      const firstOfMonth = new Date(year, month - 1, 1);
      const stringDate = this.toLocalDateString(firstOfMonth);
      console.log(stringDate);
      return stringDate;
    }
  }

  toLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  finalizePlan(): void {}
}
