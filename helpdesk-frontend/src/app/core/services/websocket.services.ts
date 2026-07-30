import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private client: Client | null = null;

  ticketUpdates$ = new Subject<any>();
  commentUpdates$ = new Subject<any>();

  connect(): void {
    this.client = new Client({
      webSocketFactory: () =>
        new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        this.client?.subscribe(
          '/topic/tickets', (message) => {
          this.ticketUpdates$.next(
            JSON.parse(message.body));
        });

        this.client?.subscribe(
          '/topic/comments', (message) => {
          this.commentUpdates$.next(
            JSON.parse(message.body));
        });
      }
    });

    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
  }
}