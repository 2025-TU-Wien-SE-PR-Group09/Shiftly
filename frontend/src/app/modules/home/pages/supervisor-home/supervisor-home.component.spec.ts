import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupervisorHomeComponent } from './supervisor-home.component';

describe('SupervisorDashboardComponent', () => {
  let component: SupervisorHomeComponent;
  let fixture: ComponentFixture<SupervisorHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupervisorHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupervisorHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
