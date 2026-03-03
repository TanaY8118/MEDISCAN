import React, { useContext, useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, SafeAreaView, ScrollView } from 'react-native';
import { AuthContext } from '../../context/AuthContext';
import { Theme } from '../../constants/Theme';

const RegisterScreen = ({ navigation }: any) => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const { register, isLoading } = useContext(AuthContext);

    const handleRegister = async () => {
        if (!username || !email || !password) {
            Alert.alert('Error', 'Please fill in all fields');
            return;
        }

        // Simple email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            Alert.alert('Error', 'Please enter a valid email address');
            return;
        }

        try {
            await register({ username, email, password });
        } catch (e) {
            Alert.alert('Registration Failed', 'Could not create account. Username or email might already be taken.');
        }
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <ScrollView contentContainerStyle={styles.scrollContainer}>
                <View style={styles.container}>
                    <View style={styles.headerContainer}>
                        <Text style={styles.title}>Create Account</Text>
                        <Text style={styles.subtitle}>Join MediScan today</Text>
                    </View>

                    <View style={styles.formContainer}>
                        <Text style={styles.label}>Username</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="Choose a username"
                            value={username}
                            onChangeText={setUsername}
                            autoCapitalize="none"
                        />

                        <Text style={styles.label}>Email</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="Enter your email"
                            value={email}
                            onChangeText={setEmail}
                            keyboardType="email-address"
                            autoCapitalize="none"
                        />

                        <Text style={styles.label}>Password</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="Choose a password"
                            value={password}
                            onChangeText={setPassword}
                            secureTextEntry
                        />

                        <TouchableOpacity
                            style={[styles.button, isLoading && styles.buttonDisabled]}
                            onPress={handleRegister}
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <ActivityIndicator color="#fff" />
                            ) : (
                                <Text style={styles.buttonText}>Register</Text>
                            )}
                        </TouchableOpacity>

                        <View style={styles.footer}>
                            <Text style={styles.footerText}>Already have an account? </Text>
                            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
                                <Text style={styles.linkText}>Back to Login</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </ScrollView>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: Theme.colors.background,
    },
    scrollContainer: {
        flexGrow: 1,
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

export default RegisterScreen;
