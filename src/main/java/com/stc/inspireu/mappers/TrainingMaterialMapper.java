package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.GetTrainingMaterialDto;
import com.stc.inspireu.dtos.TrainingMaterialManagementDto;
import com.stc.inspireu.models.TrainingMaterial;
import com.stc.inspireu.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {User.class})
public interface TrainingMaterialMapper {

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdBy")
    @Mapping(source = "name", target = "materialName")
    @Mapping(source = "path", target = "materialFile")
    TrainingMaterialManagementDto toTrainingMaterialManagementDto(TrainingMaterial trainingMaterial);

    List<TrainingMaterialManagementDto> toTrainingMaterialManagementDtoList(Iterable<TrainingMaterial> list);

    @Mapping(source = "createdOn", target = "createdAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "modifiedOn", target = "updatedAt", qualifiedByName = "convertDateToLong")
    @Mapping(source = "createdUser.alias", target = "createdUserName")
    GetTrainingMaterialDto toGetTrainingMaterialDto(TrainingMaterial e);

    List<GetTrainingMaterialDto> toGetTrainingMaterialDtoList(Iterable<TrainingMaterial> list);

    @Named("convertDateToLong")
    default Long convertDateToLong(LocalDateTime date) {
        if (date != null) {
            return ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }
}
