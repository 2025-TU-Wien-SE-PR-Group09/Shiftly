import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlanBlueprintComponent } from './plan-blueprint.component';

describe('PlanBlueprintComponent', () => {
  let component: PlanBlueprintComponent;
  let fixture: ComponentFixture<PlanBlueprintComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlanBlueprintComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlanBlueprintComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
