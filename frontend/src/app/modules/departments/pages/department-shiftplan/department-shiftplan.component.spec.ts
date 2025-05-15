import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentShiftplanComponent } from './department-shiftplan.component';

describe('DepartmentShiftplanComponent', () => {
  let component: DepartmentShiftplanComponent;
  let fixture: ComponentFixture<DepartmentShiftplanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentShiftplanComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepartmentShiftplanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
