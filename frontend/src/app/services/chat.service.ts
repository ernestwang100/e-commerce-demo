import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatMessage {
    sessionId: string;
    message: string;
    role: 'user' | 'assistant';
    timestamp: Date;
}

export interface ChatRequest {
    message: string;
    sessionId?: string;
}

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    private apiUrl = `${environment.apiUrl}/user/chat`;
    private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
    private sessionId: string | null = null;

    messages$ = this.messagesSubject.asObservable();

    constructor(private http: HttpClient) {
        this.loadSession();
    }

    private loadSession(): void {
        this.sessionId = localStorage.getItem('chatSessionId');
        if (this.sessionId) {
            this.loadHistory(this.sessionId);
        }
    }

    sendMessage(message: string): Observable<ChatMessage> {
        const request: ChatRequest = {
            message,
            sessionId: this.sessionId || undefined
        };

        // Optimistically add user message to UI
        const userMsg: ChatMessage = {
            sessionId: this.sessionId || '',
            message,
            role: 'user',
            timestamp: new Date()
        };
        const currentMessages = this.messagesSubject.value;
        this.messagesSubject.next([...currentMessages, userMsg]);

        return new Observable(observer => {
            this.http.post<ChatMessage>(`${this.apiUrl}/message`, request).subscribe({
                next: (response) => {
                    // Save session ID
                    if (!this.sessionId) {
                        this.sessionId = response.sessionId;
                        localStorage.setItem('chatSessionId', this.sessionId);
                    }

                    // Update user message with correct session ID and add assistant response
                    const msgs = this.messagesSubject.value;
                    msgs[msgs.length - 1].sessionId = response.sessionId;
                    this.messagesSubject.next([...msgs, response]);

                    observer.next(response);
                    observer.complete();
                },
                error: (err) => {
                    // Remove optimistic user message on error
                    const msgs = this.messagesSubject.value;
                    msgs.pop();
                    this.messagesSubject.next([...msgs]);
                    observer.error(err);
                }
            });
        });
    }

    loadHistory(sessionId: string): void {
        this.http.get<ChatMessage[]>(`${this.apiUrl}/history/${sessionId}`).subscribe({
            next: (messages) => this.messagesSubject.next(messages),
            error: () => this.messagesSubject.next([])
        });
    }

    clearChat(): void {
        if (this.sessionId) {
            this.http.delete(`${this.apiUrl}/history/${this.sessionId}`).subscribe();
        }
        this.sessionId = null;
        localStorage.removeItem('chatSessionId');
        this.messagesSubject.next([]);
    }

    getMessages(): ChatMessage[] {
        return this.messagesSubject.value;
    }
}
