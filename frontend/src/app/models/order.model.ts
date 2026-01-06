export interface OrderItemRequest {
    productId: number;
    quantity: number;
}

export interface OrderRequest {
    order: OrderItemRequest[];
}

export interface OrderItemResponse {
    productId: number;
    productName: string;
    quantity: number;
    purchasedPrice: number;
}

export interface OrderResponse {
    orderId: number;
    datePlaced: string;
    orderStatus: string;
    items: OrderItemResponse[];
}
