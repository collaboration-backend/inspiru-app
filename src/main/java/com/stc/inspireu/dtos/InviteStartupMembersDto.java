package com.stc.inspireu.dtos;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class InviteStartupMembersDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @Size(min = 1, max = 20, message = "minimum 1 and maximum 20 members")
    private List<InviteStartupMemberDto> members;

    private String inviteMessage;

}
