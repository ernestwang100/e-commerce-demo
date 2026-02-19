export interface OrderItemRequest {
    productId: number;
    quantity: number;
}

import { Address } from './address.model';
import { PaymentMethod } from './payment.model';

export interface OrderRequest {
    order: OrderItemRequest[];
    addressId?: number;
    newAddress?: Address;
    isPickup: boolean;
    paymentMethodId?: number;
    newPaymentMethod?: PaymentMethod;
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
    userId?: number;
    customerUsername?: string;
    customerEmail?: string;
    isPickup?: boolean;
    shippingAddress?: {
        fullName: string;
        addressLine1: string;
        addressLine2?: string;
        city: string;
        state: string;
        zipCode: string;
        country: string;
    };
    paymentMethod?: {
        cardType: string;
        last4Digits: string;
    };
}
