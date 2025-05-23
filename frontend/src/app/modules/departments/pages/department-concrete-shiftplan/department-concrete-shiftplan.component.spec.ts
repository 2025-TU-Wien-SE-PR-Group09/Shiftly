import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentConcreteShiftplanComponent } from './department-concrete-shiftplan.component';

describe('DepartmentConcreteShiftplanComponent', () => {
  let component: DepartmentConcreteShiftplanComponent;
  let fixture: ComponentFixture<DepartmentConcreteShiftplanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentConcreteShiftplanComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepartmentConcreteShiftplanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
