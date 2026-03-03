import client from './client';
import { HistoryResponseDTO, TimelineResponse } from '../types/history.types';

export const getTimeline = async (days: number = 7): Promise<TimelineResponse> => {
    const response = await client.get<TimelineResponse>(`/history/timeline`, {
        params: { days }
    });
    return response.data;
};

export const getMedicineHistory = async (medicineId: string): Promise<HistoryResponseDTO[]> => {
    const response = await client.get<HistoryResponseDTO[]>(`/history/${medicineId}`);
    return response.data;
};
