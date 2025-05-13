import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervisorDashboardComponent } from './supervisor-dashboard.component';

describe('SupervisorDashboardComponent', () => {
  let component: SupervisorDashboardComponent;
  let fixture: ComponentFixture<SupervisorDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupervisorDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupervisorDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
