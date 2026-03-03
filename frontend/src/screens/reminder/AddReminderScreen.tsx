import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert, Picker } from 'react-native'; // Note: Picker might need @react-native-picker/picker in real app, but using native generic for now or simplified selection
import { createReminder } from '../../api/reminder.api';
import { getMedicines } from '../../api/medicine.api';

// Simplified Picker for prototype using text input for medicine ID or name logic
// Real app should use proper Dropdown/Modal
// For this prototype, let's fetch medicines and assume we can just type the name or select by index if we had a proper picker. 
// I'll implement a simple list selection mock or just fetch medicines and let user pick from a list in a modal?
// Let's stick to a simpler approach: Enter Medicine Name (search implementation is complex) or just select from fetched list if small.
// I'll use a simple ScrollView with TouchableOpacity as a custom picker.

const AddReminderScreen = ({ navigation }: any) => {
    const [medicines, setMedicines] = useState<any[]>([]);
    const [selectedMedicineId, setSelectedMedicineId] = useState<string | null>(null);
    const [dateStr, setDateStr] = useState(''); // Simple text input for date YYYY-MM-DDTHH:MM:SS for prototype
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const loadMeds = async () => {
            const data = await getMedicines();
            setMedicines(data);
        };
        loadMeds();
    }, []);

    const handleSubmit = async () => {
        if (!selectedMedicineId) {
            Alert.alert('Error', 'Please select a medicine');
            return;
        }
        // Basic date validation for prototype
        // Expecting ISO string or similar that backend parses. Backend uses LocalDateTime?
        // Backend ReminderRequest has `LocalDateTime reminderTime`.
        // JSON default format for LocalDateTime is ISO-8601.
        // Let's assume user enters "2024-12-25T10:00:00"

        if (!dateStr.includes('T')) {
            Alert.alert('Error', 'Format: YYYY-MM-DDTHH:MM:SS');
            return;
        }

        setSubmitting(true);
        try {
            await createReminder({
                medicineId: selectedMedicineId,
                reminderTime: dateStr
            });
            Alert.alert('Success', 'Reminder set!');
            navigation.goBack();
        } catch (error) {
            Alert.alert('Error', 'Failed to set reminder');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <View style={styles.container}>
            <Text style={styles.label}>Select Medicine</Text>
            <View style={styles.medList}>
                {medicines.map(med => (
                    <Text
                        key={med.id}
                        style={[styles.medItem, selectedMedicineId === med.id && styles.medSelected]}
                        onPress={() => setSelectedMedicineId(med.id)}
                    >
                        {med.name}
                    </Text>
                ))}
            </View>

            <Text style={styles.label}>Reminder Time (YYYY-MM-DDTHH:MM:SS)</Text>
            <TextInput
                style={styles.input}
                value={dateStr}
                onChangeText={setDateStr}
                placeholder="2024-12-31T08:00:00"
            />

            <Button title={submitting ? "Saving..." : "Set Reminder"} onPress={handleSubmit} disabled={submitting} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        padding: 20,
        backgroundColor: '#fff',
        flex: 1,
    },
    label: {
        fontSize: 16,
        fontWeight: '600',
        marginBottom: 10,
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
    medList: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        marginBottom: 20,
    },
    medItem: {
        padding: 10,
        margin: 5,
        borderWidth: 1,
        borderColor: '#eee',
        borderRadius: 20,
        color: '#555',
    },
    medSelected: {
        backgroundColor: '#e3f2fd',
        borderColor: '#2196f3',
        color: '#2196f3',
        fontWeight: 'bold',
    }
});

export default AddReminderScreen;
