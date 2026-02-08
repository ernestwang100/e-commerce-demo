export interface Product {
    id: number;
    name: string;
    description: string;
    retailPrice: number;
    wholesalePrice?: number; // Admin only
    quantity?: number;       // Admin only
    image?: string;          // Base64 string
    imageContentType?: string;
}

export interface ProductRequest {
    name: string;
    description: string;
    wholesalePrice: number;
    retailPrice: number;
    quantity: number;
}
