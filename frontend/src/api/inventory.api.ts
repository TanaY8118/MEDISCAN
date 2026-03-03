import client from './client';
import { InventoryRequestDTO, InventoryResponseDTO } from '../types/inventory.types';

export const getInventories = async (): Promise<InventoryResponseDTO[]> => {
    const response = await client.get('/inventories');
    return response.data;
};

export const updateInventory = async (data: InventoryRequestDTO): Promise<InventoryResponseDTO> => {
    const response = await client.post('/inventories', data);
    return response.data;
};

export const getInventoryByMedicineId = async (medicineId: string): Promise<InventoryResponseDTO> => {
    const response = await client.get(`/inventories/${medicineId}`);
    return response.data;
};

export const adjustStock = async (medicineId: string, amount: number): Promise<InventoryResponseDTO> => {
    // /api/inventories/{medicineId}/adjust?amount={amount}
    const response = await client.post(`/inventories/${medicineId}/adjust?amount=${amount}`);
    return response.data;
};
