import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { UserService } from '../../services/user.service';
import { CartItem } from '../../models/cart.model';
import { Address } from '../../models/address.model';
import { PaymentMethod } from '../../models/payment.model';
import { OrderRequest, OrderItemRequest } from '../../models/order.model';
import { environment } from 'src/environments/environment';

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
  newAddress: Address = {
    fullName: '',
    addressLine1: '',
    city: '',
    state: '',
    zipCode: '',
    country: '',
    isDefault: false
  };
  showNewAddressForm: boolean = false;

  // Payment
  paymentMethods: PaymentMethod[] = [];
  selectedPaymentMethodId: number | null = null;
  newPaymentMethod: PaymentMethod = {
    cardHolder: '',
    cardType: 'Visa',
    last4: '',
    expiryDate: '',
    isDefault: false
  };
  showNewPaymentForm: boolean = false;

  processing: boolean = false;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private userService: UserService,
    private router: Router,
    private snackBar: MatSnackBar
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
      // Select default if exists
      const def = data.find(a => a.isDefault);
      if (def && def.id) this.selectedAddressId = def.id;
    });

    this.userService.getPaymentMethods().subscribe(data => {
      this.paymentMethods = data;
      const def = data.find(p => p.isDefault);
      if (def && def.id) this.selectedPaymentMethodId = def.id;
    });
  }

  toggleNewAddress(show: boolean): void {
    this.showNewAddressForm = show;
    if (show) this.selectedAddressId = null;
  }

  toggleNewPayment(show: boolean): void {
    this.showNewPaymentForm = show;
    if (show) this.selectedPaymentMethodId = null;
  }

  placeOrder(): void {
    if (!this.isPickup) {
      if (!this.selectedAddressId && !this.showNewAddressForm) {
        this.snackBar.open('Please select a shipping address', 'Close', { duration: 3000 });
        return;
      }
      if (this.showNewAddressForm && !this.isValidAddress(this.newAddress)) {
        this.snackBar.open('Please fill in valid address details', 'Close', { duration: 3000 });
        return;
      }
    }

    if (!this.selectedPaymentMethodId && !this.showNewPaymentForm) {
      this.snackBar.open('Please select a payment method', 'Close', { duration: 3000 });
      return;
    }
    if (this.showNewPaymentForm && !this.isValidPayment(this.newPaymentMethod)) {
      this.snackBar.open('Please fill in valid payment details', 'Close', { duration: 3000 });
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
      newAddress: (this.isPickup || this.selectedAddressId) ? undefined : this.newAddress,
      paymentMethodId: this.selectedPaymentMethodId || undefined,
      newPaymentMethod: this.selectedPaymentMethodId ? undefined : this.newPaymentMethod
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

  isValidAddress(addr: Address): boolean {
    return !!addr.fullName && !!addr.addressLine1 && !!addr.city && !!addr.state && !!addr.zipCode && !!addr.country;
  }

  isValidPayment(pay: PaymentMethod): boolean {
    return !!pay.cardHolder && !!pay.last4 && !!pay.expiryDate;
  }
}
