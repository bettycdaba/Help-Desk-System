import { Component } from '@angular/core';

@Component({
  selector: 'app-permissions',
  standalone: true,
  imports: [],
  template: `
    <div class="container py-4">
      <h2 class="mb-3">Permissions</h2>
      <p class="text-muted">Manage permissions for users and roles.</p>
    </div>
  `
})
export class Permissions {}
