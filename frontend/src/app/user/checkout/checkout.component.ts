import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { UserService } from '../../services/user.service';
import { CartItem } from '../../models/cart.model';
import { Address } from '../../models/address.model';
import { PaymentMethod } from '../../models/payment.model';
import { OrderRequest, OrderItemRequest } from '../../models/order.model';
import { environment } from 'src/environments/environment';
import { AddressDialogComponent } from './dialogs/address-dialog/address-dialog.component';
import { PaymentDialogComponent } from './dialogs/payment-dialog/payment-dialog.component';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {

  cartItems: CartItem[] = [];
  totalAmount: number = 0;
  apiUrl = environment.apiUrl;

  // Shipping
  isPickup: boolean = false;
  addresses: Address[] = [];
  selectedAddressId: number | null = null;

  // Payment
  paymentMethods: PaymentMethod[] = [];
  selectedPaymentMethodId: number | null = null;

  processing: boolean = false;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private userService: UserService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.cartService.getCart().subscribe(items => {
      this.cartItems = items;
      this.totalAmount = this.cartService.getCartTotal();
      if (this.cartItems.length === 0) {
        this.router.navigate(['/user/cart']);
      }
    });

    this.loadUserData();
  }

  loadUserData(): void {
    this.userService.getAddresses().subscribe(data => {
      this.addresses = data;
      // Select default if exists and not already selected
      if (!this.selectedAddressId) {
        const def = data.find(a => a.isDefault);
        if (def && def.id) this.selectedAddressId = def.id;
      }
    });

    this.userService.getPaymentMethods().subscribe(data => {
      this.paymentMethods = data;
      if (!this.selectedPaymentMethodId) {
        const def = data.find(p => p.isDefault);
        if (def && def.id) this.selectedPaymentMethodId = def.id;
      }
    });
  }

  openAddressDialog(address?: Address): void {
    const dialogRef = this.dialog.open(AddressDialogComponent, {
      width: '500px',
      data: address ? { ...address } : null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.addAddress(result).subscribe(() => {
          this.snackBar.open('Address saved successfully', 'Close', { duration: 3000 });
          this.loadUserData();
        });
      }
    });
  }

  openPaymentDialog(payment?: PaymentMethod): void {
    const dialogRef = this.dialog.open(PaymentDialogComponent, {
      width: '500px',
      data: payment ? { ...payment } : null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.addPaymentMethod(result).subscribe(() => {
          this.snackBar.open('Payment method saved successfully', 'Close', { duration: 3000 });
          this.loadUserData();
        });
      }
    });
  }

  selectAddress(id: number | undefined): void {
    if (id) this.selectedAddressId = id;
  }

  selectPayment(id: number | undefined): void {
    if (id) this.selectedPaymentMethodId = id;
  }

  placeOrder(): void {
    if (!this.isPickup) {
      if (!this.selectedAddressId) {
        this.snackBar.open('Please select a shipping address', 'Close', { duration: 3000 });
        return;
      }
    }

    if (!this.selectedPaymentMethodId) {
      this.snackBar.open('Please select a payment method', 'Close', { duration: 3000 });
      return;
    }

    this.processing = true;

    // Construct OrderRequest
    const orderItems: OrderItemRequest[] = this.cartItems.map(item => ({
      productId: item.product.id,
      quantity: item.quantity
    }));

    const request: OrderRequest = {
      order: orderItems,
      isPickup: this.isPickup,
      addressId: this.isPickup ? undefined : (this.selectedAddressId || undefined),
      paymentMethodId: this.selectedPaymentMethodId || undefined
    };

    this.orderService.placeOrder(request).subscribe({
      next: (res) => {
        this.processing = false;
        this.cartService.clearCart();
        this.snackBar.open('Order placed successfully!', 'View Orders', { duration: 5000 })
          .onAction().subscribe(() => this.router.navigate(['/user/orders']));
        this.router.navigate(['/user/orders']);
      },
      error: (err) => {
        this.processing = false;
        this.snackBar.open(err.error?.message || 'Failed to place order', 'Close', { duration: 3000 });
      }
    });
  }
}
