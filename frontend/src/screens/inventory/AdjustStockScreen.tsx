import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert } from 'react-native';
import { adjustStock, updateInventory } from '../../api/inventory.api';

const AdjustStockScreen = ({ route, navigation }: any) => {
    const { inventoryItem, medicineName } = route.params;
    const [adjustment, setAdjustment] = useState('');
    const [threshold, setThreshold] = useState(inventoryItem.lowStockThreshold.toString());
    const [submitting, setSubmitting] = useState(false);

    const handleAdjust = async (isAddition: boolean) => {
        if (!adjustment) {
            Alert.alert('Error', 'Please enter an amount');
            return;
        }
        const amount = parseInt(adjustment);
        if (isNaN(amount) || amount <= 0) {
            Alert.alert('Error', 'Invalid amount');
            return;
        }

        setSubmitting(true);
        try {
            const finalAmount = isAddition ? amount : -amount;
            await adjustStock(inventoryItem.medicineId, finalAmount);
            Alert.alert('Success', 'Stock adjusted');
            navigation.goBack();
        } catch (error) {
            Alert.alert('Error', 'Failed to adjust stock');
        } finally {
            setSubmitting(false);
        }
    };

    const handleUpdateThreshold = async () => {
        const newThreshold = parseInt(threshold);
        if (isNaN(newThreshold) || newThreshold < 0) {
            Alert.alert('Error', 'Invalid threshold');
            return;
        }

        setSubmitting(true);
        try {
            await updateInventory({
                medicineId: inventoryItem.medicineId,
                quantity: inventoryItem.quantity, // Quantity doesn't change here, but API might require it or we can just send it same
                lowStockThreshold: newThreshold
            });
            Alert.alert('Success', 'Threshold updated');
            navigation.goBack();
        } catch (error) {
            Alert.alert('Error', 'Failed to update threshold');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>{medicineName}</Text>
            <Text style={styles.currentQty}>Current Stock: <Text style={{ fontWeight: 'bold' }}>{inventoryItem.quantity}</Text></Text>

            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Adjust Stock</Text>
                <TextInput
                    style={styles.input}
                    placeholder="Amount"
                    value={adjustment}
                    onChangeText={setAdjustment}
                    keyboardType="numeric"
                />
                <View style={styles.row}>
                    <View style={styles.btn}>
                        <Button title="Add (+)" onPress={() => handleAdjust(true)} disabled={submitting} />
                    </View>
                    <View style={styles.btn}>
                        <Button title="Remove (-)" onPress={() => handleAdjust(false)} color="#ef5350" disabled={submitting} />
                    </View>
                </View>
            </View>

            <View style={styles.section}>
                <Text style={styles.sectionTitle}>Settings</Text>
                <Text style={styles.label}>Low Stock Threshold</Text>
                <TextInput
                    style={styles.input}
                    value={threshold}
                    onChangeText={setThreshold}
                    keyboardType="numeric"
                />
                <Button title="Update Threshold" onPress={handleUpdateThreshold} color="#757575" disabled={submitting} />
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
        backgroundColor: '#fff',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 5,
        textAlign: 'center',
    },
    currentQty: {
        fontSize: 18,
        textAlign: 'center',
        marginBottom: 30,
        color: '#555',
    },
    section: {
        marginBottom: 30,
        padding: 15,
        backgroundColor: '#f9f9f9',
        borderRadius: 10,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: 'bold',
        marginBottom: 10,
    },
    input: {
        borderWidth: 1,
        borderColor: '#ddd',
        padding: 10,
        borderRadius: 5,
        marginBottom: 10,
        backgroundColor: 'white',
    },
    row: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    btn: {
        width: '48%',
    },
    label: {
        marginBottom: 5,
        color: '#666',
    }
});

export default AdjustStockScreen;
