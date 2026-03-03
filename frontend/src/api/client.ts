import axios from 'axios';
import authService from '../utils/auth.service';

export const BASE_URL = 'http://10.0.2.2:8080/api';

const client = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

client.interceptors.request.use(
    async (config) => {
        const token = await authService.getAccessToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

client.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise(function (resolve, reject) {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers['Authorization'] = 'Bearer ' + token;
                        return client(originalRequest);
                    })
                    .catch((err) => {
                        return Promise.reject(err);
                    });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = await authService.getRefreshToken();
            if (!refreshToken) {
                isRefreshing = false;
                return Promise.reject(error);
            }

            try {
                // Circular dependency concern: Importing refreshTokens from auth.api might be tricky
                // if auth.api imports client. Using client directly for refresh to avoid it.
                const response = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken });
                const { token } = response.data;

                await authService.setAccessToken(token);
                client.defaults.headers.common['Authorization'] = 'Bearer ' + token;
                originalRequest.headers['Authorization'] = 'Bearer ' + token;

                processQueue(null, token);
                return client(originalRequest);
            } catch (refreshError) {
                processQueue(refreshError, null);
                await authService.clearTokens();
                // Optionally: Trigger a logout event or redirection
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default client;
