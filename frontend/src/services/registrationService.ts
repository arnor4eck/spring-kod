import type { CreateUserRequest } from "../types/types.ts";
import apiClient from "./api.ts";

const TEMPLATE_URL = "/v1/user/registration"

class RegistrationService{
    async registration(request : CreateUserRequest): Promise<void> {
        try {
            await apiClient.post(TEMPLATE_URL, request);
        } catch (error) {
            console.error('Ошибка пререгистрации: ', error);
            throw error;
        }
    }

}

export const registrationService = new RegistrationService();