package com.faforever.client.chat.event;

import com.faforever.client.chat.ChatMessage;

public class UnreadPrivateMessageEvent {


  private final ChatMessage message;
  private final boolean chatFocused;

  public UnreadPrivateMessageEvent(ChatMessage message, boolean chatFocused) {
    this.message = message;
    this.chatFocused = chatFocused;
  }

  public ChatMessage getMessage() {
    return message;
  }

  public boolean isChatFocused() {
    return chatFocused;
  }
}
