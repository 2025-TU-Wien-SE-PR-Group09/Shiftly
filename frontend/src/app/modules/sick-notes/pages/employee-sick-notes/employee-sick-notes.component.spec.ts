import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkerSickNotesComponent } from './employee-sick-notes.component';

describe('WorkerSickNotesComponent', () => {
  let component: WorkerSickNotesComponent;
  let fixture: ComponentFixture<WorkerSickNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkerSickNotesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkerSickNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
