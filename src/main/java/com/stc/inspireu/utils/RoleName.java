package com.stc.inspireu.utils;

public class RoleName {

    public static final String ROLE_STARTUPS_ADMIN = "ROLE_STARTUPS_ADMIN";
    public static final String ROLE_STARTUPS_MEMBER = "ROLE_STARTUPS_MEMBER";
    public static final String ROLE_STARTUPS_BENEFICIARY = "ROLE_STARTUPS_BENEFICIARY";
    public static final String ROLE_EXISTING_STARTUPS = "ROLE_EXISTING_STARTUPS";
    public static final String ROLE_NON_STC_JUDGES = "ROLE_NON_STC_JUDGES";
    public static final String ROLE_STC_JUDGES = "ROLE_STC_JUDGES";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_MANAGEMENT_TEAM_ADMIN = "ROLE_MANAGEMENT_TEAM_ADMIN";
    public static final String ROLE_MANAGEMENT_TEAM_MEMBER = "ROLE_MANAGEMENT_TEAM_MEMBER";
    public static final String ROLE_COACHES_AND_TRAINERS = "ROLE_COACHES_AND_TRAINERS";
    public static final String ROLE_PARTNER = "ROLE_PARTNER";

    public enum Value {
        ROLE_STARTUPS_ADMIN, ROLE_STARTUPS_MEMBER, ROLE_STARTUPS_BENEFICIARY, ROLE_EXISTING_STARTUPS, ROLE_NON_STC_JUDGES, ROLE_STC_JUDGES,
        ROLE_SUPER_ADMIN, ROLE_MANAGEMENT_TEAM_ADMIN, ROLE_MANAGEMENT_TEAM_MEMBER, ROLE_COACHES_AND_TRAINERS,
        ROLE_PARTNER
    }

    public static String getAlias(String exp) {
        switch (exp) {
            case ROLE_STARTUPS_ADMIN:
                return "Startup admin";
            case ROLE_STARTUPS_MEMBER:
                return "Startup member";
            case ROLE_EXISTING_STARTUPS:
                return "Existing startup";
            case ROLE_NON_STC_JUDGES:
                return "Non STC judge";
            case ROLE_STC_JUDGES:
                return "STC judge";
            case ROLE_SUPER_ADMIN:
                return "Super admin";
            case ROLE_MANAGEMENT_TEAM_ADMIN:
                return "Management admin";
            case ROLE_MANAGEMENT_TEAM_MEMBER:
                return "Management member";
            case ROLE_COACHES_AND_TRAINERS:
                return "Coach & trainer";
            case ROLE_PARTNER:
                return "Partner";
            case ROLE_STARTUPS_BENEFICIARY:
                return "Beneficiary";
            default:
                return "";
        }
    }
}
