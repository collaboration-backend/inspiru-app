package com.stc.inspireu.utils;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.NotificationRepository;

@Component
public class NotificationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private NotificationRepository notificationRepository;

	public void invokeNotification(User user) {

	}

}
