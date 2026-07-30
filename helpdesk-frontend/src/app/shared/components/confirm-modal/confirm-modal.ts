import { Component, Input, Output, EventEmitter }
  from '@angular/core';
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

  onConfirm(): void {
    this.confirmed.emit();
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}