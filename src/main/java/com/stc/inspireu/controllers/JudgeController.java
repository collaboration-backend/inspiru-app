package com.stc.inspireu.controllers;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.EvaluationSubmissionDto;
import com.stc.inspireu.dtos.GetIntakeProgramDto;
import com.stc.inspireu.services.EvaluationSummaryService;
import com.stc.inspireu.services.JudgeService;
import com.stc.inspireu.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${api.version}/management")
@RequiredArgsConstructor
public class JudgeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String CURRENT_USER_OBJECT = "currentUserObject";

    private final JudgeService judgeService;

    private final EvaluationSummaryService evaluationSummaryService;

    @GetMapping("judges/intakePrograms")
    public ResponseEntity<Object> judgesIntakePrograms(@RequestParam(defaultValue = "0") Integer pageNo,
                                                       @RequestParam(defaultValue = "50") Integer pageSize,
                                                       @RequestParam(defaultValue = "asc") String sortDir,
                                                       HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize);

        List<GetIntakeProgramDto> list = judgeService.judgesIntakePrograms(currentUserObject, paging);

        Page<GetIntakeProgramDto> page = new PageImpl<>(list);

        return ResponseWrapper.response(page);
    }

    @GetMapping("judges/intakePrograms/evaluation-queue")
    public ResponseEntity<Object> evaluationNextItemInQueue(HttpServletRequest httpServletRequest) {
        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);
        Map<String, Object> d = judgeService.evaluationNextItemInQueue(currentUserObject);
        return ResponseWrapper.response(d);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/submissions/{submissionId}/{phase}")
    public ResponseEntity<Object> evaluationDetails(@PathVariable Long intakeProgramId,
                                                    @PathVariable Long submissionId,
                                                    @PathVariable String phase,
                                                    HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Map<String, Object> d = judgeService.submissionDetails(currentUserObject, intakeProgramId, submissionId, phase);

        return ResponseWrapper.response(d);
    }


    @PutMapping("judges/evaluations/bootcamps")
    public ResponseEntity<Object> bootcampEvaluationSubmission(@Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto,
                                                               BindingResult bindingResult,
                                                               HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        Object obj = evaluationSummaryService.bootcampEvaluationSubmission(currentUserObject, evaluationSubmissionDto);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }

    @PutMapping("judges/evaluations/assessments")
    public ResponseEntity<Object> assessmentEvaluationSubmission(@Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto,
                                                                 BindingResult bindingResult,
                                                                 HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Object obj = evaluationSummaryService.assessmentEvaluationSubmission(currentUserObject,
            evaluationSubmissionDto);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }

    @PutMapping("judges/evaluations/bootcamps/exit")
    public ResponseEntity<Object> bootcampEvaluationExit(@Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto,
                                                         BindingResult bindingResult,
                                                         HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }
        Object obj = evaluationSummaryService.bootcampEvaluationExit(currentUserObject, evaluationSubmissionDto);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }

    @PutMapping("judges/evaluations/assessments/exit")
    public ResponseEntity<Object> assessmentEvaluationExit(@Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto,
                                                           BindingResult bindingResult,
                                                           HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Object obj = evaluationSummaryService.assessmentEvaluationExit(currentUserObject, evaluationSubmissionDto);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }

    @PutMapping("judges/evaluations/submissions/{submissionId}/{phase}/proceedSummay")
    public ResponseEntity<Object> createSummary(@PathVariable Long submissionId,
                                                @PathVariable String phase,
                                                HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Object obj = evaluationSummaryService.createSummary(currentUserObject, submissionId, phase);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }

    @PutMapping("judges/evaluations/submissions/{submissionId}/{phase}/doLater")
    public ResponseEntity<Object> doLater(@PathVariable Long submissionId,
                                          @PathVariable String phase,
                                          @Valid @RequestBody EvaluationSubmissionDto evaluationSubmissionDto,
                                          BindingResult bindingResult,
                                          HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        if (bindingResult.hasErrors()) {
            return ResponseWrapper.create400ResponseFromValidationErrors(bindingResult.getAllErrors());
        }

        Object obj = evaluationSummaryService.doLater(currentUserObject, submissionId, phase, evaluationSubmissionDto);

        if (obj instanceof String) {
            return ResponseWrapper.response((String) obj, "", HttpStatus.BAD_REQUEST);
        }

        return ResponseWrapper.response(obj);
    }


    @GetMapping("judges/evaluations/status")
    public ResponseEntity<Object> currentEvaluationStatus(HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Map<String, Object> d = judgeService.currentEvaluationStatus(currentUserObject);

        return ResponseWrapper.response(d);
    }


    @GetMapping("judges/intakePrograms/{intakeProgramId}/assessments/evaluations")
    public ResponseEntity<?> judgeAssessementEvaluations(@RequestParam(defaultValue = "0") Integer pageNo,
                                                         HttpServletRequest httpServletRequest,
                                                         @RequestParam(defaultValue = "100") Integer pageSize,
                                                         @RequestParam(defaultValue = "asc") String sortDir,
                                                         @RequestParam(defaultValue = "createdOn") String sortBy,
                                                         @RequestParam(defaultValue = "") String filterBy,
                                                         @RequestParam(defaultValue = "") String filterKeyword,
                                                         @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeAssessementEvaluations(currentUserObject, intakeProgramId, paging, filterKeyword,
            filterBy);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/bootcamps/evaluations")
    public ResponseEntity<?> judgeBootcampEvaluations(@RequestParam(defaultValue = "0") Integer pageNo,
                                                      HttpServletRequest httpServletRequest,
                                                      @RequestParam(defaultValue = "100") Integer pageSize,
                                                      @RequestParam(defaultValue = "asc") String sortDir,
                                                      @RequestParam(defaultValue = "createdOn") String sortBy,
                                                      @RequestParam(defaultValue = "") String filterBy,
                                                      @RequestParam(defaultValue = "") String filterKeyword,
                                                      @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeBootcampEvaluations(currentUserObject, intakeProgramId, paging, filterKeyword,
            filterBy);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/assessments/applications")
    public ResponseEntity<?> judgeAssessementApps(@RequestParam(defaultValue = "0") Integer pageNo,
                                                  HttpServletRequest httpServletRequest,
                                                  @RequestParam(defaultValue = "100") Integer pageSize,
                                                  @RequestParam(defaultValue = "asc") String sortDir,
                                                  @RequestParam(defaultValue = "createdOn") String sortBy,
                                                  @RequestParam(defaultValue = "") String filterBy,
                                                  @RequestParam(defaultValue = "") String filterKeyword,
                                                  @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeAssessementApps(currentUserObject, intakeProgramId, paging, filterKeyword, filterBy);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/bootcamps/applications")
    public ResponseEntity<?> judgeBootcampApps(@RequestParam(defaultValue = "0") Integer pageNo,
                                               HttpServletRequest httpServletRequest,
                                               @RequestParam(defaultValue = "100") Integer pageSize,
                                               @RequestParam(defaultValue = "asc") String sortDir,
                                               @RequestParam(defaultValue = "createdOn") String sortBy,
                                               @RequestParam(defaultValue = "") String filterBy,
                                               @RequestParam(defaultValue = "") String filterKeyword,
                                               @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeBootcampApps(currentUserObject, intakeProgramId, paging, filterKeyword, filterBy);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/evaluations/{phase}/summay/status")
    public ResponseEntity<?> getProceedToSummayStatus(HttpServletRequest httpServletRequest,
                                                      @PathVariable Long intakeProgramId,
                                                      @PathVariable String phase) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return judgeService.getProceedToSummayStatus(currentUserObject, intakeProgramId, phase);

    }

    @PutMapping("judges/intakePrograms/{intakeProgramId}/evaluations/{phase}/finalSummay/status")
    public ResponseEntity<Object> generateSummaryAssessmentBootcamp(@PathVariable Long intakeProgramId,
                                                                    @PathVariable String phase,
                                                                    HttpServletRequest httpServletRequest) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return judgeService.generateSummaryAssessmentBootcamp(currentUserObject, intakeProgramId, phase);

    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/evaluations/{phase}/finalSummay/status")
    public ResponseEntity<?> getFinalSummayStatus(HttpServletRequest httpServletRequest,
                                                  @PathVariable Long intakeProgramId,
                                                  @PathVariable String phase) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return judgeService.getFinalSummayStatus(currentUserObject, intakeProgramId, phase);

    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/evaluations/{phase}/allSummary/status")
    public ResponseEntity<?> getAllFinalSummayStatus(HttpServletRequest httpServletRequest,
                                                     @PathVariable Long intakeProgramId,
                                                     @PathVariable String phase) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        return judgeService.getAllFinalSummayStatus(currentUserObject, intakeProgramId, phase);

    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/assessments/allSummary")
    public ResponseEntity<?> judgeAssessmentAllSummary(@RequestParam(defaultValue = "0") Integer pageNo,
                                                       HttpServletRequest httpServletRequest,
                                                       @RequestParam(defaultValue = "100") Integer pageSize,
                                                       @RequestParam(defaultValue = "asc") String sortDir,
                                                       @RequestParam(defaultValue = "createdOn") String sortBy,
                                                       @RequestParam(defaultValue = "") String filterBy,
                                                       @RequestParam(defaultValue = "") String filterKeyword,
                                                       @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeAssessmentAllSummary(currentUserObject, intakeProgramId, paging, filterKeyword,
            filterBy);
    }

    @GetMapping("judges/intakePrograms/{intakeProgramId}/bootcamps/allSummary")
    public ResponseEntity<?> judgeBootcampAllSummary(@RequestParam(defaultValue = "0") Integer pageNo,
                                                     HttpServletRequest httpServletRequest,
                                                     @RequestParam(defaultValue = "100") Integer pageSize,
                                                     @RequestParam(defaultValue = "asc") String sortDir,
                                                     @RequestParam(defaultValue = "createdOn") String sortBy,
                                                     @RequestParam(defaultValue = "") String filterBy,
                                                     @RequestParam(defaultValue = "") String filterKeyword,
                                                     @PathVariable Long intakeProgramId) {

        CurrentUserObject currentUserObject = (CurrentUserObject) httpServletRequest.getAttribute(CURRENT_USER_OBJECT);

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        }
        return judgeService.judgeBootcampAllSummary(currentUserObject, intakeProgramId, paging, filterKeyword,
            filterBy);
    }

}
