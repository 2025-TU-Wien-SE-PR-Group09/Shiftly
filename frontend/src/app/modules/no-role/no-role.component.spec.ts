import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NoRoleComponent } from './no-role.component';

describe('ErrorComponent', () => {
  let component: NoRoleComponent;
  let fixture: ComponentFixture<NoRoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoRoleComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NoRoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
