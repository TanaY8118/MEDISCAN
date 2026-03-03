import React, { useContext, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, SafeAreaView } from 'react-native';
import { AuthContext } from '../../context/AuthContext';
import { Theme } from '../../constants/Theme';

const LoginScreen = ({ navigation }: any) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const { login, isLoading } = useContext(AuthContext);

    const handleLogin = async () => {
        if (!username || !password) {
            Alert.alert('Error', 'Please enter both username and password');
            return;
        }
        try {
            await login({ username, password });
        } catch (e) {
            Alert.alert('Login Failed', 'Invalid credentials');
        }
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <View style={styles.container}>
                <View style={styles.headerContainer}>
                    <Text style={styles.title}>MediScan</Text>
                    <Text style={styles.subtitle}>Sign in to manage your health</Text>
                </View>

                <View style={styles.formContainer}>
                    <Text style={styles.label}>Username</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="Enter your username"
                        value={username}
                        onChangeText={setUsername}
                        autoCapitalize="none"
                    />

                    <Text style={styles.label}>Password</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="Enter your password"
                        value={password}
                        onChangeText={setPassword}
                        secureTextEntry
                    />

                    <TouchableOpacity
                        style={[styles.button, isLoading && styles.buttonDisabled]}
                        onPress={handleLogin}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <ActivityIndicator color="#fff" />
                        ) : (
                            <Text style={styles.buttonText}>Login</Text>
                        )}
                    </TouchableOpacity>

                    <View style={styles.footer}>
                        <Text style={styles.footerText}>Don't have an account? </Text>
                        <TouchableOpacity onPress={() => navigation.navigate('Register')}>
                            <Text style={styles.linkText}>Create Account</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </View>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    container: {
        flex: 1,
        padding: Theme.spacing.lg,
        justifyContent: 'center',
    },
    headerContainer: {
        marginBottom: Theme.spacing.xl,
        alignItems: 'center',
    },
    title: {
        fontSize: 32,
        fontWeight: 'bold',
        color: Theme.colors.primary,
        marginBottom: Theme.spacing.xs,
    },
    subtitle: {
        fontSize: 16,
        color: Theme.colors.textSecondary,
    },
    formContainer: {
        backgroundColor: Theme.colors.surface,
        padding: Theme.spacing.lg,
        borderRadius: Theme.borderRadius.lg,
        elevation: 4,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    label: {
        fontSize: 14,
        fontWeight: '600',
        color: Theme.colors.text,
        marginBottom: Theme.spacing.xs,
    },
    input: {
        height: 50,
        borderColor: Theme.colors.border,
        borderWidth: 1,
        marginBottom: Theme.spacing.md,
        paddingHorizontal: Theme.spacing.md,
        borderRadius: Theme.borderRadius.md,
        backgroundColor: '#fafafa',
    },
    button: {
        backgroundColor: Theme.colors.primary,
        height: 50,
        borderRadius: Theme.borderRadius.md,
        justifyContent: 'center',
        alignItems: 'center',
        marginTop: Theme.spacing.sm,
    },
    buttonDisabled: {
        backgroundColor: Theme.colors.textSecondary,
    },
    buttonText: {
        color: 'white',
        fontSize: 18,
        fontWeight: 'bold',
    },
    footer: {
        flexDirection: 'row',
        justifyContent: 'center',
        marginTop: Theme.spacing.lg,
    },
    footerText: {
        color: Theme.colors.textSecondary,
    },
    linkText: {
        color: Theme.colors.primary,
        fontWeight: 'bold',
    },
});

export default LoginScreen;
