package com.stc.inspireu.enums;

public enum Activity {
    LOGIN_PAGE_LOADED("LOGIN_PAGE_LOADED"),
    REGISTRATION_FORM_LOADED("REGISTRATION_FORM_LOADED"),
    REGISTRATION_FORM_SUBMITTED("REGISTRATION_FORM_SUBMITTED");

    private final String text;

    Activity(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
