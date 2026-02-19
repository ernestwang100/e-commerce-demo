import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { OrderRequest } from '../../models/order.model';

@Component({
    selector: 'app-payment',
    templateUrl: './payment.component.html',
    styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {
    cardNumber: string = '';
    expiryDate: string = '';
    cvv: string = '';
    processing = false;
    totalAmount = 0;

    constructor(
        private cartService: CartService,
        private orderService: OrderService,
        private router: Router,
        private snackBar: MatSnackBar
    ) { }

    ngOnInit(): void {
        const items = this.cartService.getCartItems();
        if (items.length === 0) {
            this.router.navigate(['/user/cart']);
            return;
        }
        this.totalAmount = this.cartService.getCartTotal();
    }

    processPayment(): void {
        if (!this.cardNumber || !this.expiryDate || !this.cvv) {
            this.snackBar.open('Please fill in all payment details (simulation).', 'Close', { duration: 3000 });
            return;
        }

        this.processing = true;

        const items = this.cartService.getCartItems();
        const orderRequest: OrderRequest = {
            order: items.map(item => ({
                productId: item.product.id,
                quantity: item.quantity
            })),
            isPickup: false
        };

        this.orderService.placeOrder(orderRequest).subscribe({
            next: (order) => {
                this.processing = false;
                this.cartService.clearCart();
                this.snackBar.open('Payment successful! Order placed.', 'View Orders', {
                    duration: 5000
                }).onAction().subscribe(() => {
                    this.router.navigate(['/user/orders']);
                });
                this.router.navigate(['/user/orders']);
            },
            error: (err) => {
                this.processing = false;
                const message = err.error?.message || 'Payment failed. Please try again.';
                this.snackBar.open(message, 'Close', { duration: 5000 });
            }
        });
    }
}
