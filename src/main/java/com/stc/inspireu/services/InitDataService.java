package com.stc.inspireu.services;

public interface InitDataService {

    void createSuperAdmin(String superadminEmail, String superadminPassword);

    void createEmailTemplates();

    void existinIntake();

}
