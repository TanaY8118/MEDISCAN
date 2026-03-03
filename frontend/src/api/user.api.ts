import client from './client';

export const getProfile = async () => {
    const response = await client.get('/users/me');
    return response.data;
};
