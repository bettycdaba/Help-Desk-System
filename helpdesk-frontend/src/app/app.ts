import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from 
  './shared/components/navbar/navbar';
import { ToastComponent } from 
  './shared/components/toast/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, ToastComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  title = 'helpdesk-frontend';
}