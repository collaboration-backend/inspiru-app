package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetProfileCardDto;
import com.stc.inspireu.models.ProfileCard;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {User.class, Startup.class, ProfileCard.class})
public interface ProfileCardMapper {

    @Mapping(source = "id", target = "profileCardId")
    @Mapping(source = "createdOn", target = "createdAt")
    @Mapping(source = "modifiedOn", target = "updatedAt")
    @Mapping(source = "name", target = "profileCardName")
    @Mapping(source = "createdUser.alias", target = "createdUser")
    @Mapping(source = "startup.startupName", target = "startup")
    @Mapping(source = "refProfileCard.name", target = "refProfileCard")
    GetProfileCardDto toGetProfileCardDto(ProfileCard form);
}
