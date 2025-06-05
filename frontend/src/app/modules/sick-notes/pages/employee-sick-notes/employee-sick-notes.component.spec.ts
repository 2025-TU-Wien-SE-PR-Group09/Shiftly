import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeSickNotesComponent } from './employee-sick-notes.component';

describe('EmployeeSickNotesComponent', () => {
  let component: EmployeeSickNotesComponent;
  let fixture: ComponentFixture<EmployeeSickNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeSickNotesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeSickNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
