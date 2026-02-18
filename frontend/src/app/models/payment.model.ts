export interface PaymentMethod {
    id?: number;
    user_id?: number;
    cardHolder: string;
    cardType: string;
    last4: string;
    expiryDate: string;
    isDefault: boolean;
}
