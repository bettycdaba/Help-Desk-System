import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();
  private counter = 0;

  success(message: string): void {
    this.addToast(message, 'success');
  }

  error(message: string): void {
    this.addToast(message, 'error');
  }

  warning(message: string): void {
    this.addToast(message, 'warning');
  }

  info(message: string): void {
    this.addToast(message, 'info');
  }

  private addToast(message: string,
    type: 'success' | 'error' | 'warning' | 'info'): void {
    const toast: Toast = {
      id: ++this.counter,
      message,
      type
    };

    const current = this.toastsSubject.value;
    this.toastsSubject.next([...current, toast]);

    setTimeout(() => this.removeToast(toast.id), 4000);
  }

  removeToast(id: number): void {
    const current = this.toastsSubject.value;
    this.toastsSubject.next(
      current.filter(t => t.id !== id));
  }
}