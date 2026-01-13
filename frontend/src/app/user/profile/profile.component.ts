import { Component, OnInit } from '@angular/core';
import { UserService, UserProfile, ProfileUpdateRequest } from '../../services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

    profile: UserProfile | null = null;
    email: string = '';
    password: string = '';
    selectedFile: File | null = null;
    profilePictureUrl: string | null = null;
    isLoading = false;

    constructor(private userService: UserService, private snackBar: MatSnackBar) { }

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.isLoading = true;
        this.userService.getProfile().subscribe({
            next: (data) => {
                this.profile = data;
                this.email = data.email;
                if (data.hasProfilePicture) {
                    // Append timestamp to bust cache
                    this.profilePictureUrl = `${this.userService.getProfilePictureUrl()}?t=${new Date().getTime()}`;
                }
                this.isLoading = false;
            },
            error: (err) => {
                this.showSnackBar('Failed to load profile');
                this.isLoading = false;
            }
        });
    }

    onFileSelected(event: any): void {
        this.selectedFile = event.target.files[0];
    }

    uploadPicture(): void {
        if (!this.selectedFile) return;

        const formData = new FormData();
        formData.append('file', this.selectedFile);

        this.userService.uploadProfilePicture(formData).subscribe({
            next: (res) => {
                this.showSnackBar('Profile picture uploaded!');
                this.selectedFile = null;
                this.loadProfile(); // Refresh to see new picture
            },
            error: (err) => {
                this.showSnackBar('Failed to upload picture');
            }
        });
    }

    updateProfile(): void {
        const request: ProfileUpdateRequest = {
            email: this.email,
            password: this.password || undefined // Only send if not empty
        };

        this.userService.updateProfile(request).subscribe({
            next: (res) => {
                this.showSnackBar('Profile updated successfully!');
                this.password = ''; // Clear password field
            },
            error: (err) => {
                this.showSnackBar(err.error || 'Failed to update profile');
            }
        });
    }

    private showSnackBar(message: string): void {
        this.snackBar.open(message, 'Close', { duration: 3000 });
    }
}
