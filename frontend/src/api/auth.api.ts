import client from './client';

export const loginUser = async (credentials: any) => {
    const response = await client.post('/auth/login', credentials);
    return response.data;
};

export const registerUser = async (userData: any) => {
    const response = await client.post('/auth/register', userData);
    return response.data;
};

export const refreshTokens = async (refreshToken: string) => {
    const response = await client.post('/auth/refresh', { refreshToken });
    return response.data;
};
