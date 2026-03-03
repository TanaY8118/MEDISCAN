/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
export interface MedicineRequestDTO {
    name: string;
    description?: string;
}

export interface MedicineResponseDTO {
    id: string;
    name: string;
    description?: string;
    createdAt: string; // LocalDateTime handled as strings in JSON
}
