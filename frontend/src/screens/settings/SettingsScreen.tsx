import React, { useContext, useState, useEffect } from 'react';
import { View, Text, Button, StyleSheet, ActivityIndicator } from 'react-native';
import { AuthContext } from '../../context/AuthContext';
import { getProfile } from '../../api/user.api';

const SettingsScreen = () => {
    const { logout } = useContext(AuthContext);
    const [profile, setProfile] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const data = await getProfile();
                setProfile(data);
            } catch (error) {
                console.error(error);
            } finally {
                setLoading(false);
            }
        };
        fetchProfile();
    }, []);

    if (loading) {
        return <View style={styles.center}><ActivityIndicator size="large" color="#2196f3" /></View>;
    }

    return (
        <View style={styles.container}>
            <Text style={styles.header}>User Profile</Text>

            {profile && (
                <View style={styles.infoContainer}>
                    <Text style={styles.label}>Username</Text>
                    <Text style={styles.value}>{profile.username}</Text>

                    <Text style={styles.label}>Email</Text>
                    <Text style={styles.value}>{profile.email}</Text>

                    <Text style={styles.label}>Role</Text>
                    <Text style={styles.value}>{profile.role}</Text>
                </View>
            )}

            <View style={styles.logoutBtn}>
                <Button title="Logout" onPress={logout} color="#d32f2f" />
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
        backgroundColor: '#fff',
    },
    center: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    header: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 30,
        textAlign: 'center',
        color: '#333',
    },
    infoContainer: {
        marginBottom: 40,
    },
    label: {
        fontSize: 14,
        color: '#666',
        marginTop: 15,
    },
    value: {
        fontSize: 18,
        fontWeight: '500',
        color: '#333',
        borderBottomWidth: 1,
        borderBottomColor: '#eee',
        paddingBottom: 5,
    },
    logoutBtn: {
        marginTop: 'auto',
    }
});

export default SettingsScreen;
