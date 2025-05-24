import { NgModule } from '@angular/core';
import { NoRoleRoutingModule } from './no-role-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { AngularSvgIconModule } from 'angular-svg-icon';

@NgModule({ declarations: [], imports: [NoRoleRoutingModule, AngularSvgIconModule.forRoot()], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class NoRoleModule {}
