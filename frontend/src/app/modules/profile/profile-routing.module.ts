import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ProfileComponent } from './profile.component';

import { NewPasswordComponent } from './pages/new-password/new-password.component';
import { YourProfileComponent } from './pages/your-profile/your-profile.component';

const routes: Routes = [
  {
    path: '',
    component: ProfileComponent,
    children: [
      { path: '', redirectTo: 'your-profile', pathMatch: 'full' },
      { path: 'new-password', component: NewPasswordComponent },
      { path: 'your-profile', component: YourProfileComponent },
      { path: '**', redirectTo: 'your-profile', pathMatch: 'full' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProfileRoutingModule {}
