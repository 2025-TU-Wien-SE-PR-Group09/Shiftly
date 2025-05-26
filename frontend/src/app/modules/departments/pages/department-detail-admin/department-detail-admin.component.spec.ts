import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepartmentDetailAdminComponent } from './department-detail-admin.component';

describe('DepartmentDetailAdminComponent', () => {
  let component: DepartmentDetailAdminComponent;
  let fixture: ComponentFixture<DepartmentDetailAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DepartmentDetailAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepartmentDetailAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
