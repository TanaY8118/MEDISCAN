import React, { useMemo } from 'react';
import { View, Text, FlatList, StyleSheet, TouchableOpacity, ActivityIndicator, RefreshControl } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useInventory } from '../../hooks/useInventory';
import { useMedicines } from '../../hooks/useMedicines';
import { Theme } from '../../constants/Theme';

const InventoryListScreen = ({ navigation }: any) => {
    const { inventory, loading: loadingInv, refresh: refreshInv } = useInventory();
    const { medicines, loading: loadingMed, refresh: refreshMed } = useMedicines();

    const isLoading = loadingInv || loadingMed;

    useFocusEffect(
        React.useCallback(() => {
            refreshInv();
            refreshMed();
        }, [])
    );

    const getMedicineName = (id: string) => {
        const med = medicines.find(m => m.id === id);
        return med ? med.name : 'Unknown Medicine';
    };

    const criticalItems = useMemo(() => {
        return inventory.filter(item => item.quantity <= item.lowStockThreshold);
    }, [inventory]);

    const getStockColor = (qty: number, threshold: number) => {
        if (qty === 0) return Theme.colors.danger;
        if (qty <= threshold) return Theme.colors.accent;
        return Theme.colors.success;
    };

    const renderItem = ({ item }: any) => {
        const stockPercent = Math.min((item.quantity / (item.lowStockThreshold * 2)) * 100, 100);
        const statusColor = getStockColor(item.quantity, item.lowStockThreshold);

        return (
            <TouchableOpacity
                style={styles.card}
                onPress={() => navigation.navigate('AdjustStock', { inventoryItem: item, medicineName: getMedicineName(item.medicineId) })}
            >
                <View style={styles.cardHeader}>
                    <Text style={styles.medName}>{getMedicineName(item.medicineId)}</Text>
                    <View style={[styles.pillBadge, { backgroundColor: statusColor + '20' }]}>
                        <Text style={[styles.pillBadgeText, { color: statusColor }]}>
                            {item.quantity <= item.lowStockThreshold ? 'Low Stock' : 'In Stock'}
                        </Text>
                    </View>
                </View>

                <View style={styles.progressContainer}>
                    <View style={styles.progressBarBg}>
                        <View style={[styles.progressBarFill, { width: `${stockPercent}%`, backgroundColor: statusColor }]} />
                    </View>
                    <View style={styles.progressLabelRow}>
                        <Text style={styles.qtyText}>{item.quantity} units left</Text>
                        <Text style={styles.thresholdText}>Threshold: {item.lowStockThreshold}</Text>
                    </View>
                </View>

                <View style={styles.cardFooter}>
                    <Text style={styles.updatedText}>Last updated: Today</Text>
                    <TouchableOpacity
                        style={styles.restockButton}
                        onPress={() => {/* dummy restock flow */ }}
                    >
                        <Text style={styles.restockText}>Restock</Text>
                    </TouchableOpacity>
                </View>
            </TouchableOpacity>
        );
    };

    return (
        <View style={styles.container}>
            {criticalItems.length > 0 && (
                <View style={styles.alertHeader}>
                    <Text style={styles.alertTitle}>Stock Attention Required</Text>
                    <Text style={styles.alertSub}>{criticalItems.length} items are running low.</Text>
                </View>
            )}

            <FlatList
                data={inventory}
                keyExtractor={(item: any) => item.id.toString()}
                renderItem={renderItem}
                contentContainerStyle={styles.list}
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={() => { refreshInv(); refreshMed(); }} tintColor={Theme.colors.primary} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyContainer}>
                        <Text style={styles.emptyIcon}>📦</Text>
                        <Text style={styles.emptyText}>No inventory records found.</Text>
                    </View>
                }
            />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    alertHeader: {
        backgroundColor: '#FEF3C7',
        paddingHorizontal: Theme.spacing.lg,
        paddingVertical: Theme.spacing.md,
        borderBottomWidth: 1,
        borderBottomColor: Theme.colors.accent,
    },
    alertTitle: {
        color: '#92400E',
        fontWeight: 'bold',
        fontSize: 16,
    },
    alertSub: {
        color: '#92400E',
        fontSize: 12,
        marginTop: 2,
    },
    list: {
        padding: Theme.spacing.lg,
    },
    card: {
        backgroundColor: Theme.colors.surface,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.lg,
        marginBottom: Theme.spacing.md,
        ...Theme.shadows.soft,
        borderWidth: 1,
        borderColor: 'rgba(0,0,0,0.02)',
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: Theme.spacing.lg,
    },
    medName: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        fontFamily: Theme.typography.header,
    },
    pillBadge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 12,
    },
    pillBadgeText: {
        fontSize: 10,
        fontWeight: 'bold',
        textTransform: 'uppercase',
    },
    progressContainer: {
        marginBottom: Theme.spacing.lg,
    },
    progressBarBg: {
        height: 8,
        backgroundColor: Theme.colors.background,
        borderRadius: 4,
        overflow: 'hidden',
        marginBottom: 8,
    },
    progressBarFill: {
        height: '100%',
        borderRadius: 4,
    },
    progressLabelRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    qtyText: {
        fontSize: 14,
        fontWeight: '600',
        color: Theme.colors.text,
    },
    thresholdText: {
        fontSize: 12,
        color: Theme.colors.textMuted,
    },
    cardFooter: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderTopWidth: 1,
        borderTopColor: Theme.colors.border,
        paddingTop: Theme.spacing.md,
    },
    updatedText: {
        fontSize: 12,
        color: Theme.colors.textMuted,
        fontStyle: 'italic',
    },
    restockButton: {
        backgroundColor: Theme.colors.primary + '10',
        paddingHorizontal: Theme.spacing.md,
        paddingVertical: 6,
        borderRadius: 8,
    },
    restockText: {
        color: Theme.colors.primary,
        fontWeight: 'bold',
        fontSize: 12,
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
});

export default InventoryListScreen;
