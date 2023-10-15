package com.stc.inspireu.dtos;

import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.Survey;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
class LocalStartupDto implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

}

@Data
class LocalUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

}

@Data
public class SurveyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String status;

    private Long dueDate;

    private Long submittedOn;

    private LocalUserDto submittedUser;

    private LocalStartupDto submittedStartup;

    private Long workshopSession;

    private String jsonForm;

    private String reviwed1Status;

    private String reviwed2Status;

    private String reviwer1;

    private String reviwer2;

    private Long reviwed1On;

    private Long reviwed2On;


    public static List<SurveyDto> fromEntityList(Iterable<Survey> list) {

        List<SurveyDto> surveyDtos = new ArrayList<SurveyDto>();

        for (Survey survey : list) {
            SurveyDto surveyDto = new SurveyDto();
            surveyDto.setId(survey.getId());
            surveyDto.setName(survey.getName());
            surveyDto.setStatus(survey.getStatus());
            surveyDto.setDueDate(survey.getDueDate().toInstant().toEpochMilli());
            surveyDto.setWorkshopSession(survey.getWorkshopSession().getId());
            surveyDto.setJsonForm(survey.getJsonForm());
            if (survey.getSubmittedOn() != null) {
                surveyDto.setSubmittedOn(survey.getSubmittedOn().toInstant().toEpochMilli());
            }
            if (survey.getSubmittedUser() != null) {
                LocalUserDto localUserDto = new LocalUserDto();
                localUserDto.setId(survey.getSubmittedUser().getId());
                localUserDto.setName(survey.getSubmittedUser().getEmail());
                surveyDto.setSubmittedUser(localUserDto);
            }
            if (survey.getSubmittedStartup() != null) {
                LocalStartupDto localStartupDto = new LocalStartupDto();
                localStartupDto.setId(survey.getSubmittedStartup().getId());
                localStartupDto.setName(survey.getSubmittedStartup().getStartupName());
                surveyDto.setSubmittedStartup(localStartupDto);
            }
            surveyDto.setReviwed1Status(survey.getReview1Status());
            surveyDto.setReviwed2Status(survey.getReview2Status());
            if (!survey.getReview1Status().equals(Constant.PENDING.toString())) {
                surveyDto.setReviwer1(survey.getReview1User().getAlias());
                surveyDto.setReviwed1On(
                    survey.getReviwed1On() != null ? survey.getReviwed1On().toInstant().toEpochMilli() : null);
            }
            if (!survey.getReview2Status().equals(Constant.PENDING.toString())) {
                surveyDto.setReviwer2(survey.getReview2User().getAlias());
                surveyDto.setReviwed2On(
                    survey.getReviwed2On() != null ? survey.getReviwed2On().toInstant().toEpochMilli() : null);
            }
            surveyDtos.add(surveyDto);
        }

        return surveyDtos;
    }

}
