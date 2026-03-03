import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import InventoryListScreen from '../screens/inventory/InventoryListScreen';
import AdjustStockScreen from '../screens/inventory/AdjustStockScreen';

const Stack = createNativeStackNavigator();

const InventoryNavigator = () => {
    return (
        <Stack.Navigator>
            <Stack.Screen
                name="InventoryList"
                component={InventoryListScreen}
                options={{ title: 'Inventory' }}
            />
            <Stack.Screen
                name="AdjustStock"
                component={AdjustStockScreen}
                options={{ title: 'Manage Stock' }}
            />
        </Stack.Navigator>
    );
};

export default InventoryNavigator;
