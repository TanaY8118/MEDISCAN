import { useState, useEffect, useCallback } from 'react';
import { getInventories, adjustStock, getInventoryByMedicineId } from '../api/inventory.api';
import { InventoryResponseDTO } from '../types/inventory.types';

export const useInventory = () => {
    const [inventory, setInventory] = useState<InventoryResponseDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchInventory = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getInventories();
            setInventory(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to fetch inventory');
        } finally {
            setLoading(false);
        }
    }, []);

    const updateStock = async (medicineId: string, amount: number) => {
        try {
            const updated = await adjustStock(medicineId, amount);
            setInventory(prev => prev.map(item =>
                item.medicineId === medicineId ? updated : item
            ));
            return updated;
        } catch (err: any) {
            throw new Error(err.response?.data?.message || 'Failed to adjust stock');
        }
    };

    useEffect(() => {
        fetchInventory();
    }, [fetchInventory]);

    return {
        inventory,
        loading,
        error,
        refresh: fetchInventory,
        updateStock,
        getMedicineStock: (medicineId: string) => inventory.find(i => i.medicineId === medicineId)
    };
};
