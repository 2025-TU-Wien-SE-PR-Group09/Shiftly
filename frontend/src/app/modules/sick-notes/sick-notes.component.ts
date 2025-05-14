import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-sick-notes',
  templateUrl: './sick-notes.component.html',
  imports: [RouterOutlet],
})
export class SickNotesComponent implements OnInit {
  constructor(private _authService: AuthService) {}

  ngOnInit(): void {}
}
