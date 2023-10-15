package com.stc.inspireu.controllers;

import com.stc.inspireu.services.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/${api.version}/sse")
@RequiredArgsConstructor
public class PushNotificationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PushNotificationService pushNotificationService;

    public SseEmitter sse(@PathVariable String clientId) {
        return pushNotificationService.registerJudgeClient(clientId, 3700000L);
    }

}
