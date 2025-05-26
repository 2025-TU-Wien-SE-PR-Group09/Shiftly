import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervisorSickNotesComponent } from './supervisor-sick-notes.component';

describe('SupervisorSickNotesComponent', () => {
  let component: SupervisorSickNotesComponent;
  let fixture: ComponentFixture<SupervisorSickNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupervisorSickNotesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupervisorSickNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
