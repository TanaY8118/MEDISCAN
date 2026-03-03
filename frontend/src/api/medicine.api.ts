import client from './client';
import { MedicineRequestDTO, MedicineResponseDTO } from '../types/medicine.types';

export const getMedicines = async (): Promise<MedicineResponseDTO[]> => {
    const response = await client.get('/medicines');
    return response.data;
};

export const getMedicineByBarcode = async (barcode: string): Promise<MedicineResponseDTO> => {
    const response = await client.get(`/medicines/barcode/${barcode}`);
    return response.data;
};

export const createMedicine = async (medicineData: MedicineRequestDTO): Promise<MedicineResponseDTO> => {
    const response = await client.post('/medicines', medicineData);
    return response.data;
};

export const updateMedicine = async (id: string, medicineData: MedicineRequestDTO): Promise<MedicineResponseDTO> => {
    const response = await client.put(`/medicines/${id}`, medicineData);
    return response.data;
};

export const deleteMedicine = async (id: string): Promise<void> => {
    await client.delete(`/medicines/${id}`);
};
