package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_messages")
public class Chat extends BaseEntity {

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "is_read", columnDefinition = "boolean default false")
    private Boolean isRead = false;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean isDeleted = false;

    @Column(name = "deleted_on")
    private Date deletedOn;

    @ManyToOne
    private User recipient;

    @ManyToOne
    private User sender;

    @Column(name = "chat_key")
    private String key;
}
