import axios from 'axios';
import { authService } from './authService.ts';
import type { User, Dataset } from '../types/types';
import api from './api';
import type { DeleteMarkupLineRequest, UpdateMarkupLineRequest } from '../types/types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

console.log(API_BASE_URL);

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use((config) => {
    const token = authService.getToken();
    
    const publicEndpoints = ['/v1/user/registration', '/v1/user/authentication'];
    const isPublic = publicEndpoints.some(endpoint => config.url?.includes(endpoint));
    
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    } else if (!isPublic) {
        console.error("Нет токена авторизации для", config.url);
    }
    
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response.status === 401 || error.response.status === 403) {
            window.location.href = '/auth';
        }
        return Promise.reject({
            code: error.response.data.code,
            messages: error.response.data.messages
        });
    }
);

export const userAPI = {
    getMe: async (): Promise<User> => {
        const response = await api.get('/v1/user/me');
        return response.data;
    },
    
    getMyDatasets: async (): Promise<Dataset[]> => {
        const response = await api.get('/v1/datasitory/my');
        return response.data;
    },
};



export const fileAPI = {
    uploadImages: async (datasetId: number, files: File[]): Promise<void> => {
        const formData = new FormData();
        files.forEach(file => {
            formData.append('files', file);
        });
        await apiClient.post(`/v1/files/images/${datasetId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },

    uploadMarkup: async (datasetId: number, file: File): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        await apiClient.post(`/v1/files/markup/${datasetId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },

    uploadProbability: async (datasetId: number, file: File): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        await apiClient.post(`/v1/files/probability/${datasetId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },

    uploadMetadata: async (datasetId: number, file: File): Promise<void> => {
        const formData = new FormData();
        formData.append('file', file);
        await apiClient.post(`/v1/files/metadata/${datasetId}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
};
export const datasetAPI = {
    create: async (data: {
        name: string;
        description?: string;
        visibility: 'PUBLIC' | 'PRIVATE';
        creatorId: number;
    }): Promise<Dataset> => {
        const requestData = {
            name: data.name,
            description: data.description || "",
            datasitoryType: data.visibility === 'PUBLIC' ? 'OPEN' : 'PRIVATE',
            creatorId: data.creatorId
        };
        const response = await apiClient.post('/v1/datasitory', requestData);
        return response.data;
    },

    getDatasetById: async (datasetId: number): Promise<Dataset> => {
        const response = await apiClient.get(`/v1/datasitory/${datasetId}`);
        return response.data;
    },

    update: async (datasetId: number, data: {
        name: string;
        description?: string;
        visibility: 'PUBLIC' | 'PRIVATE';
    }): Promise<Dataset> => {
        const requestData = {
            name: data.name,
            description: data.description || "",
            datasitoryType: data.visibility === 'PUBLIC' ? 'OPEN' : 'PRIVATE',
        };
        const response = await apiClient.put(`/v1/datasitory/${datasetId}`, requestData);
        return response.data;
    },

    delete: async (datasetId: number): Promise<void> => {
        await apiClient.delete(`/v1/datasitory/${datasetId}`);
    },
    
    getMyDatasets: async (): Promise<Dataset[]> => {
        const response = await apiClient.get('/v1/datasitory/my');
        return response.data;
    },
     exportDataset: async (datasetId: number): Promise<Blob> => {
        const response = await apiClient.get(`/v1/datasitory/export/${datasetId}`, {
            responseType: 'blob'
        });
        return response.data;
    },
};


export const favoriteAPI = {
    addToFavorites: async (datasetId: number): Promise<void> => {
        await apiClient.post(`/v1/favorite/${datasetId}`);
    },

    removeFromFavorites: async (datasetId: number): Promise<void> => {
        await apiClient.delete(`/v1/favorite/${datasetId}`);
    },

    getMyFavorites: async (): Promise<Dataset[]> => {
        const response = await apiClient.get('/v1/favorite/my');
        if (response.data && Array.isArray(response.data.datasitoryDtoList)) {
            return response.data.datasitoryDtoList;
        }
        if (Array.isArray(response.data)) {
            return response.data;
        }
        console.error('getMyFavorites вернул неожиданный формат:', response.data);
        return [];
    },
};

export const markupAPI = {
    deleteMarkupLines: async (datasetId: number, request: DeleteMarkupLineRequest): Promise<void> => {
        await apiClient.patch(`/v1/markup/delete/${datasetId}`, request);
    },

    updateMarkupLines: async (datasetId: number, request: UpdateMarkupLineRequest): Promise<void> => {
        await apiClient.patch(`/v1/markup/update/${datasetId}`, request);
    },
};

export default apiClient;