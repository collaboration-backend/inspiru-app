package com.stc.inspireu.services.impl;

import com.stc.inspireu.dtos.ChatComposeDTO;
import com.stc.inspireu.dtos.ChatDTO;
import com.stc.inspireu.dtos.ChatMessageDTO;
import com.stc.inspireu.dtos.ChatRecipientDTO;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ChatKeyProjection;
import com.stc.inspireu.mappers.ChatMapper;
import com.stc.inspireu.models.Chat;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.ChatRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.ChatService;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;


    @Override
    public Page<ChatRecipientDTO> fetchAllRecipients(Long userId, String searchKey, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        List<ChatKeyProjection> chatKeyProjections = chatRepository.findRecipients(userId);
        Set<Long> idsToBeExcluded = new HashSet<>();
        idsToBeExcluded.add(userId);
        chatKeyProjections.forEach(ck -> {
            String[] ckSplit = ck.getChat_key().split("_");
            Arrays.stream(ckSplit).forEach(ckS -> idsToBeExcluded.add(Long.valueOf(ckS)));
        });
        Page<User> recipients;
        boolean searchRequired = Objects.nonNull(searchKey) && !searchKey.isEmpty();
        List<String> roles = new ArrayList<>();
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_ADMIN);
        roles.add(RoleName.ROLE_MANAGEMENT_TEAM_MEMBER);
        roles.add(RoleName.ROLE_SUPER_ADMIN);
        if (!user.getRole().getRoleName().contains("STARTUP")) {
            roles.add(RoleName.ROLE_EXISTING_STARTUPS);
            roles.add(RoleName.ROLE_STARTUPS_ADMIN);
            roles.add(RoleName.ROLE_STARTUPS_BENEFICIARY);
            roles.add(RoleName.ROLE_STARTUPS_MEMBER);
        }
        recipients = searchRequired
            ? userRepository.findByIdNotInAndRole_RoleNameInAndAliasContainingIgnoreCase(idsToBeExcluded, roles, searchKey, pageable)
            : userRepository.findByIdNotInAndRole_RoleNameInAndAliasNotNull(idsToBeExcluded, roles, pageable);
        return new PageImpl<>(recipients.map(chatMapper::toChatRecipientDTO).toList(), pageable, recipients.getTotalElements());
    }

    @Override
    public Page<ChatDTO> fetchAllChats(Long userId, Pageable pageable) {
        Page<Chat> chats = chatRepository.findLatestChats(userId, pageable);
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return new PageImpl<>(chats
            .map(c -> createChatDTO(c, user))
            .toList(), pageable, chats.getTotalElements());
    }

    private ChatDTO createChatDTO(Chat chat, User user) {
        ChatDTO dto = new ChatDTO();
        User recipient = chat.getRecipient();
        User sender = chat.getSender();
        dto.setRecipientId(recipient.getId());
        dto.setRecipient(recipient.getAlias());
        dto.setRecipientRole(recipient.getRole().getRoleAlias());
        dto.setSenderId(sender.getId());
        dto.setSender(sender.getAlias());
        dto.setSenderRole(sender.getRole().getRoleAlias());
        dto.setMessage(chat.getMessage());
        dto.setLastMessageWasOn(new PrettyTime().format(new Date(ZonedDateTime.of(chat.getCreatedOn(), ZoneId.systemDefault()).toInstant().toEpochMilli())));
        dto.setNewReply(chatRepository.countByIsDeletedFalseAndIsReadFalseAndSenderAndRecipient((!sender.equals(user) ? sender : recipient), user));
        dto.setTotalReplies(chatRepository.totalRepliesCount(sender.getId(), recipient.getId()));
        if (dto.getTotalReplies() > 0)
            dto.setTotalReplies(dto.getTotalReplies() - 1);
        return dto;
    }

    @Transactional
    @Override
    public Page<ChatMessageDTO> findChatMessages(Long userId, Long recipientId, Pageable pageable) {
        if (userId.equals(recipientId))
            throw new CustomRunTimeException("You cannot send message to yourself");
        userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        userRepository.findById(recipientId).orElseThrow(() -> ItemNotFoundException.builder("Recipient").build());
        Page<Chat> chats = chatRepository.findChats(userId, recipientId, pageable);
        chatRepository.updateMessageRead(recipientId, userId);
        simpMessagingTemplate.convertAndSend("/topic/unreadMessageCount/user_" + userId, this.unreadCount(userId));
        return new PageImpl<>(chats.map(chatMapper::toChatMessageDTO)
            .toList(), pageable, chats.getTotalElements());
    }

    @Override
    public ChatMessageDTO saveMessage(ChatComposeDTO composeDTO) {
        if (composeDTO.getUserId().equals(composeDTO.getRecipientId()))
            throw new CustomRunTimeException("You cannot send message to yourself");
        User user = userRepository.findById(composeDTO.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        User recipient = userRepository.findById(composeDTO.getRecipientId()).orElseThrow(() -> ItemNotFoundException.builder("Recipient").build());
        Chat chat = new Chat();
        chat.setRecipient(recipient);
        chat.setSender(user);
        chat.setMessage(composeDTO.getMessage());
        chat.setCreatedOn(LocalDateTime.now());
        if (user.getId() < recipient.getId())
            chat.setKey(user.getId() + "_" + recipient.getId());
        else
            chat.setKey(recipient.getId() + "_" + user.getId());
        chat = chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/topic/unreadMessageCount/user_" + recipient.getId(), this.unreadCount(recipient.getId()));
        ChatMessageDTO dto = chatMapper.toChatMessageDTO(chat);
        simpMessagingTemplate.convertAndSend("/topic/newMessage/user_" + recipient.getId(), dto);
        return dto;
    }

    @Override
    public void deleteMessage(Long userId, Long recipientId, Long chatId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        User recipient = userRepository.findById(recipientId).orElseThrow(() -> ItemNotFoundException.builder("Recipient").build());
        Chat chat = chatRepository.findBySenderAndRecipientAndIsDeletedFalseAndId(user, recipient, chatId)
            .orElseThrow(() -> new CustomRunTimeException("Message not found or already deleted"));
        chat.setIsDeleted(true);
        chat.setDeletedOn(new Date());
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/topic/unreadMessageCount/user_" + recipientId, this.unreadCount(recipientId));
        simpMessagingTemplate.convertAndSend("/topic/deleteMessage/user_" + recipientId, chatId);
    }

    @Override
    public Integer unreadCount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        return chatRepository.countByIsDeletedFalseAndIsReadFalseAndRecipient(user);
    }

    @Override
    public void updateRead(Long userId, Long messageId) {
        Chat chat = chatRepository.findById(messageId).orElseThrow(() -> ItemNotFoundException.builder("Message").build());
        chat.setIsRead(true);
        this.chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/topic/unreadMessageCount/user_" + userId, this.unreadCount(userId));
    }
}
