package com.stc.inspireu.repositories;

import com.stc.inspireu.jpa.projections.ChatKeyProjection;
import com.stc.inspireu.models.Chat;
import com.stc.inspireu.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findBySenderAndRecipientAndIsDeletedFalseAndId(User sender, User recipient, Long id);

    @Query(value = "select * from chat_messages cm \n" +
        "inner join(select chat_key,max(id) as lastMessageId\n" +
        "from chat_messages \n" +
        "where is_deleted=false and (sender_id=:userId or recipient_id=:userId) \n" +
        "group by chat_key)cm_f on cm.id=cm_f.lastMessageId \n" +
        "order by created_on desc", nativeQuery = true)
    Page<Chat> findLatestChats(Long userId, Pageable pageable);

    @Query(value = "select * from chat_messages \n" +
        "where is_deleted=false and (sender_id=:userId or recipient_id=:userId)\n" +
        "and (sender_id=:recipientId or recipient_id=:recipientId)\n" +
        "order by created_on desc", nativeQuery = true)
    Page<Chat> findChats(Long userId, Long recipientId, Pageable pageable);

    @Modifying
    @Query(value = "update chat_messages set is_read=true where is_read=false and sender_id=:userId and recipient_id=:recipientId", nativeQuery = true)
    void updateMessageRead(Long userId, Long recipientId);

    Integer countByIsDeletedFalseAndIsReadFalseAndSenderAndRecipient(User sender, User recipient);

    Integer countByIsDeletedFalseAndIsReadFalseAndRecipient(User user);

    @Query(value = "select count(*) from chat_messages \n" +
        "where is_deleted=false and (sender_id=:userId or recipient_id=:userId)\n" +
        "and (sender_id=:recipientId or recipient_id=:recipientId)" , nativeQuery = true)
    Integer totalRepliesCount(Long userId,Long recipientId);

    @Query(value = "select chat_key from chat_messages where (sender_id=:userId or recipient_id=:userId) and is_deleted=false group by chat_key",nativeQuery = true)
    List<ChatKeyProjection> findRecipients(Long userId);
}
