package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.FileFolderDto;
import com.stc.inspireu.models.FileFolder;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class, FileFolder.class})
public interface FileFolderMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.id", target = "createdUserId")
    @Mapping(source = "createdUser.alias", target = "createdUserName")
    @Mapping(source = "refFileFolder.id", target = "refFileFolderId")
    @Mapping(source = "refFileFolder.name", target = "refFileFolderName")
    FileFolderDto toFileFolderDto(FileFolder fileFolder);

    List<FileFolderDto> toFileFolderDtoList(Iterable<FileFolder> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
