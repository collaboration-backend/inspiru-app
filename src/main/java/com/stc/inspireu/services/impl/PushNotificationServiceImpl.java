package com.stc.inspireu.services.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.stc.inspireu.services.PushNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.stc.inspireu.enums.Constant;

@Service
public class PushNotificationServiceImpl implements PushNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ConcurrentHashMap<String, SseEmitter> judgeClients = new ConcurrentHashMap<>();

	@Override
	public SseEmitter registerJudgeClient(String clientId, Long timeout) {
		if (judgeClients.containsKey(clientId)) {
			try {
				judgeClients.get(clientId).send("heartbeat");
				return judgeClients.get(clientId);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				judgeClients.remove(clientId);
			}
		}

		SseEmitter emitter = new SseEmitter(timeout);

		emitter.onCompletion(() -> {
			judgeClients.remove(clientId);
			LOGGER.debug("Subscription completed: there are " + judgeClients.size() + " subscribers");
		});

		emitter.onError(error -> {
			judgeClients.remove(clientId);
			LOGGER.debug("Subscription crashed: there are " + judgeClients.size() + " subscribers");
		});

		emitter.onTimeout(() -> {
			judgeClients.remove(clientId);
			LOGGER.debug("Subscription timed out: there are " + judgeClients.size() + " subscribers");
		});

		judgeClients.put(clientId, emitter);

		LOGGER.debug("Subscription added: there are " + judgeClients.size() + " subscribers");

		return emitter;
	}

	@Async("asyncExecutor")
	public void sendEventToJudgeClients(String type, String data) {

		LOGGER.debug("befor cleaning judgeClients " + judgeClients.size());

		Set<String> deadEmitters = new HashSet<String>();

		for (Map.Entry<String, SseEmitter> entry : judgeClients.entrySet()) {

			try {

				entry.getValue().send(data);

			} catch (Exception e) {
				deadEmitters.add(entry.getKey());
			}
		}

		judgeClients.keySet().removeAll(deadEmitters);

		LOGGER.debug("after cleaning judgeClients " + judgeClients.size());
		LOGGER.debug("dead judgeClients " + deadEmitters.size());

	}

	@Async("asyncExecutor")
	@Override
	public void killDeadSSE() {

		try {
			LOGGER.debug("befor cleaning judgeClients " + judgeClients.size());

			Set<String> deadEmitters = new HashSet<String>();

			for (Map.Entry<String, SseEmitter> entry : judgeClients.entrySet()) {

				try {
					entry.getValue().send("heartbeat");
				} catch (Exception e) {
					deadEmitters.add(entry.getKey());
				}
			}

			LOGGER.debug("after cleaning judgeClients " + judgeClients.size());
			LOGGER.debug("dead judgeClients " + deadEmitters.size());

			judgeClients.keySet().removeAll(deadEmitters);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
		}

	}

	@Async("asyncExecutor")
	@Override
	public void eventBroadcast(String eventType, String data) {
		if (eventType.equals(Constant.ASSESSMENT_EVALUATION_START.toString())) {

			sendEventToJudgeClients(eventType, data);

		} else if (eventType.equals(Constant.ASSESSMENT_EVALUATION_STOP.toString())) {

			sendEventToJudgeClients(eventType, data);

		} else if (eventType.equals(Constant.BOOTCAMP_EVALUATION_START.toString())) {

			sendEventToJudgeClients(eventType, data);

		} else if (eventType.equals(Constant.BOOTCAMP_EVALUATION_STOP.toString())) {

			sendEventToJudgeClients(eventType, data);

		} else {
            sendEventToJudgeClients(eventType, data);
		}

	}

}
