import AsyncStorage from '@react-native-async-storage/async-storage';

const TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

class AuthService {
    /**
     * Store tokens in persistent storage
     */
    async setTokens(accessToken: string, refreshToken: string): Promise<void> {
        try {
            await Promise.all([
                AsyncStorage.setItem(TOKEN_KEY, accessToken),
                AsyncStorage.setItem(REFRESH_TOKEN_KEY, refreshToken),
            ]);
        } catch (error) {
            console.error('Error storing tokens:', error);
            throw error;
        }
    }

    /**
     * Update access token only (e.g. after refresh)
     */
    async setAccessToken(token: string): Promise<void> {
        try {
            await AsyncStorage.setItem(TOKEN_KEY, token);
        } catch (error) {
            console.error('Error storing access token:', error);
            throw error;
        }
    }

    /**
     * Retrieve access token
     */
    async getAccessToken(): Promise<string | null> {
        return await AsyncStorage.getItem(TOKEN_KEY);
    }

    /**
     * Retrieve refresh token
     */
    async getRefreshToken(): Promise<string | null> {
        return await AsyncStorage.getItem(REFRESH_TOKEN_KEY);
    }

    /**
     * Clear all tokens (logout)
     */
    async clearTokens(): Promise<void> {
        try {
            await Promise.all([
                AsyncStorage.removeItem(TOKEN_KEY),
                AsyncStorage.removeItem(REFRESH_TOKEN_KEY),
            ]);
        } catch (error) {
            console.error('Error clearing tokens:', error);
            throw error;
        }
    }

    /**
     * Check if a session exists
     */
    async hasValidSession(): Promise<boolean> {
        const token = await this.getAccessToken();
        return !!token;
    }
}

export default new AuthService();
