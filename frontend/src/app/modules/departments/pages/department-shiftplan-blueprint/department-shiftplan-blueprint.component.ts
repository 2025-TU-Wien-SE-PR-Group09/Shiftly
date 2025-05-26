import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DepartmentDetailRestResponseDto, DepartmentService, PlanBlueprintResponse } from 'src/app/rest_client';
import { ToastrService } from 'ngx-toastr';
import { PlanBlueprintComponent } from './plan-blueprint/plan-blueprint.component';
import { CreateConcretePlanComponent } from './create-concrete-plan/create-concrete-plan.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { ReactiveFormsModule } from '@angular/forms';
@Component({
  selector: 'app-department-shiftplan',
  imports: [CommonModule, PlanBlueprintComponent, CreateConcretePlanComponent, ButtonComponent, ReactiveFormsModule],
  templateUrl: './department-shiftplan-blueprint.component.html',
  styleUrl: './department-shiftplan-blueprint.component.css',
})
export class DepartmentShiftplanBlueprintComponent {
  shiftplans!: PlanBlueprintResponse[];
  department!: DepartmentDetailRestResponseDto;
  isLoading = true;
  error: string | null = null;
  expanded: boolean[] = [];
  showForm: boolean = false;

  constructor(
    private departmentService: DepartmentService,
    private route: ActivatedRoute,
    private toastrService: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.department = this.route.snapshot.data['department'];
    console.log(this.department);
    this.shiftplans = this.route.snapshot.data['blueprints'];
    this.expanded = this.shiftplans.map((s) => false);
    console.log(this.shiftplans);
  }

  loadBlueprints(): void {
    this.departmentService.getShiftplanBlueprints(this.department.name!).subscribe({
      next: (data: PlanBlueprintResponse[]) => (this.shiftplans = data),
      error: (_) => {
        this.toastrService.error('Could not load shiftplan-blueprints!');
      },
    });
  }
}
