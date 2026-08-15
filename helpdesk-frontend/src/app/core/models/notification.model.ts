export interface AppNotification {
  id: number;
  message: string;
  type: 'assign' | 'status' | 'comment' | 'resolved';
  ticketId?: number;
  ticketNumber?: string;
  isRead: boolean;
  createdAt: string;
}