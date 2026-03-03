import React, { useMemo, useState, useEffect } from 'react';
import { View, Text, StyleSheet, SectionList, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useReminders } from '../../hooks/useReminders';
import { useMedicines } from '../../hooks/useMedicines';
import { Theme } from '../../constants/Theme';
import { format, formatDistanceToNow, isAfter, parseISO } from 'date-fns';

const ReminderListScreen = ({ navigation }: any) => {
    const { reminders, loading: loadingRem, refresh: refreshRem, takeReminder } = useReminders();
    const { medicines, refresh: refreshMed } = useMedicines();
    const [now, setNow] = useState(new Date());

    useEffect(() => {
        const timer = setInterval(() => setNow(new Date()), 60000);
        return () => clearInterval(timer);
    }, []);

    useFocusEffect(
        React.useCallback(() => {
            refreshRem();
            refreshMed();
        }, [])
    );

    const nextReminder = useMemo(() => {
        const upcoming = reminders
            .filter(r => !r.isTaken && isAfter(new Date(r.reminderTime), now))
            .sort((a, b) => new Date(a.reminderTime).getTime() - new Date(b.reminderTime).getTime());
        return upcoming.length > 0 ? upcoming[0] : null;
    }, [reminders, now]);

    const sections = useMemo(() => {
        const morning: any[] = [];
        const afternoon: any[] = [];
        const evening: any[] = [];
        const night: any[] = [];

        reminders.forEach(r => {
            const hour = new Date(r.reminderTime).getHours();
            if (hour >= 5 && hour < 12) morning.push(r);
            else if (hour >= 12 && hour < 17) afternoon.push(r);
            else if (hour >= 17 && hour < 21) evening.push(r);
            else night.push(r);
        });

        const sortFn = (a, b) => new Date(a.reminderTime).getTime() - new Date(b.reminderTime).getTime();

        return [
            { title: 'Morning', data: morning.sort(sortFn) },
            { title: 'Afternoon', data: afternoon.sort(sortFn) },
            { title: 'Evening', data: evening.sort(sortFn) },
            { title: 'Night', data: night.sort(sortFn) },
        ].filter(s => s.data.length > 0);
    }, [reminders]);

    const renderHeader = () => {
        if (!nextReminder) return null;

        return (
            <View style={styles.countdownCard}>
                <View style={styles.countdownLeft}>
                    <Text style={styles.countdownLabel}>Next Dose</Text>
                    <Text style={styles.countdownValue}>
                        {formatDistanceToNow(new Date(nextReminder.reminderTime), { addSuffix: true })}
                    </Text>
                </View>
                <View style={styles.countdownRight}>
                    <Text style={styles.countdownMed}>{nextReminder.medicineName}</Text>
                    <Text style={styles.countdownTime}>at {format(new Date(nextReminder.reminderTime), 'HH:mm')}</Text>
                </View>
            </View>
        );
    };

    const renderItem = ({ item }: any) => {
        const isTaken = item.isTaken;
        const isMissed = new Date(item.reminderTime) < now && !isTaken;
        const timeStr = format(new Date(item.reminderTime), 'HH:mm');

        return (
            <View style={[styles.card, isTaken && styles.cardTaken]}>
                <View style={styles.timeColumn}>
                    <Text style={[styles.timeText, isMissed && !isTaken && styles.missedText]}>{timeStr}</Text>
                    <View style={[
                        styles.statusLine,
                        isTaken ? styles.statusLineTaken : (isMissed ? styles.statusLineMissed : styles.statusLineUpcoming)
                    ]} />
                </View>

                <View style={styles.contentColumn}>
                    <Text style={styles.medName}>{item.medicineName}</Text>
                    <Text style={styles.dosageInfo}>1 Tablet • Before food</Text>
                    {isTaken ? (
                        <Text style={styles.takenLabel}>✓ Taken at {format(new Date(item.takenAt!), 'HH:mm')}</Text>
                    ) : (
                        <View style={styles.actionRow}>
                            <TouchableOpacity style={styles.takeBtn} onPress={() => takeReminder(item.id)}>
                                <Text style={styles.takeBtnText}>Mark as Taken</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={styles.snoozeBtn}>
                                <Text style={styles.snoozeBtnText}>Snooze</Text>
                            </TouchableOpacity>
                        </View>
                    )}
                </View>
            </View>
        );
    };

    return (
        <View style={styles.container}>
            <SectionList
                sections={sections}
                keyExtractor={(item) => item.id}
                renderItem={renderItem}
                ListHeaderComponent={renderHeader}
                renderSectionHeader={({ section: { title } }) => (
                    <Text style={styles.sectionHeader}>{title}</Text>
                )}
                contentContainerStyle={styles.list}
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl refreshing={loadingRem} onRefresh={refreshRem} tintColor={Theme.colors.primary} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyContainer}>
                        <Text style={styles.emptyIcon}>🔔</Text>
                        <Text style={styles.emptyText}>No reminders set for today.</Text>
                    </View>
                }
            />

            <TouchableOpacity
                style={styles.fab}
                onPress={() => navigation.navigate('AddReminder')}
            >
                <Text style={styles.fabIcon}>+</Text>
            </TouchableOpacity>
        </View>
    );
};


const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    list: {
        paddingHorizontal: Theme.spacing.lg,
        paddingBottom: 100,
    },
    sectionHeader: {
        fontSize: 14,
        fontWeight: 'bold',
        color: Theme.colors.textMuted,
        textTransform: 'uppercase',
        marginTop: Theme.spacing.lg,
        marginBottom: Theme.spacing.md,
        letterSpacing: 1,
    },
    card: {
        backgroundColor: Theme.colors.surface,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.md,
        marginBottom: Theme.spacing.md,
        flexDirection: 'row',
        ...Theme.shadows.soft,
        borderWidth: 1,
        borderColor: 'rgba(0,0,0,0.02)',
    },
    cardTaken: {
        opacity: 0.6,
    },
    timeColumn: {
        alignItems: 'center',
        width: 60,
        marginRight: Theme.spacing.md,
    },
    timeText: {
        fontSize: 16,
        fontWeight: 'bold',
        color: Theme.colors.primary,
    },
    missedText: {
        color: Theme.colors.danger,
    },
    statusLine: {
        width: 3,
        flex: 1,
        borderRadius: 1.5,
        marginTop: 8,
    },
    statusLineTaken: {
        backgroundColor: Theme.colors.success,
    },
    statusLineMissed: {
        backgroundColor: Theme.colors.danger,
    },
    statusLineUpcoming: {
        backgroundColor: Theme.colors.border,
    },
    contentColumn: {
        flex: 1,
    },
    medName: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.text,
        fontFamily: Theme.typography.header,
    },
    dosageInfo: {
        fontSize: 14,
        color: Theme.colors.textMuted,
        marginTop: 2,
    },
    actionRow: {
        flexDirection: 'row',
        marginTop: Theme.spacing.md,
    },
    takeBtn: {
        backgroundColor: Theme.colors.primary,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 8,
        marginRight: Theme.spacing.sm,
    },
    takeBtnText: {
        color: 'white',
        fontWeight: 'bold',
        fontSize: 12,
    },
    snoozeBtn: {
        backgroundColor: Theme.colors.surface,
        paddingHorizontal: 16,
        paddingVertical: 8,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: Theme.colors.border,
    },
    snoozeBtnText: {
        color: Theme.colors.text,
        fontWeight: '600',
        fontSize: 12,
    },
    takenLabel: {
        marginTop: Theme.spacing.md,
        fontSize: 12,
        color: Theme.colors.success,
        fontWeight: 'bold',
    },
    emptyContainer: {
        alignItems: 'center',
        marginTop: 100,
    },
    emptyIcon: {
        fontSize: 64,
        opacity: 0.2,
        marginBottom: Theme.spacing.md,
    },
    emptyText: {
        color: Theme.colors.textMuted,
        fontSize: 16,
    },
    fab: {
        position: 'absolute',
        width: 60,
        height: 60,
        alignItems: 'center',
        justifyContent: 'center',
        right: 24,
        bottom: 24,
        backgroundColor: Theme.colors.accent,
        borderRadius: 30,
        ...Theme.shadows.deep,
    },
    fabIcon: {
        fontSize: 32,
        color: 'white',
        fontWeight: '300',
    },
    countdownCard: {
        backgroundColor: Theme.colors.primary,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.lg,
        marginTop: Theme.spacing.lg,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        ...Theme.shadows.deep,
    },
    countdownLeft: {
        flex: 1,
    },
    countdownLabel: {
        color: 'rgba(255,255,255,0.7)',
        fontSize: 12,
        fontWeight: 'bold',
        textTransform: 'uppercase',
    },
    countdownValue: {
        color: 'white',
        fontSize: 24,
        fontWeight: 'bold',
        marginTop: 4,
    },
    countdownRight: {
        alignItems: 'flex-end',
    },
    countdownMed: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
    },
    countdownTime: {
        color: 'rgba(255,255,255,0.9)',
        fontSize: 14,
        marginTop: 2,
    },
});


export default ReminderListScreen;
