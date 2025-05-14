import { MenuItem } from '../models/menu.model';

export class Menu {
  public static pages: MenuItem[] = [
    {
      group: 'Admin',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/home.svg',
          label: 'Home',
          route: '/home/admin',
        },
        {
          icon: 'assets/icons/heroicons/outline/building-office.svg',
          label: 'Departments',
          route: '/departments/admin',
        },
      ],
      role: "ADMIN"
    },
    {
      group: 'Supervisor',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/home.svg',
          label: 'Home',
          route: '/home/supervisor',
        },
        {
          icon: 'assets/icons/heroicons/outline/building-office.svg',
          label: 'Department',
          route: '/departments/supervisor',
        },
        {
          icon: 'assets/icons/heroicons/outline/sun.svg',
          label: 'Vacations',
          route: '/vacations/supervisor',
        },
        {
          icon: 'assets/icons/heroicons/outline/heart.svg',
          label: 'Sick Notes',
          route: '/sick-notes/supervisor',
        },
      ],
      role: "ADMIN"
    },
    {
      group: 'Employee',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/home.svg',
          label: 'Home',
          route: '/home/employee',
        },
        {
          icon: 'assets/icons/heroicons/outline/sun.svg',
          label: 'Vacations',
          route: '/vacations/employee',
        },
        {
          icon: 'assets/icons/heroicons/outline/heart.svg',
          label: 'Sick Notes',
          route: '/sick-notes/employee',
        }
      ],
      role: "ADMIN"
    },

    {
      group: 'Template',
      separator: false,
      items: [
        {
          icon: 'assets/icons/heroicons/outline/lock-closed.svg',
          label: 'Auth',
          route: '/auth',
          children: [
            { label: 'Sign up', route: '/auth/sign-up' },
            { label: 'Sign in', route: '/auth/sign-in' },
            { label: 'Forgot Password', route: '/auth/forgot-password' },
            { label: 'New Password', route: '/auth/new-password' },
            { label: 'Two Steps', route: '/auth/two-steps' },
          ],
        },
        {
          icon: 'assets/icons/heroicons/outline/cube.svg',
          label: 'Components',
          route: '/components',
          children: [{ label: 'Table', route: '/components/table' }],
        },
      ],
      role: "ADMIN"
    }
  ];
}
