package com.stc.inspireu.enums;

public enum Constant {
    BOOKED, CANCELLED, PAID, PENDING, BOOK_SLOT, ONE2ONE_MEETING, ACCEPTED, REGISTERED, INVITAION_SEND,
    STARTUP_MEMBER_INVITATION, STARTUP_INVITATION, USER_INVITATION, BLOCKED, ARCHIVE, POSTPONED,

    UPLOADED, STARTUP_DUEDILIGENCE_INVITATION,

    //////////////////////// academy rooms status , form template
    //////////////////////// status/////////////////////////////
    IN_PROGRESS, COMPLETE, NEW, PUBLISHED, NOT_PUBLISHED, DRAFT, ALL, // to get all status

    /////////////////////////////// form template types,workshop submissions
    /////////////////////////////// fileType////////////////////////////////////
    SURVEY, FEEDBACK, PROGRESS_REPORT,

    //////////////////////// progress reports, survey
    //////////////////////// status/////////////////////////////
    SUBMITTED, NOT_SUBMITTED,

    //////////////////////// attendance status/////////////////////////////
    PRESENT, LATE, ABSENT, OFFDAY,

    //////////////////////// assignment review status/////////////////////////////
    APPROVED, RESUBMIT,

    //////////////////////// program name/////////////////////////

    INSPIREU, INPACTU,

    //////////////////////// workshop submissions fileType/////////////////////////

    ASSIGNMENT,

    //////////////////////// intake program status/////////////////////////

    ACTIVE, IN_ACTIVE,

    /////////////////////// message types/////////////////////////

    INFO, WARNING, SUCCESS, ERROR, REGISTRATION, ASSESSMENT, EVALUATION_STATE, START, STOP, EVALUATION_START,
    ASSESSMENT_EVALUATE, BOOTCAMP, ASSESSMENT_EVALUATION_START,ASSESSMENT_EVALUATION_COMPLETED, SCREENING,SCREENING_EVALUATION_START, SCREENING_EVALUATION_STOP, SCREENING_EVALUATION_COMPLETED, BOOTCAMP_EVALUATION_START, ASSESSMENT_EVALUATION,
    STARTUP, ASSESSMENT_EVALUATION_STOP, BOOTCAMP_EVALUATION_STOP,BOOTCAMP_EVALUATION_COMPLETED, BOOTCAMP_EVALUATE, BOOTCAMP_SELECTED, SUMMARY,
    BOOTCAMP_END, BOOTCAMP_FINISH, ASSESSMENT_END, STARTUP_EMAIL, STARTUP_NAME, ACCOUNT, GENERAL, JUDGE, MANAGEMENT,
    OTHER, COACH_AND_TRAINER, RECEPTIONIST, STARTUP_ADMIN, STARTUP_MEMBER, SUPER_ADMIN, INVITE_LINK, EMAIL,
    NON_STC_JUDGES, STC_JUDGES, COACHES_AND_TRAINERS, MANAGEMENT_TEAM_MEMBER, MANAGEMENT_TEAM_ADMIN, STARTUPS_ADMIN,
    STARTUPS_MEMBER, PARTNER,

    MIDNIGHT_CRON_LOCK, UNLOCKED, LOCKED, TOTAL_STARTUPS_INCUBATED, FILES_ALLOWDED, FILE_SIZE, EXSISTING_STARTUP,
    EXISTING_INTAKE,

    POST_LOGIN_,
    WS //shot of workshop session

}
