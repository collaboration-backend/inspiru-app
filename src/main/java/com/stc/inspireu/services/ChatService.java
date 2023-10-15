package com.stc.inspireu.services;

import com.stc.inspireu.dtos.ChatComposeDTO;
import com.stc.inspireu.dtos.ChatDTO;
import com.stc.inspireu.dtos.ChatMessageDTO;
import com.stc.inspireu.dtos.ChatRecipientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    Page<ChatRecipientDTO> fetchAllRecipients(Long userId, String searchKey, Pageable pageable);

    Page<ChatDTO> fetchAllChats(Long userId, Pageable pageable);

    Page<ChatMessageDTO> findChatMessages(Long userId, Long recipientId, Pageable pageable);

    ChatMessageDTO saveMessage(ChatComposeDTO composeDTO);

    void deleteMessage(Long userId, Long recipientId, Long chatId);

    Integer unreadCount(Long userId);

    void updateRead(Long userId, Long messageId);
}
