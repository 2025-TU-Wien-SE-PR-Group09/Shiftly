import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSickNotesComponent } from './admin-sick-notes.component';

describe('AdminSickNotesComponent', () => {
  let component: AdminSickNotesComponent;
  let fixture: ComponentFixture<AdminSickNotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSickNotesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminSickNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
