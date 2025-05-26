import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeVacationsComponent } from './employee-vacations.component';

describe('WorkerVacationsComponent', () => {
  let component: EmployeeVacationsComponent;
  let fixture: ComponentFixture<EmployeeVacationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeVacationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeVacationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
