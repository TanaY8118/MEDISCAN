import React, { useContext } from 'react';
import { View, ActivityIndicator } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { AuthProvider, AuthContext } from './src/context/AuthContext';
import AuthNavigator from './src/navigation/AuthNavigator';
import AppNavigator from './src/navigation/AppNavigator';

// Placeholder Home Screen
import { Text, Button } from 'react-native';
const HomeScreen = ({ navigation }: any) => {
    const { logout } = useContext(AuthContext);
    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <Text>Welcome to Smart Medicine System!</Text>
            <Button title="Logout" onPress={logout} />
        </View>
    );
};

const Stack = createNativeStackNavigator();

const AppNav = () => {
    const { isLoading, userToken } = useContext(AuthContext);

    if (isLoading) {
        return (
            <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
                <ActivityIndicator size="large" />
            </View>
        );
    }

    return (
        <NavigationContainer>
            {userToken !== null ? (
                <AppNavigator />
            ) : (
                <AuthNavigator />
            )}
        </NavigationContainer>
    );
};

function App(): JSX.Element {
    return (
        <AuthProvider>
            <AppNav />
        </AuthProvider>
    );
}

export default App;
