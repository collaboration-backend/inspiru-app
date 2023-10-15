package com.stc.inspireu.mappers;

import com.stc.inspireu.dtos.ContactUsSubjectDTO;
import com.stc.inspireu.models.ContactUsSubject;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContactUsSubjectMapper {

    ContactUsSubjectDTO toContactUsSubjectDTO(ContactUsSubject subject);
}
