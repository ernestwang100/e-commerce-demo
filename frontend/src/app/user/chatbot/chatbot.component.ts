import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ChatService, ChatMessage } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-chatbot',
    templateUrl: './chatbot.component.html',
    styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit, OnDestroy {
    @ViewChild('messagesContainer') messagesContainer!: ElementRef;
    @ViewChild('messageInput') messageInput!: ElementRef;

    isOpen = false;
    isLoading = false;
    messages: ChatMessage[] = [];
    newMessage = '';

    private subscription: Subscription = new Subscription();

    constructor(
        private chatService: ChatService,
        public authService: AuthService
    ) { }

    ngOnInit(): void {
        this.subscription.add(
            this.chatService.messages$.subscribe(messages => {
                this.messages = messages;
                setTimeout(() => this.scrollToBottom(), 100);
            })
        );
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    toggleChat(): void {
        this.isOpen = !this.isOpen;
        if (this.isOpen) {
            setTimeout(() => {
                this.messageInput?.nativeElement?.focus();
                this.scrollToBottom();
            }, 100);
        }
    }

    sendMessage(): void {
        if (!this.newMessage.trim() || this.isLoading) return;

        const message = this.newMessage;
        this.newMessage = '';
        this.isLoading = true;

        this.chatService.sendMessage(message).subscribe({
            next: () => {
                this.isLoading = false;
            },
            error: () => {
                this.isLoading = false;
            }
        });
    }

    clearChat(): void {
        this.chatService.clearChat();
    }

    private scrollToBottom(): void {
        if (this.messagesContainer) {
            const container = this.messagesContainer.nativeElement;
            container.scrollTop = container.scrollHeight;
        }
    }

    onKeyPress(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }
}
