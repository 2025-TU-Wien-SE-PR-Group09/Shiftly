import { Component , OnInit} from '@angular/core';
import { AdminEndpointService } from '../../../../rest_client';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-home',
  imports: [CommonModule],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css'
})
export class AdminHomeComponent implements OnInit{

  code: string = 'test';

  constructor(private _adminService: AdminEndpointService,
              private readonly _toastr: ToastrService,
              ) {}

  ngOnInit(): void {
    this._adminService.authCode().subscribe({
      next: (data) => {
        this.code = data.code!;
      },
      error: (err) => {
        console.error('Full error:', err); // Log the full error
        console.error('Status:', err.status); // Log the status code
        console.error('Message:', err.message); // Log the error message
        this._toastr.error('Error fetching code');
      }

    })
  }
}
