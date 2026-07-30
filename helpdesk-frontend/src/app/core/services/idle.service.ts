import { Injectable, NgZone, OnDestroy } 
  from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } 
  from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class IdleService implements OnDestroy {

  private idleTimeout = 30 * 60 * 1000;
  private timer: any = null;
  private isWatching = false;

  private boundReset = this.resetTimer.bind(this);

  constructor(
    private router: Router,
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  startWatching(): void {
    if (this.isWatching) return;
    this.isWatching = true;

    this.ngZone.runOutsideAngular(() => {
      window.addEventListener(
        'mousemove', this.boundReset);
      window.addEventListener(
        'mousedown', this.boundReset);
      window.addEventListener(
        'keypress', this.boundReset);
      window.addEventListener(
        'touchmove', this.boundReset);
      window.addEventListener(
        'scroll', this.boundReset);
    });

    this.resetTimer();
  }

  stopWatching(): void {
    this.isWatching = false;
    window.removeEventListener(
      'mousemove', this.boundReset);
    window.removeEventListener(
      'mousedown', this.boundReset);
    window.removeEventListener(
      'keypress', this.boundReset);
    window.removeEventListener(
      'touchmove', this.boundReset);
    window.removeEventListener(
      'scroll', this.boundReset);
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }

  private resetTimer(): void {
    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(() => {
      this.ngZone.run(() => {
        this.authService.logout();
        this.router.navigate(['/login']);
      });
    }, this.idleTimeout);
  }

  ngOnDestroy(): void {
    this.stopWatching();
  }
}