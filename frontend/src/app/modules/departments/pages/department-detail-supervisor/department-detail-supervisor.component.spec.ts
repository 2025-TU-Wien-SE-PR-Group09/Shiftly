import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentDetailSupervisorComponent } from './department-detail-supervisor.component';

describe('DepartmentDetailComponent', () => {
  let component: DepartmentDetailSupervisorComponent;
  let fixture: ComponentFixture<DepartmentDetailSupervisorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentDetailSupervisorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepartmentDetailSupervisorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
