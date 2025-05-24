import { NgModule, OnInit } from '@angular/core';

import { HomeRoutingModule } from './home-routing.module';
import { FormBuilder } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@NgModule({
  imports: [HomeRoutingModule],
})
export class HomeModule {

}
