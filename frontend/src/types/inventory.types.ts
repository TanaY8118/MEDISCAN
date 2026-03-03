/**
 * ⚠️ FROZEN API CONTRACT - v1
 */
export interface InventoryRequestDTO {
    medicineId: string;
    quantity: number;
    lowStockThreshold: number;
}

export interface InventoryResponseDTO {
    id: number;
    medicineId: string;
    quantity: number;
    lowStockThreshold: number;
    updatedAt: string;
}
