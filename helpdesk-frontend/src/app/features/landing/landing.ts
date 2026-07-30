import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.html',
  styleUrl: './landing.css'
})
export class Landing {

  features = [
    {
      icon: 'bi-ticket-perforated',
      title: 'Ticket Management',
      description:
        'Create, track, and resolve support tickets efficiently. Never lose track of an issue again.'
    },
    {
      icon: 'bi-bell',
      title: 'Email Notifications',
      description:
        'Automatic email alerts keep everyone informed when tickets are created, assigned, or resolved.'
    },
    {
      icon: 'bi-people',
      title: 'Team Collaboration',
      description:
        'Assign tickets to the right team members, add comments, and track every update in real time.'
    },
    {
      icon: 'bi-shield-check',
      title: 'Secure Access',
      description:
        'JWT-based authentication ensures only authorized staff can access the system.'
    },
    {
      icon: 'bi-clock-history',
      title: 'Full Audit Trail',
      description:
        'Every status change and assignment is recorded. Full history for every ticket.'
    },
    {
      icon: 'bi-bar-chart',
      title: 'Dashboard Overview',
      description:
        'Get a real-time overview of all open, in-progress, and resolved tickets at a glance.'
    }
  ];

  stats = [
    { value: '500+', label: 'Tickets Resolved' },
    { value: '50+',  label: 'Active Users' },
    { value: '99%',  label: 'SLA Compliance' },
    { value: '24/7', label: 'System Uptime' }
  ];

  constructor(private router: Router) {}

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
  
  scrollTo(sectionId: string): void {
  const element = document.getElementById(sectionId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
  }
}
}
