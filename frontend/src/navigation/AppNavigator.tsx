import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import DashboardScreen from '../screens/home/DashboardScreen';
import MedicineNavigator from './MedicineNavigator';
import InventoryNavigator from './InventoryNavigator';
import ReminderNavigator from './ReminderNavigator';
import HistoryScreen from '../screens/history/HistoryScreen';
import SettingsScreen from '../screens/settings/SettingsScreen';
import ScanScreen from '../screens/common/ScanScreen';
import { View, Text, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { Theme } from '../constants/Theme';

const Tab = createBottomTabNavigator();

const AppNavigator = () => {
    return (
        <Tab.Navigator
            screenOptions={{
                tabBarStyle: styles.tabBar,
                tabBarActiveTintColor: Theme.colors.primary,
                tabBarInactiveTintColor: Theme.colors.textMuted,
                headerStyle: styles.header,
                headerTitleStyle: styles.headerTitle,
                tabBarLabelStyle: styles.tabBarLabel,
            }}
        >
            <Tab.Screen
                name="Dashboard"
                component={DashboardScreen}
                options={{
                    title: 'Home',
                    tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>🏠</Text>
                }}
            />
            <Tab.Screen
                name="MedicinesStack"
                component={MedicineNavigator}
                options={{
                    title: 'Meds',
                    headerShown: false,
                    tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>💊</Text>
                }}
            />
            <Tab.Screen
                name="Scan"
                component={ScanScreen}
                options={{
                    tabBarButton: (props) => (
                        <TouchableOpacity {...props} style={styles.scanButtonContainer}>
                            <View style={styles.scanButton}>
                                <Text style={styles.scanButtonText}>+</Text>
                            </View>
                        </TouchableOpacity>
                    ),
                }}
            />
            <Tab.Screen
                name="ReminderStack"
                component={ReminderNavigator}
                options={{
                    title: 'Reminders',
                    headerShown: false,
                    tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>⏰</Text>
                }}
            />
            <Tab.Screen
                name="InventoryStack"
                component={InventoryNavigator}
                options={{
                    title: 'Stock',
                    headerShown: false,
                    tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>📦</Text>
                }}
            />
            <Tab.Screen
                name="History"
                component={HistoryScreen}
                options={{
                    title: 'History',
                    tabBarIcon: ({ color }) => <Text style={{ color, fontSize: 20 }}>📊</Text>
                }}
            />
        </Tab.Navigator>
    );
};

const styles = StyleSheet.create({
    tabBar: {
        height: 75,
        backgroundColor: Theme.colors.surface,
        borderTopWidth: 1,
        borderTopColor: Theme.colors.border,
        paddingBottom: Platform.OS === 'ios' ? 25 : 12,
        paddingTop: 10,
        ...Theme.shadows.soft,
    },
    tabBarLabel: {
        fontSize: 10,
        fontWeight: '600',
        marginTop: 4,
    },
    header: {
        backgroundColor: Theme.colors.surface,
        elevation: 0,
        shadowOpacity: 0,
        borderBottomWidth: 1,
        borderBottomColor: Theme.colors.border,
    },
    headerTitle: {
        color: Theme.colors.primary,
        fontWeight: 'bold',
        fontSize: 20,
    },
    scanButtonContainer: {
        top: -24,
        justifyContent: 'center',
        alignItems: 'center',
    },
    scanButton: {
        width: 60,
        height: 60,
        borderRadius: Theme.borderRadius.round,
        backgroundColor: Theme.colors.accent,
        justifyContent: 'center',
        alignItems: 'center',
        ...Theme.shadows.deep,
    },
    scanButtonText: {
        color: 'white',
        fontSize: 32,
        fontWeight: '300',
    },
});

export default AppNavigator;
