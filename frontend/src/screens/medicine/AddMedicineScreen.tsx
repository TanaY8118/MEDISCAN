```
import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, ScrollView } from 'react-native';
import { createMedicine } from '../../api/medicine.api';

const AddMedicineScreen = ({ navigation, route }: any) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [barcode, setBarcode] = useState('');
    const [submitting, setSubmitting] = useState(false);

    React.useEffect(() => {
        if (route.params?.scannedBarcode) {
            setBarcode(route.params.scannedBarcode);
        }
    }, [route.params]);

    const handleSubmit = async () => {
        if (!name) {
            Alert.alert('Validation Error', 'Medicine Name is required');
            return;
        }

        setSubmitting(true);
        try {
            await createMedicine({ name, description, barcode });
            Alert.alert('Success', 'Medicine added successfully');
            navigation.goBack();
        } catch (error) {
            Alert.alert('Error', 'Failed to add medicine');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.label}>Medicine Name *</Text>
            <TextInput
                style={styles.input}
                value={name}
                onChangeText={setName}
                placeholder="e.g. Paracetamol"
            />

            <Text style={styles.label}>Description</Text>
            <TextInput
                style={[styles.input, styles.textArea]}
                value={description}
                onChangeText={setDescription}
                placeholder="Dosage, instructions, etc."
                multiline
                numberOfLines={4}
            />

            <Text style={styles.label}>Barcode (Optional)</Text>
            <TextInput
                style={styles.input}
                value={barcode}
                onChangeText={setBarcode}
                placeholder="Scan or enter barcode"
                keyboardType="numeric"
            />

            <View style={styles.buttonContainer}>
                <Button title={submitting ? "Saving..." : "Save Medicine"} onPress={handleSubmit} disabled={submitting} />
            </View>
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        padding: 20,
        backgroundColor: '#fff',
        flexGrow: 1,
    },
    label: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 5,
        color: '#333',
    },
    input: {
        borderWidth: 1,
        borderColor: '#ddd',
        borderRadius: 8,
        padding: 10,
        marginBottom: 20,
        fontSize: 16,
        backgroundColor: '#fafafa',
    },
    textArea: {
        height: 100,
        textAlignVertical: 'top',
    },
    buttonContainer: {
        marginTop: 10,
    },
});

export default AddMedicineScreen;
