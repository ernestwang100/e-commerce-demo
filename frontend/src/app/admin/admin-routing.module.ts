import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { AdminDebugComponent } from './admin-debug/admin-debug.component';

const routes: Routes = [
  { path: '', component: AdminDashboardComponent },
  { path: 'products', component: ProductManagementComponent },
  { path: 'orders', component: OrderManagementComponent },
  { path: 'debug', component: AdminDebugComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
