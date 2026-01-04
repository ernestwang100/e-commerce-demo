import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderResponse } from '../../models/order.model';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-order-management',
  templateUrl: './order-management.component.html',
  styleUrls: ['./order-management.component.css']
})
export class OrderManagementComponent implements OnInit {
  orders: OrderResponse[] = [];
  displayedColumns = ['orderId', 'datePlaced', 'items', 'total', 'status', 'actions'];
  loading = true;
  currentPage = 1;
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
    this.orderService.getAdminOrders(this.currentPage).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load orders', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  toggleExpand(orderId: number): void {
    this.expandedOrderId = this.expandedOrderId === orderId ? null : orderId;
  }

  completeOrder(orderId: number): void {
    this.orderService.completeOrder(orderId).subscribe({
      next: () => {
        this.snackBar.open('Order completed', 'Close', { duration: 3000 });
        this.loadOrders();
      },
      error: () => {
        this.snackBar.open('Failed to complete order', 'Close', { duration: 3000 });
      }
    });
  }

  cancelOrder(orderId: number): void {
    if (confirm('Cancel this order?')) {
      this.orderService.cancelOrderAdmin(orderId).subscribe({
        next: () => {
          this.snackBar.open('Order cancelled', 'Close', { duration: 3000 });
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

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  nextPage(): void {
    this.currentPage++;
    this.loadOrders();
  }
}
