import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-vacations',
  templateUrl: './departments.component.html',
  imports: [RouterOutlet],
})
export class DepartmentsComponent implements OnInit {
  ngOnInit(): void {}
}
