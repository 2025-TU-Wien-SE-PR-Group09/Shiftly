import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervisorVacationsComponent } from './supervisor-vacations.component';

describe('SupervisorVacationsComponent', () => {
  let component: SupervisorVacationsComponent;
  let fixture: ComponentFixture<SupervisorVacationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupervisorVacationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupervisorVacationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
