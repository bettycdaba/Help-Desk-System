export interface Ticket {
  id?: number;
  ticketNumber?: string;
  subject: string;
  description?: string;
  status?: string;
  priority?: string;
  createdAt?: string;
  updatedAt?: string;
  resolvedAt?: string;
  createdById?: number;
  createdByName?: string;
  assignedToId?: number;
  assignedToName?: string;
  categoryId?: number;
  categoryName?: string;
}

export interface TicketComment {
  id?: number;
  comment: string;
  commentedAt?: string;
  ticketId?: number;
  userId: number;
  userName?: string;
}

export interface TicketAssignmentHistory {
  id?: number;
  ticketId?: number;
  oldAssigneeId?: number;
  oldAssigneeName?: string;
  newAssigneeId?: number;
  newAssigneeName?: string;
  assignedById?: number;
  assignedByName?: string;
  assignedAt?: string;
}

export interface TicketStatusHistory {
  id?: number;
  ticketId?: number;
  oldStatus?: string;
  newStatus?: string;
  changedById?: number;
  changedByName?: string;
  changedAt?: string;
}

export interface TicketAssignRequest {
  newAssigneeId: number;
  assignedById: number;
}

export interface TicketStatusUpdateRequest {
  newStatus: string;
  changedById: number;
}