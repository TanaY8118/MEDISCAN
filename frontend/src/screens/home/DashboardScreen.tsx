import React, { useContext, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, RefreshControl, ActivityIndicator, Dimensions } from 'react-native';
import { AuthContext } from '../../context/AuthContext';
import { useMedicines } from '../../hooks/useMedicines';
import { useReminders } from '../../hooks/useReminders';
import { useInventory } from '../../hooks/useInventory';
import { Theme } from '../../constants/Theme';

const { width } = Dimensions.get('window');

const DashboardScreen = ({ navigation }: any) => {
    const { user, logout } = useContext(AuthContext);
    const { medicines, loading: loadingMed, refresh: refreshMed } = useMedicines();
    const { reminders, loading: loadingRem, refresh: refreshRem } = useReminders();
    const { inventory, loading: loadingInv, refresh: refreshInv } = useInventory();

    const isRefreshing = loadingMed || loadingRem || loadingInv;

    const onRefresh = () => {
        refreshMed();
        refreshRem();
        refreshInv();
    };

    const greeting = useMemo(() => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Good morning';
        if (hour < 17) return 'Good afternoon';
        return 'Good evening';
    }, []);

    const streak = useMemo(() => {
        const history = reminders.filter(r => r.isTaken || new Date(r.reminderTime) < new Date());
        if (history.length === 0) return 0;

        const dateGroups = new Map<string, boolean>();
        history.forEach(r => {
            const date = new Date(r.reminderTime).toDateString();
            const current = dateGroups.get(date) ?? true;
            dateGroups.set(date, current && r.isTaken);
        });

        let count = 0;
        let d = new Date();
        // Today check (if all taken so far)
        if (dateGroups.get(d.toDateString()) === false) return 0;
        if (dateGroups.get(d.toDateString())) count++;

        // Backwards check
        while (true) {
            d.setDate(d.getDate() - 1);
            if (dateGroups.get(d.toDateString())) {
                count++;
            } else {
                break;
            }
            if (count > 365) break; // sanity
        }
        return count;
    }, [reminders]);

    const overdueReminders = reminders.filter(r => !r.isTaken && new Date(r.reminderTime) < new Date());
    const upcomingToday = reminders
        .filter(r => !r.isTaken && new Date(r.reminderTime).toDateString() === new Date().toDateString() && new Date(r.reminderTime) >= new Date())
        .sort((a, b) => new Date(a.reminderTime).getTime() - new Date(b.reminderTime).getTime());

    const nextDose = upcomingToday[0];

    const stats = {
        active: medicines.length,
        takenToday: reminders.filter(r => r.isTaken && new Date(r.takenAt || '').toDateString() === new Date().toDateString()).length,
        streak,
    };

    return (
        <View style={styles.container}>
            <ScrollView
                style={styles.scrollView}
                refreshControl={
                    <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} tintColor={Theme.colors.primary} />
                }
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.header}>
                    <View>
                        <Text style={styles.greetingText}>{greeting},</Text>
                        <Text style={styles.userNameText}>Alex</Text>
                    </View>
                    <TouchableOpacity style={styles.profileCircle} onPress={logout}>
                        <Text style={styles.profileInitial}>A</Text>
                    </TouchableOpacity>
                </View>

                {overdueReminders.length > 0 && (
                    <View style={styles.alertBanner}>
                        <Text style={styles.alertText}>
                            ⚠️ You have {overdueReminders.length} overdue dose{overdueReminders.length > 1 ? 's' : ''}
                        </Text>
                        <TouchableOpacity onPress={() => navigation.navigate('ReminderStack')}>
                            <Text style={styles.alertAction}>Review</Text>
                        </TouchableOpacity>
                    </View>
                )}

                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Next Dose</Text>
                    {nextDose ? (
                        <TouchableOpacity
                            style={styles.nextDoseCard}
                            onPress={() => navigation.navigate('ReminderStack')}
                        >
                            <View style={styles.nextDoseInfo}>
                                <Text style={styles.nextDoseTime}>
                                    {new Date(nextDose.reminderTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </Text>
                                <Text style={styles.nextDoseName}>{nextDose.medicineName}</Text>
                                <Text style={styles.nextDoseSub}>1 Tablet • Before food</Text>
                            </View>
                            <View style={styles.countdownCircle}>
                                <Text style={styles.countdownText}>2h</Text>
                                <Text style={styles.countdownUnit}>left</Text>
                            </View>
                        </TouchableOpacity>
                    ) : (
                        <View style={styles.emptyCard}>
                            <Text style={styles.emptyText}>All caught up for today! ✨</Text>
                        </View>
                    )}
                </View>

                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Today's Schedule</Text>
                    <ScrollView
                        horizontal
                        showsHorizontalScrollIndicator={false}
                        contentContainerStyle={styles.timelineContainer}
                    >
                        {upcomingToday.map((item, index) => (
                            <View key={item.id} style={[styles.timelinePill, index === 0 && styles.timelinePillActive]}>
                                <Text style={[styles.timelineTime, index === 0 && styles.timelineTextActive]}>
                                    {new Date(item.reminderTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </Text>
                                <Text style={[styles.timelineName, index === 0 && styles.timelineTextActive]}>
                                    {item.medicineName.substring(0, 8)}...
                                </Text>
                            </View>
                        ))}
                        {upcomingToday.length === 0 && <Text style={styles.emptyTimelineText}>No more doses scheduled for today.</Text>}
                    </ScrollView>
                </View>

                <View style={styles.statsRow}>
                    <View style={styles.statItem}>
                        <Text style={styles.statValue}>{stats.active}</Text>
                        <Text style={styles.statLabel}>Active</Text>
                    </View>
                    <View style={styles.statDivider} />
                    <View style={styles.statItem}>
                        <Text style={styles.statValue}>{stats.takenToday}</Text>
                        <Text style={styles.statLabel}>Taken</Text>
                    </View>
                    <View style={styles.statDivider} />
                    <View style={styles.statItem}>
                        <Text style={styles.statValue}>{stats.streak} 🔥</Text>
                        <Text style={styles.statLabel}>Streak</Text>
                    </View>
                </View>

                <View style={styles.quickActionsSection}>
                    <TouchableOpacity
                        style={styles.mainActionButton}
                        onPress={() => navigation.navigate('MedicinesStack')}
                    >
                        <Text style={styles.mainActionText}>Manage Medicines</Text>
                    </TouchableOpacity>
                </View>
            </ScrollView>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    scrollView: {
        flex: 1,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: Theme.spacing.lg,
        paddingTop: Theme.spacing.xl,
        paddingBottom: Theme.spacing.md,
    },
    greetingText: {
        fontSize: 16,
        color: Theme.colors.textMuted,
        fontFamily: Theme.typography.body,
    },
    userNameText: {
        fontSize: 28,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        fontFamily: Theme.typography.header,
    },
    profileCircle: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: Theme.colors.surface,
        justifyContent: 'center',
        alignItems: 'center',
        ...Theme.shadows.soft,
    },
    profileInitial: {
        color: Theme.colors.primary,
        fontWeight: 'bold',
        fontSize: 18,
    },
    alertBanner: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#FEF3C7', // Soft amber
        marginHorizontal: Theme.spacing.lg,
        padding: Theme.spacing.md,
        borderRadius: Theme.borderRadius.md,
        marginBottom: Theme.spacing.lg,
        borderWidth: 1,
        borderColor: Theme.colors.accent,
    },
    alertText: {
        color: '#92400E',
        fontWeight: '600',
        fontSize: 14,
    },
    alertAction: {
        color: Theme.colors.accent,
        fontWeight: 'bold',
        textDecorationLine: 'underline',
    },
    section: {
        marginBottom: Theme.spacing.xl,
        paddingHorizontal: Theme.spacing.lg,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        marginBottom: Theme.spacing.md,
        fontFamily: Theme.typography.header,
    },
    nextDoseCard: {
        backgroundColor: Theme.colors.primary,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.lg,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        ...Theme.shadows.deep,
    },
    nextDoseInfo: {
        flex: 1,
    },
    nextDoseTime: {
        color: 'rgba(255,255,255,0.7)',
        fontSize: 14,
        fontWeight: '600',
        marginBottom: 4,
    },
    nextDoseName: {
        color: 'white',
        fontSize: 22,
        fontWeight: 'bold',
        marginBottom: 4,
    },
    nextDoseSub: {
        color: 'rgba(255,255,255,0.9)',
        fontSize: 14,
    },
    countdownCircle: {
        width: 60,
        height: 60,
        borderRadius: 30,
        backgroundColor: 'rgba(255,255,255,0.15)',
        justifyContent: 'center',
        alignItems: 'center',
        borderWidth: 2,
        borderColor: 'rgba(255,255,255,0.3)',
    },
    countdownText: {
        color: 'white',
        fontWeight: 'bold',
        fontSize: 18,
    },
    countdownUnit: {
        color: 'white',
        fontSize: 10,
        textTransform: 'uppercase',
    },
    emptyCard: {
        backgroundColor: Theme.colors.surface,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.xl,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: Theme.colors.border,
        borderStyle: 'dashed',
    },
    emptyText: {
        color: Theme.colors.textMuted,
        fontSize: 16,
    },
    timelineContainer: {
        paddingRight: Theme.spacing.lg,
    },
    timelinePill: {
        backgroundColor: Theme.colors.surface,
        paddingVertical: 12,
        paddingHorizontal: 16,
        borderRadius: 25,
        marginRight: Theme.spacing.sm,
        borderWidth: 1,
        borderColor: Theme.colors.border,
        alignItems: 'center',
        minWidth: 100,
    },
    timelinePillActive: {
        backgroundColor: Theme.colors.primary,
        borderColor: Theme.colors.primary,
    },
    timelineTime: {
        fontSize: 12,
        fontWeight: 'bold',
        color: Theme.colors.text,
    },
    timelineName: {
        fontSize: 12,
        color: Theme.colors.textMuted,
        marginTop: 2,
    },
    timelineTextActive: {
        color: 'white',
    },
    emptyTimelineText: {
        color: Theme.colors.textMuted,
        fontStyle: 'italic',
    },
    statsRow: {
        flexDirection: 'row',
        backgroundColor: Theme.colors.surface,
        marginHorizontal: Theme.spacing.lg,
        padding: Theme.spacing.lg,
        borderRadius: Theme.borderRadius.md,
        justifyContent: 'space-around',
        alignItems: 'center',
        ...Theme.shadows.soft,
        marginBottom: Theme.spacing.xl,
    },
    statItem: {
        alignItems: 'center',
    },
    statValue: {
        fontSize: 20,
        fontWeight: 'bold',
        color: Theme.colors.primary,
    },
    statLabel: {
        fontSize: 12,
        color: Theme.colors.textMuted,
        marginTop: 2,
    },
    statDivider: {
        width: 1,
        height: '60%',
        backgroundColor: Theme.colors.border,
    },
    quickActionsSection: {
        paddingHorizontal: Theme.spacing.lg,
        paddingBottom: Theme.spacing.xl,
    },
    mainActionButton: {
        backgroundColor: Theme.colors.surface,
        padding: Theme.spacing.md,
        borderRadius: Theme.borderRadius.md,
        alignItems: 'center',
        borderWidth: 1,
        borderColor: Theme.colors.border,
    },
    mainActionText: {
        color: Theme.colors.primary,
        fontWeight: '600',
    },
    loadingContainer: {
        padding: Theme.spacing.xl,
        alignItems: 'center',
    }
});

export default DashboardScreen;
