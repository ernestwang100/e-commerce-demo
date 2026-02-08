import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Product, ProductRequest } from '../../models/product.model';
import { ProductService } from '../../services/product.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-product-management',
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.css']
})
export class ProductManagementComponent implements OnInit {
  products: Product[] = [];
  displayedColumns = ['id', 'image', 'name', 'description', 'retailPrice', 'wholesalePrice', 'quantity', 'actions'];
  loading = true;
  showForm = false;
  editingProduct: Product | null = null;
  productForm: FormGroup;
  submitting = false;

  selectedFile: File | null = null;
  imagePreview: SafeUrl | string | null = null;

  constructor(
    private productService: ProductService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required]],
      wholesalePrice: [0, [Validators.required, Validators.min(0)]],
      retailPrice: [0, [Validators.required, Validators.min(0)]],
      quantity: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load products', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  openAddForm(): void {
    this.editingProduct = null;
    this.productForm.reset({ wholesalePrice: 0, retailPrice: 0, quantity: 0 });
    this.selectedFile = null;
    this.imagePreview = null;
    this.showForm = true;
  }

  openEditForm(product: Product): void {
    this.editingProduct = product;
    this.productForm.patchValue({
      name: product.name,
      description: product.description,
      wholesalePrice: product.wholesalePrice || 0,
      retailPrice: product.retailPrice,
      quantity: product.quantity || 0
    });

    this.selectedFile = null;
    this.imagePreview = null;
    this.loadProductImage(product.id);

    this.showForm = true;
  }

  loadProductImage(id: number): void {
    this.productService.getProductImageBlob(id).subscribe({
      next: (blob) => {
        const objectUrl = URL.createObjectURL(blob);
        this.imagePreview = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
      },
      error: () => {
        this.imagePreview = null;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const objectUrl = URL.createObjectURL(file);
      this.imagePreview = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
    }
  }

  closeForm(): void {
    this.showForm = false;
    this.editingProduct = null;
    this.selectedFile = null;
    this.imagePreview = null;
  }

  submitForm(): void {
    if (this.productForm.invalid) return;

    this.submitting = true;
    const request: ProductRequest = this.productForm.value;

    const action = this.editingProduct
      ? this.productService.updateProduct(this.editingProduct.id, request)
      : this.productService.createProduct(request);

    action.subscribe({
      next: (product) => {
        if (this.selectedFile) {
          this.uploadImage(product.id);
        } else {
          this.finishSubmit();
        }
      },
      error: () => {
        this.snackBar.open('Operation failed', 'Close', { duration: 3000 });
        this.submitting = false;
      }
    });
  }

  uploadImage(productId: number): void {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.productService.uploadProductImage(productId, formData).subscribe({
      next: () => {
        this.finishSubmit();
      },
      error: () => {
        this.snackBar.open('Product saved but image upload failed', 'Close', { duration: 3000 });
        this.finishSubmit();
      }
    });
  }

  finishSubmit(): void {
    this.snackBar.open(
      this.editingProduct ? 'Product updated' : 'Product created',
      'Close',
      { duration: 3000 }
    );
    this.closeForm();
    this.loadProducts();
    this.submitting = false;
  }

  deleteProduct(product: Product): void {
    if (confirm(`Delete "${product.name}"?`)) {
      this.productService.deleteProduct(product.id).subscribe({
        next: () => {
          this.snackBar.open('Product deleted', 'Close', { duration: 3000 });
          this.loadProducts();
        },
        error: () => {
          this.snackBar.open('Failed to delete', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
