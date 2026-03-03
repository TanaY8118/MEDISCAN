import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import ReminderListScreen from '../screens/reminder/ReminderListScreen';
import AddReminderScreen from '../screens/reminder/AddReminderScreen';

const Stack = createNativeStackNavigator();

const ReminderNavigator = () => {
    return (
        <Stack.Navigator>
            <Stack.Screen
                name="ReminderList"
                component={ReminderListScreen}
                options={{ title: 'Reminders' }}
            />
            <Stack.Screen
                name="AddReminder"
                component={AddReminderScreen}
                options={{ title: 'Set Reminder' }}
            />
        </Stack.Navigator>
    );
};

export default ReminderNavigator;
