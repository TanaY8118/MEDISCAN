import React, { useState } from 'react';
import { View, Text, Button, StyleSheet, ActivityIndicator } from 'react-native';

const ScanScreen = ({ navigation }: any) => {
    const [scanning, setScanning] = useState(false);

    const simulateScan = () => {
        setScanning(true);
        // Simulate camera delay
        setTimeout(() => {
            setScanning(false);
            // Mock Barcode
            const mockBarcode = '8901234567890';
            // Navigate to Add Medicine with pre-filled barcode
            // Note: We need to make sure MedicineNavigator handles this, or we navigate to the AddMedicine screen directly if accessible.
            // Since AddMedicine is inside MedicineStack, we navigate there.
            navigation.navigate('MedicinesStack', {
                screen: 'AddMedicine',
                params: { scannedBarcode: mockBarcode }
            });
        }, 2000);
    };

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Scan Barcode</Text>
            <View style={styles.viewport}>
                <Text style={styles.instruction}>Align code within frame</Text>
                {scanning && <ActivityIndicator size="large" color="#fff" style={styles.loader} />}
            </View>
            <Button title={scanning ? "Scanning..." : "Simulate Scan"} onPress={simulateScan} disabled={scanning} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
        justifyContent: 'center',
        backgroundColor: '#000',
    },
    title: {
        fontSize: 24,
        color: 'white',
        textAlign: 'center',
        marginBottom: 20,
    },
    viewport: {
        height: 250,
        borderWidth: 2,
        borderColor: '#2196f3',
        borderRadius: 10,
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: 30,
        backgroundColor: 'rgba(255,255,255,0.1)',
    },
    instruction: {
        color: '#ccc',
    },
    loader: {
        marginTop: 20,
    }
});

export default ScanScreen;
