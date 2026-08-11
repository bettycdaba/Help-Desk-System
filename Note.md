# 📋 HELP DESK SYSTEM - CASCADE DELETE & TICKET HANDLING

---

## 🎯 OVERVIEW

When a user is deleted from the system, we need to handle all related data gracefully without breaking the application. Tickets, comments, attachments, and history should remain intact while properly handling the missing user references.

---

## 🗄️ DATABASE CHANGES

### Foreign Key Cascade Strategy

| Table | Column | Cascade Type | Behavior When User Deleted |
|-------|--------|-------------|---------------------------|
| `ticket` | `created_by` | `ON DELETE SET NULL` | Ticket stays, creator set to NULL |
| `ticket` | `assigned_to` | `ON DELETE SET NULL` | Ticket stays, becomes unassigned |
| `ticket_comment` | `user_id` | `ON DELETE SET NULL` | Comment stays, user anonymized |
| `ticket_attachment` | `uploaded_by` | `ON DELETE SET NULL` | File stays, uploader removed |
| `ticket_assignment_history` | `assigned_by` | `ON DELETE SET NULL` | History stays |
| `ticket_assignment_history` | `new_assignee_id` | `ON DELETE SET NULL` | History stays |
| `ticket_assignment_history` | `old_assignee_id` | `ON DELETE SET NULL` | History stays |
| `ticket_status_history` | `changed_by` | `ON DELETE SET NULL` | History stays |
| `ticket_comment` | `ticket_id` | `ON DELETE CASCADE` | Comments deleted with ticket |
| `ticket_attachment` | `ticket_id` | `ON DELETE CASCADE` | Files deleted with ticket |
| `ticket_assignment_history` | `ticket_id` | `ON DELETE CASCADE` | History deleted with ticket |
| `ticket_status_history` | `ticket_id` | `ON DELETE CASCADE` | History deleted with ticket |
| `user_roles` | `user_id` | `ON DELETE CASCADE` | Role assignments deleted |
| `user_roles` | `role_id` | `ON DELETE CASCADE` | Role removed from all users |
| `role_permission` | `role_id` | `ON DELETE CASCADE` | Permissions deleted with role |
| `role_permission` | `permission_id` | `ON DELETE CASCADE` | Permission removed from roles |

### SQL Executed

```sql
-- Make columns nullable where needed
ALTER TABLE ticket MODIFY assigned_to BIGINT NULL;
ALTER TABLE ticket_comment MODIFY user_id BIGINT NULL;
ALTER TABLE ticket_attachment MODIFY uploaded_by BIGINT NULL;
ALTER TABLE ticket_assignment_history MODIFY assigned_by BIGINT NULL;
ALTER TABLE ticket_assignment_history MODIFY new_assignee_id BIGINT NULL;
ALTER TABLE ticket_assignment_history MODIFY old_assignee_id BIGINT NULL;
ALTER TABLE ticket_status_history MODIFY changed_by BIGINT NULL;

-- Drop and recreate foreign keys with cascade rules
-- (All ALTER TABLE DROP FOREIGN KEY + ADD CONSTRAINT statements)
```

### Handling Orphaned References

Before adding the new constraint, orphaned records (tickets referencing deleted users) must be cleaned:

```sql
UPDATE ticket SET created_by = NULL 
WHERE created_by IS NOT NULL 
  AND created_by NOT IN (SELECT id FROM users);
```

---

## ☕ BACKEND CODE CHANGES

### 1. Ticket Entity (`Ticket.java`)

**Added `@NotFound` annotation** to prevent JPA from crashing when user reference is NULL:

```java
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "created_by", nullable = true)  // Changed to nullable
@NotFound(action = NotFoundAction.IGNORE)           // Handle missing users
private User createdBy;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "assigned_to", nullable = true)
@NotFound(action = NotFoundAction.IGNORE)
private User assignedTo;
```

### 2. TicketServiceImpl - `mapToResponse()` Method

**Added null checks** for `createdBy` and `assignedTo`:

```java
private TicketResponseDTO mapToResponse(Ticket ticket) {
    return TicketResponseDTO.builder()
            // ... other fields ...
            .createdById(ticket.getCreatedBy() != null 
                ? ticket.getCreatedBy().getId() : null)
            .createdByName(ticket.getCreatedBy() != null 
                ? ticket.getCreatedBy().getFirstName() + " " 
                  + ticket.getCreatedBy().getLastName() 
                : null)
            .assignedToId(ticket.getAssignedTo() != null 
                ? ticket.getAssignedTo().getId() : null)
            .assignedToName(ticket.getAssignedTo() != null 
                ? ticket.getAssignedTo().getFirstName() + " " 
                  + ticket.getAssignedTo().getLastName() 
                : null)
            // ...
            .build();
}
```

### 3. Inactive User Prevention

Added check in `assignTicket()` to prevent assigning to inactive users:

```java
if (!newAssignee.getActive()) {
    throw new BadRequestException(
        "Cannot assign ticket to inactive user: " 
        + newAssignee.getFirstName() + " " + newAssignee.getLastName());
}
```

### 4. Active Users Endpoint

Added `/api/users/active` to return only active users for assignment dropdowns:

- `UserRepository.findByActiveTrue()`
- `UserService.getActiveUsers()`
- `UserController.getActiveUsers()`

---

## 🎨 FRONTEND CHANGES

### 1. Ticket Detail Component

- `createdByName` and `assignedToName` display "—" when NULL
- Assign dropdown only shows active users
- Only admins can assign tickets

### 2. Ticket List Component

- Handles NULL `createdByName` gracefully
- Tabs: All, Assigned, Unassigned
- Sortable columns

### 3. User Management

- Delete button deactivates user (soft delete supported)
- Active users only in assign dropdowns

---

## 📊 WHAT HAPPENS AFTER USER DELETION

### Tickets Created by Deleted User

| Field | Before | After |
|-------|--------|-------|
| `created_by` | User ID | NULL |
| Display | "John Doe" | "—" |
| Ticket | ✅ Exists | ✅ Exists |
| Can edit? | ✅ | ✅ (admin) |

### Tickets Assigned to Deleted User

| Field | Before | After |
|-------|--------|-------|
| `assigned_to` | User ID | NULL |
| Display | "John Doe" | "—" or "Unassigned" |
| Ticket | ✅ Exists | ✅ Exists |
| Can reassign? | ✅ | ✅ (admin) |

### Comments by Deleted User

| Field | Before | After |
|-------|--------|-------|
| `user_id` | User ID | NULL |
| Comment text | ✅ Preserved | ✅ Preserved |
| Display name | "John Doe" | "—" |

### History Records

| Field | Before | After |
|-------|--------|-------|
| User references | User ID | NULL |
| History | ✅ Preserved | ✅ Preserved |

---

## ✅ SUMMARY

| Feature | Status |
|---------|--------|
| Delete user without breaking tickets | ✅ Working |
| Tickets display correctly after user deleted | ✅ Working |
| Comments preserved | ✅ Working |
| Attachments preserved | ✅ Working |
| History preserved | ✅ Working |
| Inactive users cannot be assigned | ✅ Working |
| Only active users shown in dropdowns | ✅ Working |
| Only admins can assign tickets | ✅ Working |

---

## 📁 FILES MODIFIED

| File | Changes |
|------|---------|
| `Ticket.java` | Added `@NotFound`, changed `nullable = true` |
| `TicketServiceImpl.java` | Null checks in `mapToResponse`, inactive user check |
| `TicketService.java` | Added `updateTicketDetails` method |
| `UserController.java` | Added `/active` endpoint |
| `UserServiceImpl.java` | Added `getActiveUsers` method |
| `UserService.java` | Added `getActiveUsers` to interface |
| `UserRepository.java` | Added `findByActiveTrue` |
| `ticket-list.ts` | Tab filtering, null handling |
| `ticket-detail.ts` | Active users only, admin-only assign |
| `ticket-create.html` | Removed assign for non-admin |
| Database | Cascade constraints on all FK relationships |

---

**End of Documentation** 📋