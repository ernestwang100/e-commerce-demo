export interface User {
    username: string;
    token: string;
    isAdmin: boolean;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    username: string;
    isAdmin: boolean;
}
