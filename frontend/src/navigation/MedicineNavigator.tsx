import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import MedicineListScreen from '../screens/medicine/MedicineListScreen';
import AddMedicineScreen from '../screens/medicine/AddMedicineScreen';

const Stack = createNativeStackNavigator();

const MedicineNavigator = () => {
    return (
        <Stack.Navigator>
            <Stack.Screen
                name="MedicineList"
                component={MedicineListScreen}
                options={{ title: 'My Medicines' }}
            />
            <Stack.Screen
                name="AddMedicine"
                component={AddMedicineScreen}
                options={{ title: 'Add Medicine' }}
            />
        </Stack.Navigator>
    );
};

export default MedicineNavigator;
