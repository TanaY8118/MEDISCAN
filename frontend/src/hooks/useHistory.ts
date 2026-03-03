import { useState, useEffect, useCallback, useMemo } from 'react';
import { getTimeline } from '../api/history.api';
import { TimelineEventDTO } from '../types/history.types';
import { parseISO, format, isSameDay, subDays } from 'date-fns';

export const useHistory = (days: number = 30) => {
    const [events, setEvents] = useState<TimelineEventDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchTimeline = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getTimeline(days);
            setEvents(data.events);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to fetch timeline history');
        } finally {
            setLoading(false);
        }
    }, [days]);

    useEffect(() => {
        fetchTimeline();
    }, [fetchTimeline]);

    // Derive summary for the heatmap (last X days)
    const heatmapData = useMemo(() => {
        const data: { [key: string]: 'success' | 'missed' | 'empty' } = {};
        const today = new Date();

        for (let i = 0; i < days; i++) {
            const date = subDays(today, i);
            const dateKey = format(date, 'yyyy-MM-dd');

            // Check events for this day
            const dayEvents = events.filter(e => isSameDay(parseISO(e.timestamp), date));

            if (dayEvents.length === 0) {
                // Future days are empty, past days with no events are empty
                data[dateKey] = 'empty';
            } else {
                // If any event is MISSES or SKIPPED, mark as missed (simplified)
                const hasMissed = dayEvents.some(e =>
                    e.eventType.includes('MISSED') || e.eventType.includes('SKIPPED')
                );
                const hasTaken = dayEvents.some(e => e.eventType.includes('TAKEN'));

                if (hasMissed && !hasTaken) {
                    data[dateKey] = 'missed';
                } else if (hasTaken) {
                    data[dateKey] = 'success';
                } else {
                    data[dateKey] = 'empty';
                }
            }
        }
        return data;
    }, [events, days]);

    const stats = useMemo(() => {
        const taken = events.filter(e => e.eventType.includes('TAKEN')).length;
        const missed = events.filter(e => e.eventType.includes('MISSED')).length;
        const total = taken + missed;

        return {
            rate: total > 0 ? Math.round((taken / total) * 100) : 0,
            taken,
            missed
        };
    }, [events]);

    return {
        events,
        heatmapData,
        stats,
        loading,
        error,
        refresh: fetchTimeline
    };
};
