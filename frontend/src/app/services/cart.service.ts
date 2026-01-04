import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CartItem } from '../models/cart.model';
import { Product } from '../models/product.model';

@Injectable({
    providedIn: 'root'
})
export class CartService {
    private cartItems: CartItem[] = [];
    private cartSubject = new BehaviorSubject<CartItem[]>([]);

    constructor() {
        this.loadCart();
    }

    getCart(): Observable<CartItem[]> {
        return this.cartSubject.asObservable();
    }

    getCartItems(): CartItem[] {
        return [...this.cartItems];
    }

    addToCart(product: Product, quantity: number = 1): void {
        const existingItem = this.cartItems.find(item => item.product.id === product.id);
        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            this.cartItems.push({ product, quantity });
        }
        this.saveCart();
        this.cartSubject.next([...this.cartItems]);
    }

    updateQuantity(productId: number, quantity: number): void {
        const item = this.cartItems.find(item => item.product.id === productId);
        if (item) {
            if (quantity <= 0) {
                this.removeFromCart(productId);
            } else {
                item.quantity = quantity;
                this.saveCart();
                this.cartSubject.next([...this.cartItems]);
            }
        }
    }

    removeFromCart(productId: number): void {
        this.cartItems = this.cartItems.filter(item => item.product.id !== productId);
        this.saveCart();
        this.cartSubject.next([...this.cartItems]);
    }

    clearCart(): void {
        this.cartItems = [];
        this.saveCart();
        this.cartSubject.next([]);
    }

    getCartTotal(): number {
        return this.cartItems.reduce((total, item) =>
            total + (item.product.retailPrice * item.quantity), 0);
    }

    getCartCount(): number {
        return this.cartItems.reduce((count, item) => count + item.quantity, 0);
    }

    private saveCart(): void {
        localStorage.setItem('cart', JSON.stringify(this.cartItems));
    }

    private loadCart(): void {
        const savedCart = localStorage.getItem('cart');
        if (savedCart) {
            try {
                this.cartItems = JSON.parse(savedCart);
                this.cartSubject.next([...this.cartItems]);
            } catch {
                this.cartItems = [];
            }
        }
    }
}
