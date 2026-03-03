import { useState, useEffect, useCallback } from 'react';
import { getReminders, markReminderAsTaken, createReminder } from '../api/reminder.api';
import { ReminderResponseDTO, ReminderRequestDTO } from '../types/reminder.types';

export const useReminders = () => {
    const [reminders, setReminders] = useState<ReminderResponseDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchReminders = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getReminders();
            setReminders(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to fetch reminders');
        } finally {
            setLoading(false);
        }
    }, []);

    const takeReminder = async (id: string) => {
        try {
            await markReminderAsTaken(id);
            setReminders(prev => prev.map(r =>
                r.id === id ? { ...r, isTaken: true, takenAt: new Date().toISOString() } : r
            ));
        } catch (err: any) {
            throw new Error(err.response?.data?.message || 'Failed to mark reminder as taken');
        }
    };

    const addReminder = async (data: ReminderRequestDTO) => {
        try {
            const newReminder = await createReminder(data);
            setReminders(prev => [...prev, newReminder]);
            return newReminder;
        } catch (err: any) {
            throw new Error(err.response?.data?.message || 'Failed to create reminder');
        }
    };

    useEffect(() => {
        fetchReminders();
    }, [fetchReminders]);

    return {
        reminders,
        loading,
        error,
        refresh: fetchReminders,
        takeReminder,
        addReminder
    };
};
