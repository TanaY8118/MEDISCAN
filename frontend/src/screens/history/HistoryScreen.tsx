import React, { useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { Theme } from '../../constants/Theme';
import { useHistory } from '../../hooks/useHistory';
import { format, subDays, startOfMonth, endOfMonth, eachDayOfInterval, isSameMonth } from 'date-fns';

const HistoryScreen = () => {
    const { events, heatmapData, stats, loading, refresh } = useHistory(30);

    const renderHeatmap = () => {
        const today = new Date();
        const monthStart = startOfMonth(today);
        const monthEnd = endOfMonth(today);
        const daysInMonth = eachDayOfInterval({ start: monthStart, end: monthEnd });

        return (
            <View style={styles.heatmapContainer}>
                <Text style={styles.monthTitle}>{format(today, 'MMMM yyyy')}</Text>
                <View style={styles.grid}>
                    {daysInMonth.map((date, idx) => {
                        const dateKey = format(date, 'yyyy-MM-dd');
                        const status = heatmapData[dateKey] || 'empty';

                        return (
                            <View
                                key={idx}
                                style={[
                                    styles.dayBox,
                                    status === 'success' && styles.daySuccess,
                                    status === 'missed' && styles.dayMissed
                                ]}
                            />
                        );
                    })}
                </View>
                <View style={styles.legend}>
                    <View style={styles.legendItem}><View style={[styles.dayBox, styles.daySuccess]} /><Text style={styles.legendText}>Taken</Text></View>
                    <View style={styles.legendItem}><View style={[styles.dayBox, styles.dayMissed]} /><Text style={styles.legendText}>Missed</Text></View>
                    <View style={styles.legendItem}><View style={[styles.dayBox]} /><Text style={styles.legendText}>None/Upcoming</Text></View>
                </View>
            </View>
        );
    };

    if (loading && events.length === 0) {
        return (
            <View style={styles.centered}>
                <ActivityIndicator size="large" color={Theme.colors.primary} />
            </View>
        );
    }

    return (
        <View style={styles.container}>
            <ScrollView
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl refreshing={loading} onRefresh={refresh} tintColor={Theme.colors.primary} />
                }
            >
                <View style={styles.summaryRow}>
                    <View style={styles.statCard}>
                        <Text style={styles.statLabel}>Adherence</Text>
                        <Text style={styles.statValue}>{stats.rate}%</Text>
                    </View>
                    <View style={styles.statCard}>
                        <Text style={styles.statLabel}>Taken</Text>
                        <Text style={[styles.statValue, { color: Theme.colors.success }]}>{stats.taken}</Text>
                    </View>
                    <View style={styles.statCard}>
                        <Text style={styles.statLabel}>Missed</Text>
                        <Text style={[styles.statValue, { color: Theme.colors.danger }]}>{stats.missed}</Text>
                    </View>
                </View>

                <View style={styles.section}>
                    <Text style={styles.sectionTitle}>Adherence Overview</Text>
                    {renderHeatmap()}
                </View>

                <View style={styles.section}>
                    <View style={styles.sectionHeader}>
                        <Text style={styles.sectionTitle}>Activity Log</Text>
                        <TouchableOpacity>
                            <Text style={styles.exportText}>Export CSV</Text>
                        </TouchableOpacity>
                    </View>

                    {events.map((event, idx) => (
                        <View key={`${event.timestamp}-${idx}`} style={styles.logCard}>
                            <View style={styles.logLeft}>
                                <Text style={styles.logTime}>
                                    {format(new Date(event.timestamp), 'HH:mm')}
                                </Text>
                                <Text style={styles.logDate}>
                                    {format(new Date(event.timestamp), 'MMM dd')}
                                </Text>
                            </View>
                            <View style={styles.logCenter}>
                                <Text style={styles.logName}>{event.medicineName}</Text>
                                <Text style={[
                                    styles.logStatus,
                                    { color: event.eventType.includes('TAKEN') ? Theme.colors.success : Theme.colors.textMuted }
                                ]}>
                                    {event.description}
                                </Text>
                            </View>
                            <Text style={styles.logPill}>
                                {event.eventType.includes('STOCK') ? '📦' : '💊'}
                            </Text>
                        </View>
                    ))}

                    {events.length === 0 && (
                        <View style={styles.emptyLog}>
                            <Text style={styles.emptyText}>No activity logs found.</Text>
                        </View>
                    )}
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
    centered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    summaryRow: {
        flexDirection: 'row',
        padding: Theme.spacing.lg,
        justifyContent: 'space-between',
    },
    statCard: {
        backgroundColor: Theme.colors.surface,
        padding: Theme.spacing.md,
        borderRadius: Theme.borderRadius.md,
        flex: 1,
        marginHorizontal: 4,
        alignItems: 'center',
        ...Theme.shadows.soft,
    },
    statLabel: {
        fontSize: 10,
        color: Theme.colors.textMuted,
        textTransform: 'uppercase',
        fontWeight: 'bold',
        marginBottom: 4,
    },
    statValue: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.primary,
    },
    section: {
        paddingHorizontal: Theme.spacing.lg,
        marginBottom: Theme.spacing.xl,
    },
    sectionHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: Theme.spacing.md,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        fontFamily: Theme.typography.header,
        marginBottom: Theme.spacing.md,
    },
    exportText: {
        color: Theme.colors.accent,
        fontWeight: 'bold',
        fontSize: 14,
    },
    heatmapContainer: {
        backgroundColor: Theme.colors.surface,
        padding: Theme.spacing.lg,
        borderRadius: Theme.borderRadius.md,
        ...Theme.shadows.soft,
    },
    monthTitle: {
        fontSize: 14,
        fontWeight: '600',
        color: Theme.colors.text,
        marginBottom: Theme.spacing.md,
    },
    grid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
    },
    dayBox: {
        width: 14,
        height: 14,
        borderRadius: 3,
        backgroundColor: Theme.colors.background,
        margin: 3,
    },
    daySuccess: {
        backgroundColor: Theme.colors.success,
    },
    dayMissed: {
        backgroundColor: Theme.colors.danger,
    },
    legend: {
        flexDirection: 'row',
        marginTop: Theme.spacing.md,
        flexWrap: 'wrap',
    },
    legendItem: {
        flexDirection: 'row',
        alignItems: 'center',
        marginRight: Theme.spacing.md,
        marginBottom: 4,
    },
    legendText: {
        fontSize: 10,
        color: Theme.colors.textMuted,
        marginLeft: 4,
    },
    logCard: {
        flexDirection: 'row',
        backgroundColor: Theme.colors.surface,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.md,
        marginBottom: Theme.spacing.sm,
        alignItems: 'center',
        ...Theme.shadows.soft,
    },
    logLeft: {
        width: 60,
    },
    logTime: {
        fontSize: 13,
        fontWeight: 'bold',
        color: Theme.colors.primary,
    },
    logDate: {
        fontSize: 10,
        color: Theme.colors.textMuted,
    },
    logCenter: {
        flex: 1,
        marginLeft: Theme.spacing.md,
    },
    logName: {
        fontSize: 16,
        fontWeight: '600',
        color: Theme.colors.text,
    },
    logStatus: {
        fontSize: 12,
        marginTop: 2,
    },
    logPill: {
        fontSize: 20,
        opacity: 0.5,
    },
    emptyLog: {
        padding: Theme.spacing.xl,
        alignItems: 'center',
    },
    emptyText: {
        color: Theme.colors.textMuted,
    }
});

export default HistoryScreen;

