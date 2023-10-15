package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${api.version}/chat")
public class ChatController {

    private final ChatService chatService;

    @GetMapping(value = "recipients")
    public ResponseEntity<JsonResponseDTO<Page<ChatRecipientDTO>>> recipients(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int size,
                                                                              @RequestParam(required = false) String searchKey,
                                                                              HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return ResponseEntity.ok(new JsonResponseDTO<>(true, chatService.fetchAllRecipients(currentUserObject.getUserId(),
            searchKey, PageRequest.of(page, size))));
    }

    @PostMapping(value = "/{recipientId}")
    public ResponseEntity<JsonResponseDTO<ChatMessageDTO>> sendMessage(HttpServletRequest httpServletRequest,
                                                                       @PathVariable Long recipientId,
                                                                       @RequestBody @Valid ChatComposeDTO chatComposeDTO) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        chatComposeDTO.setUserId(currentUserObject.getUserId());
        chatComposeDTO.setRecipientId(recipientId);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Message successfully sent",
            chatService.saveMessage(chatComposeDTO)));
    }

    @DeleteMapping(value = "/{recipientId}/{chatId}")
    public ResponseEntity<JsonResponseDTO<Void>> deleteMessage(HttpServletRequest httpServletRequest,
                                                               @PathVariable Long recipientId,
                                                               @PathVariable Long chatId) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        chatService.deleteMessage(currentUserObject.getUserId(), recipientId, chatId);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Message successfully deleted"));
    }

    @GetMapping
    public ResponseEntity<JsonResponseDTO<Page<ChatDTO>>> getAllChat(HttpServletRequest httpServletRequest,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return ResponseEntity.ok(new JsonResponseDTO<>(true, chatService.fetchAllChats(currentUserObject.getUserId(),
            PageRequest.of(page, size))));
    }

    @GetMapping(value = "/{recipientId}")
    public ResponseEntity<JsonResponseDTO<Page<ChatMessageDTO>>> getChat(HttpServletRequest httpServletRequest,
                                                                         @PathVariable Long recipientId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return ResponseEntity.ok(new JsonResponseDTO<>(true, chatService.findChatMessages(currentUserObject.getUserId(),
            recipientId, PageRequest.of(page, size))));
    }

    @GetMapping(value = "unread-count")
    public ResponseEntity<JsonResponseDTO<Integer>> unreadCount(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        return ResponseEntity.ok(new JsonResponseDTO<>(true, chatService.unreadCount(currentUserObject.getUserId())));
    }

    @PutMapping(value = "/{recipientId}/{chatId}/read")
    public ResponseEntity<JsonResponseDTO<Void>> updateRead(@PathVariable Long recipientId,
                                                            @PathVariable Long chatId,
                                                            HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute("currentUserObject");
        chatService.updateRead(currentUserObject.getUserId(), chatId);
        return ResponseEntity.ok(new JsonResponseDTO<>(true, "Updated"));
    }
}
