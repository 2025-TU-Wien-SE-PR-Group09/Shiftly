import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { SickLeaveCertificateEndpointService, SickLeaveCertificateRestDto } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-sick-notes',
  imports: [CommonModule, DatePipe, NgIf, ButtonComponent, AngularSvgIconModule],
  templateUrl: './admin-sick-notes.component.html',
  styleUrl: './admin-sick-notes.component.css',
})
export class AdminSickNotesComponent implements OnInit {
  sickNotes: SickLeaveCertificateRestDto[] = [];
  confirmingDeleteNote: SickLeaveCertificateRestDto | null = null;
  confirmingDeleteId: number | null = null;

  constructor(
    private sickLeaveService: SickLeaveCertificateEndpointService,
    private toastr: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAllCertificates();
  }

  loadAllCertificates(): void {
    this.sickLeaveService.getAllCertificates().subscribe({
      next: (data) => (this.sickNotes = data),
      error: () => this.toastr.error('Could not load certificates.'),
    });
  }

  downloadFile(note: SickLeaveCertificateRestDto): void {
    (
      this.sickLeaveService.download(note.id!, 'body', false, {
        httpHeaderAccept: 'application/octet-stream',
      }) as unknown as Observable<Blob>
    ).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = note.fileName ?? 'sick_note';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.toastr.error('Download failed.'),
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
