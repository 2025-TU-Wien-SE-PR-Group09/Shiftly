import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SickLeaveCertificateEndpointService, SickLeaveCertificateRestDto, SickLeaveCertificateSupervisorRestDto } from '../../../../rest_client';
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
  sickNotes: SickLeaveCertificateSupervisorRestDto[] = [];
  confirmingDeleteId: number | null = null;

  constructor(
    private sickLeaveService: SickLeaveCertificateEndpointService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.sickLeaveService.getSickLeavesForSupervisor().subscribe({
      next: (data) => {
        this.sickNotes = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden:', err);
        this.toastr.error('Etwas ist schiefgelaufen');
      },
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
