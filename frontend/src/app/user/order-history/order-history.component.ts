import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderResponse } from '../../models/order.model';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-order-history',
  templateUrl: './order-history.component.html',
  styleUrls: ['./order-history.component.css']
})
export class OrderHistoryComponent implements OnInit {
  orders: OrderResponse[] = [];
  loading = true;
  error = '';
  expandedOrderId: number | null = null;

  constructor(
    private orderService: OrderService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getUserOrders().subscribe({
      next: (orders) => {
        this.orders = orders.sort((a, b) =>
          new Date(b.datePlaced).getTime() - new Date(a.datePlaced).getTime()
        );
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load orders';
        this.loading = false;
      }
    });
  }

  toggleExpand(orderId: number): void {
    this.expandedOrderId = this.expandedOrderId === orderId ? null : orderId;
  }

  cancelOrder(orderId: number): void {
    if (confirm('Are you sure you want to cancel this order?')) {
      this.orderService.cancelOrder(orderId).subscribe({
        next: () => {
          this.snackBar.open('Order cancelled successfully', 'Close', { duration: 3000 });
          this.loadOrders();
        },
        error: () => {
          this.snackBar.open('Failed to cancel order', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getOrderTotal(order: OrderResponse): number {
    return order.items.reduce((total, item) =>
      total + (item.purchasedPrice * item.quantity), 0);
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'completed': return 'status-completed';
      case 'cancelled': case 'canceled': return 'status-cancelled';
      case 'processing': return 'status-processing';
      default: return 'status-pending';
    }
  }
}
