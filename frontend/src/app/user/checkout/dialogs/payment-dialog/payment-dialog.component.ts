import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { PaymentMethod } from '../../../../models/payment.model';

@Component({
  selector: 'app-payment-dialog',
  templateUrl: './payment-dialog.component.html',
  styleUrls: ['./payment-dialog.component.css']
})
export class PaymentDialogComponent {

  paymentMethod: PaymentMethod;
  isEdit: boolean;

  constructor(
    public dialogRef: MatDialogRef<PaymentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PaymentMethod | null
  ) {
    this.isEdit = !!data;
    this.paymentMethod = data ? { ...data } : {
      cardHolder: '',
      cardType: 'Visa',
      last4: '',
      expiryDate: '',
      isDefault: false
    };
  }

  onSave(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.paymentMethod);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  isValid(): boolean {
    return !!this.paymentMethod.cardHolder && !!this.paymentMethod.last4 &&
      !!this.paymentMethod.expiryDate && this.paymentMethod.last4.length === 4;
  }
}
