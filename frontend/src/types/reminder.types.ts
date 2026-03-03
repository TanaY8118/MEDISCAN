/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
export interface ReminderRequestDTO {
    medicineId: string;
    reminderTime: string; // LocalDateTime as ISO string
    frequency?: string;
    note?: string;
}

export interface ReminderResponseDTO {
    id: string;
    medicineId: string;
    medicineName: string;
    reminderTime: string;
    frequency?: string;
    isTaken: boolean;
    takenAt?: string;
    note?: string;
}

export interface DueReminderDTO {
    id: string;
    medicineId: string;
    medicineName: string;
    dueTime: string;
    message: string;
    frequency?: string;
}
