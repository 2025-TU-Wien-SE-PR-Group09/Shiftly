import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateConcretePlanComponent } from './create-concrete-plan.component';

describe('CreateConcretePlanComponent', () => {
  let component: CreateConcretePlanComponent;
  let fixture: ComponentFixture<CreateConcretePlanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateConcretePlanComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateConcretePlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
