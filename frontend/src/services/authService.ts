import api from './api.ts';
import {
    type AuthenticationRequest,
    type AuthenticationResponse, type User
} from '../types/types.ts';

const USER_URL = "/v1/user";

class AuthService {

    async login(credentials: AuthenticationRequest): Promise<AuthenticationResponse> {
        try {
            const res = await api.post(USER_URL + '/authentication', credentials);
            console.log('ответ:', res);
            console.log('данные:', res.data);

            if (!res.data || !res.data.token)
                throw new Error('Неизвестная ошибка сервера: отсутствует токен');


            const { token } = res.data;
            this.setToken(token);

            const user = await this.fetchCurrentUser();
            this.setCurrentUser(user);

            return {
                token
            };

        } catch (error) {
            console.error('Ошибка авторизации:', error);
            throw error;
        }
    }

    async fetchCurrentUser(): Promise<User> {
        try {
            const res = await api.get<User>(USER_URL + '/me');
            return res.data;
        } catch (error) {
            console.error('Ошибка получения пользователя', error);
            return {
                id: -1,
                username: "unknow",
                email: "unknow",
                createdAt: "unknow"
            };
        }
    }

    logout(): void {
        this.removeToken();
        this.removeCurrentUser();
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    getCurrentUser(): User | null {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    }

    setCurrentUser(user: User): void {
        localStorage.setItem('user', JSON.stringify(user));
    }

    removeCurrentUser(): void {
        localStorage.removeItem('user');
    }

    getToken(): string | null {
        return localStorage.getItem('jwt');
    }

    setToken(token: string): void {
        localStorage.setItem('jwt', token);
    }

    removeToken(): void {
        localStorage.removeItem('jwt');
    }
}

export const authService = new AuthService();