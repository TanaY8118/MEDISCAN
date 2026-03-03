import { useState, useEffect, useCallback } from 'react';
import { getMedicines, createMedicine, deleteMedicine } from '../api/medicine.api';
import { MedicineResponseDTO, MedicineRequestDTO } from '../types/medicine.types';

export const useMedicines = () => {
    const [medicines, setMedicines] = useState<MedicineResponseDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchMedicines = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getMedicines();
            setMedicines(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to fetch medicines');
        } finally {
            setLoading(false);
        }
    }, []);

    const addMedicine = async (data: MedicineRequestDTO) => {
        try {
            const newMedicine = await createMedicine(data);
            setMedicines(prev => [...prev, newMedicine]);
            return newMedicine;
        } catch (err: any) {
            throw new Error(err.response?.data?.message || 'Failed to add medicine');
        }
    };

    const removeMedicine = async (id: string) => {
        try {
            await deleteMedicine(id);
            setMedicines(prev => prev.filter(m => m.id !== id));
        } catch (err: any) {
            throw new Error(err.response?.data?.message || 'Failed to delete medicine');
        }
    };

    useEffect(() => {
        fetchMedicines();
    }, [fetchMedicines]);

    return {
        medicines,
        loading,
        error,
        refresh: fetchMedicines,
        addMedicine,
        removeMedicine
    };
};
