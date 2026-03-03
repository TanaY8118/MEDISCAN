export interface TimelineEventDTO {
    timestamp: string;
    eventType: string; // REMINDER_TAKEN, STOCK_CHANGED, MEDICINE_ADDED, etc.
    medicineName: string;
    description: string;
    metadata: Record<string, any>;
}

export interface TimelineResponse {
    events: TimelineEventDTO[];
    totalEvents: number;
    page: number;
    pageSize: number;
}

export interface HistoryResponseDTO {
    id: string;
    medicineId: string;
    action: string;
    details: string;
    performedBy: string;
    timestamp: string;
}
