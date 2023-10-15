package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.ChatDTO;
import com.stc.inspireu.dtos.ChatMessageDTO;
import com.stc.inspireu.dtos.ChatRecipientDTO;
import com.stc.inspireu.models.Chat;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.models.User;
import org.mapstruct.*;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Mapper(componentModel = "spring", uses = {User.class, Role.class})
public interface ChatMapper {

    @Mapping(source = "createdOn", target = "lastMessageWasOn", qualifiedByName = "convertDateToString")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.alias", target = "sender")
    @Mapping(source = "sender.role.roleAlias", target = "senderRole")
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "recipient.alias", target = "recipient")
    @Mapping(source = "recipient.role.roleAlias", target = "recipientRole")
    ChatDTO toChatDTO(Chat chat);

    @Mapping(source = "chat.id", target = "messageId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.alias", target = "sender")
    @Mapping(source = "sender.role.roleAlias", target = "senderRole")
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "recipient.alias", target = "recipient")
    @Mapping(source = "recipient.role.roleAlias", target = "recipientRole")
    @Mapping(source = "createdOn", target = "createdOn", qualifiedByName = "convertDateToString")
    ChatMessageDTO toChatMessageDTO(Chat chat);

    @Mapping(source = "role.roleAlias", target = "role")
    @Mapping(source = "alias", target = "name")
    ChatRecipientDTO toChatRecipientDTO(User user);

    @Named("convertDateToString")
    default String convertDateToString(LocalDateTime date) {
        if (date != null) {
            return new PrettyTime().format(new Date(ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli()));
        } else {
            return null;
        }
    }

    @AfterMapping
    default void updateChatDTOFromFixedValues(@MappingTarget ChatDTO dto) {
        dto.setNewReply(2);
        dto.setTotalReplies(23);
    }
}
