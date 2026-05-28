import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
    isAuthenticated: boolean;
    login: () => void;
    logout: () => void;
    checkAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            isAuthenticated: false,
            login: () => set({ isAuthenticated: true }),
            logout: () => {
                localStorage.removeItem('jwt');
                localStorage.removeItem('user');
                set({ isAuthenticated: false });
            },
            checkAuth: () => {
                const token = localStorage.getItem('jwt');
                set({ isAuthenticated: !!token });
            },
        }),
        {
            name: 'auth-storage',
        }
    )
);