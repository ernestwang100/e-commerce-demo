export interface Product {
    id: number;
    name: string;
    description: string;
    retailPrice: number;
    wholesalePrice?: number; // Admin only
    quantity?: number;       // Admin only
}

export interface ProductRequest {
    name: string;
    description: string;
    wholesalePrice: number;
    retailPrice: number;
    quantity: number;
}
