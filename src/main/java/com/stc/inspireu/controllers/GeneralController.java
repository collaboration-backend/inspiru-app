package com.stc.inspireu.controllers;

import com.stc.inspireu.services.GeneralService;
import com.stc.inspireu.services.IntakeProgramService;
import com.stc.inspireu.utils.Encryption;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/${api.version}/general")
@RequiredArgsConstructor
public class GeneralController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final IntakeProgramService intakeProgramService;

    private final GeneralService generalService;


    @GetMapping(value = "ongoing-intakes")
    public ResponseEntity<?> registrationLinks(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("links", intakeProgramService.onGoingIntakes());
        generalService.saveActivityLog(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "cdn/{filePath}")
    public ResponseEntity<Resource> publicFileRender(@PathVariable String filePath) throws Exception {
        filePath = Encryption.decrypt(filePath);
        File file = generalService.findFile(filePath);
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filePath.contains("/")
            ? filePath.split("/")[1] : filePath));
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return ResponseEntity.ok()
            .headers(header)
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(Files.newInputStream(file.toPath())));

    }
}
