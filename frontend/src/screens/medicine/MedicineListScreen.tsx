import React, { useState, useMemo } from 'react';
import { View, Text, FlatList, StyleSheet, TouchableOpacity, ActivityIndicator, RefreshControl, TextInput } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useMedicines } from '../../hooks/useMedicines';
import { Theme } from '../../constants/Theme';

const MedicineListScreen = ({ navigation }: any) => {
    const { medicines, loading, refresh } = useMedicines();
    const [searchQuery, setSearchQuery] = useState('');

    useFocusEffect(
        React.useCallback(() => {
            refresh();
        }, [])
    );

    const filteredMedicines = useMemo(() => {
        return medicines.filter(m =>
            m.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            m.description?.toLowerCase().includes(searchQuery.toLowerCase())
        );
    }, [medicines, searchQuery]);

    const getStatusColor = (status: string) => {
        switch (status?.toLowerCase()) {
            case 'active': return Theme.colors.success;
            case 'paused': return Theme.colors.accent;
            case 'finished': return Theme.colors.textMuted;
            default: return Theme.colors.primary;
        }
    };

    const renderItem = ({ item }: any) => (
        <TouchableOpacity
            style={styles.card}
            onPress={() => navigation.navigate('MedicineDetails', { id: item.id })}
        >
            <View style={styles.cardHeader}>
                <View style={[styles.statusBadge, { backgroundColor: getStatusColor('Active') + '20' }]}>
                    <View style={[styles.statusDot, { backgroundColor: getStatusColor('Active') }]} />
                    <Text style={[styles.statusText, { color: getStatusColor('Active') }]}>Active</Text>
                </View>
                <Text style={styles.stockBadge}>In Stock</Text>
            </View>

            <View style={styles.content}>
                <Text style={styles.pillIcon}>💊</Text>
                <View style={styles.details}>
                    <Text style={styles.name}>{item.name}</Text>
                    <Text style={styles.dosage}>500mg • Twice daily</Text>
                </View>
            </View>

            <View style={styles.cardFooter}>
                <Text style={styles.subText}>{item.description || 'No description provided.'}</Text>
            </View>
        </TouchableOpacity>
    );

    return (
        <View style={styles.container}>
            <View style={styles.searchContainer}>
                <TextInput
                    style={styles.searchInput}
                    placeholder="Search medicines..."
                    value={searchQuery}
                    onChangeText={setSearchQuery}
                    placeholderTextColor={Theme.colors.textMuted}
                />
            </View>

            <FlatList
                data={filteredMedicines}
                keyExtractor={(item: any) => item.id.toString()}
                renderItem={renderItem}
                contentContainerStyle={styles.list}
                showsVerticalScrollIndicator={false}
                refreshControl={
                    <RefreshControl refreshing={loading} onRefresh={refresh} tintColor={Theme.colors.primary} />
                }
                ListEmptyComponent={
                    <View style={styles.emptyContainer}>
                        <Text style={styles.pillIconLarge}>🏥</Text>
                        <Text style={styles.emptyText}>
                            {searchQuery ? 'No matching medicines found.' : 'Your medicine cabinet is empty.'}
                        </Text>
                    </View>
                }
            />

            <TouchableOpacity
                style={styles.fab}
                onPress={() => navigation.navigate('AddMedicine')}
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
    searchContainer: {
        paddingHorizontal: Theme.spacing.lg,
        paddingVertical: Theme.spacing.md,
        backgroundColor: Theme.colors.surface,
        borderBottomWidth: 1,
        borderBottomColor: Theme.colors.border,
    },
    searchInput: {
        backgroundColor: Theme.colors.background,
        padding: Theme.spacing.md,
        borderRadius: Theme.borderRadius.md,
        color: Theme.colors.text,
        fontSize: 16,
    },
    list: {
        padding: Theme.spacing.lg,
        paddingBottom: 100,
    },
    card: {
        backgroundColor: Theme.colors.surface,
        borderRadius: Theme.borderRadius.md,
        padding: Theme.spacing.md,
        marginBottom: Theme.spacing.md,
        ...Theme.shadows.soft,
        borderWidth: 1,
        borderColor: 'rgba(0,0,0,0.02)',
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: Theme.spacing.md,
    },
    statusBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 12,
    },
    statusDot: {
        width: 6,
        height: 6,
        borderRadius: 3,
        marginRight: 6,
    },
    statusText: {
        fontSize: 10,
        fontWeight: 'bold',
        textTransform: 'uppercase',
    },
    stockBadge: {
        fontSize: 10,
        color: Theme.colors.textMuted,
        fontWeight: '600',
    },
    content: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: Theme.spacing.md,
    },
    pillIcon: {
        fontSize: 32,
        marginRight: Theme.spacing.md,
    },
    details: {
        flex: 1,
    },
    name: {
        fontSize: 18,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        fontFamily: Theme.typography.header,
    },
    dosage: {
        fontSize: 14,
        color: Theme.colors.textMuted,
        marginTop: 2,
    },
    cardFooter: {
        borderTopWidth: 1,
        borderTopColor: Theme.colors.border,
        paddingTop: Theme.spacing.sm,
    },
    subText: {
        fontSize: 12,
        color: Theme.colors.textMuted,
        fontStyle: 'italic',
    },
    emptyContainer: {
        alignItems: 'center',
        marginTop: 100,
    },
    pillIconLarge: {
        fontSize: 64,
        marginBottom: Theme.spacing.md,
        opacity: 0.2,
    },
    emptyText: {
        color: Theme.colors.textMuted,
        fontSize: 16,
        textAlign: 'center',
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
});

export default MedicineListScreen;
