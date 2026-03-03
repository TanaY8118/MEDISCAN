import React, { createContext, useState, useEffect } from 'react';
import authService from '../utils/auth.service';
import { loginUser, registerUser } from '../api/auth.api';

export const AuthContext = createContext<any>(null);

export const AuthProvider = ({ children }: any) => {
    const [userToken, setUserToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const login = async (credentials: any) => {
        setIsLoading(true);
        try {
            const res = await loginUser(credentials);
            if (res.token && res.refreshToken) {
                setUserToken(res.token);
                await authService.setTokens(res.token, res.refreshToken);
            }
        } catch (e) {
            console.error('Login error:', e);
            throw e;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: any) => {
        setIsLoading(true);
        try {
            const res = await registerUser(userData);
            if (res.token && res.refreshToken) {
                setUserToken(res.token);
                await authService.setTokens(res.token, res.refreshToken);
            }
        } catch (e) {
            console.error('Registration error:', e);
            throw e;
        } finally {
            setIsLoading(false);
        }
    };

    const logout = async () => {
        setIsLoading(true);
        try {
            await authService.clearTokens();
            setUserToken(null);
        } catch (e) {
            console.error('Logout error:', e);
        } finally {
            setIsLoading(false);
        }
    };

    const loadStorageData = async () => {
        try {
            const token = await authService.getAccessToken();
            if (token) {
                setUserToken(token);
            }
        } catch (e) {
            console.error('Error loading token from storage:', e);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadStorageData();
    }, []);

    return (
        <AuthContext.Provider value={{ login, register, logout, isLoading, userToken }}>
            {children}
        </AuthContext.Provider>
    );
};
