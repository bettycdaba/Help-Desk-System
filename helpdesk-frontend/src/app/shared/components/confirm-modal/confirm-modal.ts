import { Component, Input, Output, EventEmitter, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-modal.html',
  styleUrl: './confirm-modal.css'
})
export class ConfirmModal {

  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure?';
  @Input() confirmText = 'Delete';
  @Input() cancelText = 'Cancel';
  @Input() isVisible = false;
  @Input() isDanger = true;

  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  constructor(private cdr: ChangeDetectorRef) {}

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.isVisible) {
      this.onCancel();
    }
  }

  onConfirm(): void {
    this.confirmed.emit();
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.onCancel();
    }
  }
}