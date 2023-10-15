package com.stc.inspireu.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PushNotificationService {

	void killDeadSSE();

	SseEmitter registerJudgeClient(String clientId, Long timeout);

	void eventBroadcast(String string, String string2);

}
