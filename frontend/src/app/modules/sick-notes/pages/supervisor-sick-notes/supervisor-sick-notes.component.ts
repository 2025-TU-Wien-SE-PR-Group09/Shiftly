import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SickLeaveCertificateEndpointService, SickLeaveCertificateRestDto } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-supervisor-sick-notes',
  imports: [FormsModule, CommonModule, NgIf, DatePipe],
  templateUrl: './supervisor-sick-notes.component.html',
  styleUrl: './supervisor-sick-notes.component.css',
  standalone: true,
})
export class SupervisorSickNotesComponent implements OnInit {
  sickNotes: SickLeaveCertificateRestDto[] = [];
  confirmingDeleteId: number | null = null;

  constructor(
    private sickLeaveService: SickLeaveCertificateEndpointService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.sickLeaveService.getSickLeavesForSupervisor().subscribe({
      next: (data: SickLeaveCertificateRestDto[]) => (this.sickNotes = data),
      error: () => this.toastr.error('Could not load certificates.'),
    });
  }

  toggleConfirmDelete(id: number): void {
    this.confirmingDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmingDeleteId = null;
  }

  confirmDelete(id: number): void {
    this.sickLeaveService.deleteSickLeaveCertificate(id).subscribe({
      next: () => {
        this.toastr.success('Certificate deleted');
        this.sickNotes = this.sickNotes.filter((note) => note.id !== id);
        this.confirmingDeleteId = null;
      },
      error: () => this.toastr.error('Failed to delete certificate'),
    });
  }
}
