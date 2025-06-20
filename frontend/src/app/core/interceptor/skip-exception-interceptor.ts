import { HttpContextToken } from '@angular/common/http';

export const SKIP_EXCEPTION_INTERCEPTOR = new HttpContextToken<boolean>(() => false);
