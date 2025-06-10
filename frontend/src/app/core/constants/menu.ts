import { MenuItem } from '../models/menu.model';

export class Menu {
  public static pages: MenuItem[] = [
    {
      group: 'Menu',
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
        {
          icon: 'assets/icons/heroicons/outline/heart.svg',
          label: 'Sick Notes',
          route: '/sick-notes/admin',
        },
      ],
      role: "ADMIN"
    },
    {
      group: 'Menu',
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
      role: "SUPERVISOR"
    },
    {
      group: 'Menu',
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
      role: "EMPLOYEE"
    },
  ];
}
