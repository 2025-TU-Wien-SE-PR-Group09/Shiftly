import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentShiftplanBlueprintComponent } from './department-shiftplan-blueprint.component';

describe('DepartmentShiftplanComponent', () => {
  let component: DepartmentShiftplanBlueprintComponent;
  let fixture: ComponentFixture<DepartmentShiftplanBlueprintComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentShiftplanBlueprintComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DepartmentShiftplanBlueprintComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
