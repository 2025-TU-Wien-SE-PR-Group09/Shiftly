import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkerVacationsComponent } from './worker-vacations.component';

describe('WorkerVacationsComponent', () => {
  let component: WorkerVacationsComponent;
  let fixture: ComponentFixture<WorkerVacationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkerVacationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WorkerVacationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
