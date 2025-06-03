import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule, DatePipe, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SickLeaveCertificateRestDto, SickLeaveCertificateEndpointService } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-employee-sick-notes',
  imports: [DatePipe, FormsModule, CommonModule, NgIf, ButtonComponent],
  providers: [DatePipe],
  templateUrl: './employee-sick-notes.component.html',
  styleUrl: './employee-sick-notes.component.css',
})
export class EmployeeSickNotesComponent implements OnInit {
  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  sickNotes: SickLeaveCertificateRestDto[] = [];
  selectedFile: File | null = null;
  loading = false;
  uploadSuccess = false;
  startDate: string = '';
  endDate: string = '';
  confirmingDeleteId: number | null = null;

  constructor(
    private sickLeaveService: SickLeaveCertificateEndpointService,
    private toastr: ToastrService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadSickNotes();
  }

  loadSickNotes(): void {
    this.sickLeaveService.getMyCertificates().subscribe({
      next: (data) => {
        this.sickNotes = data;
      },
      error: () => {
        this.showError('Could not load sick leave certificates.');
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      this.toastr.warning('Please select a file before uploading.');
      return;
    }

    if (!this.startDate || !this.endDate) {
      this.toastr.warning('Please provide both start and end dates.');
      return;
    }
    const start = new Date(this.startDate);
    const end = new Date(this.endDate);
    if (start > end) {
      this.toastr.error('Start date cannot be after end date.');
      return;
    }

    this.loading = true;
    this.uploadSuccess = false;

    this.sickLeaveService.upload(this.startDate, this.endDate, this.selectedFile!).subscribe({
      next: (newCert) => {
        this.toastr.success('File uploaded successfully.');
        this.selectedFile = null;
        this.startDate = '';
        this.endDate = '';
        this.uploadSuccess = true;
        this.loadSickNotes();

        setTimeout(() => {
          this.uploadSuccess = false;
        }, 5000);
      },
      error: (err) => {
        const msg = err?.error?.errors?.[0] ?? 'Upload failed. Please try again.';
        this.showError(msg);
      },
      complete: () => {
        this.loading = false;
      },
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
      error: () => {
        this.showError('Failed to download the file.');
      },
    });
  }

  cancelUpload(): void {
    this.selectedFile = null;

    if (this.fileInputRef) {
      this.fileInputRef.nativeElement.value = '';
    }

    this.toastr.info('File selection cleared.');
  }

  get selectedFileName(): string {
    return this.selectedFile?.name ?? 'No file selected';
  }

  private showError(message: string): void {
    this.toastr.error(message, '', {
      timeOut: 8000,
      progressBar: true,
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
