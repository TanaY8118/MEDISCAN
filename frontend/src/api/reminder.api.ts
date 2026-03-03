import client from './client';
import { ReminderRequestDTO, ReminderResponseDTO } from '../types/reminder.types';

export const getReminders = async (): Promise<ReminderResponseDTO[]> => {
    const response = await client.get('/reminders');
    return response.data;
};

export const createReminder = async (reminderData: ReminderRequestDTO): Promise<ReminderResponseDTO> => {
    const response = await client.post('/reminders', reminderData);
    return response.data;
};

export const markReminderAsTaken = async (id: string): Promise<void> => {
    await client.post(`/reminders/${id}/taken`);
};
