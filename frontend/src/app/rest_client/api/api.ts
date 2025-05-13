export * from './adminEndpoint.service';
import { AdminEndpointService } from './adminEndpoint.service';
export * from './customHealthEndpoint.service';
import { CustomHealthEndpointService } from './customHealthEndpoint.service';
export * from './loginEndpoint.service';
import { LoginEndpointService } from './loginEndpoint.service';
export * from './messageEndpoint.service';
import { MessageEndpointService } from './messageEndpoint.service';
export const APIS = [AdminEndpointService, CustomHealthEndpointService, LoginEndpointService, MessageEndpointService];
