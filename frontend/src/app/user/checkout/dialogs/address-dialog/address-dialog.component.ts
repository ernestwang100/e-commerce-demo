import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Address } from '../../../../models/address.model';

@Component({
  selector: 'app-address-dialog',
  templateUrl: './address-dialog.component.html',
  styleUrls: ['./address-dialog.component.css']
})
export class AddressDialogComponent {

  address: Address;
  isEdit: boolean;

  constructor(
    public dialogRef: MatDialogRef<AddressDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Address | null
  ) {
    this.isEdit = !!data;
    this.address = data ? { ...data } : {
      fullName: '',
      addressLine1: '',
      city: '',
      state: '',
      zipCode: '',
      country: '',
      isDefault: false
    };
  }

  onSave(): void {
    if (this.isValid()) {
      this.dialogRef.close(this.address);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  isValid(): boolean {
    return !!this.address.fullName && !!this.address.addressLine1 &&
      !!this.address.city && !!this.address.state &&
      !!this.address.zipCode && !!this.address.country;
  }
}
