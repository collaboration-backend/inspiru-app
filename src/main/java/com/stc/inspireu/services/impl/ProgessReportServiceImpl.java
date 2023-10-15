package com.stc.inspireu.services.impl;

import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.jpa.projections.ProjectProgressReportFile;
import com.stc.inspireu.mappers.ProgressReportMapper;
import com.stc.inspireu.models.IntakeProgram;
import com.stc.inspireu.models.ProgressReport;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.IntakeProgramRepository;
import com.stc.inspireu.repositories.ProgessReportRepository;
import com.stc.inspireu.repositories.ProgressReportFileRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.ProgessReportService;
import com.stc.inspireu.utils.ConstantUtility;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ProgessReportServiceImpl implements ProgessReportService {

    private final UserRepository userRepository;
    private final ProgessReportRepository progessReportRepository;
    private final NotificationService notificationService;
    private final Utility utility;
    private final IntakeProgramRepository intakeProgramRepository;
    private final ProgressReportFileRepository progressReportFilesRepository;
    private final FileAdapter fileAdapter;
    private final ProgressReportMapper progressReportMapper;

    @Override
    @Transactional
    public Object createProgessReport(CurrentUserObject currentUserObject,
                                      ProgressReportPostRequestDto progressReportRequest) {
        User userObj = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        ProgressReport progressReport = progessReportRepository.findByStartup_IdAndMonthAndYear(
            userObj.getStartup().getId(), progressReportRequest.getMonth(), progressReportRequest.getYear());
        if (progressReport == null || !progressReport.getStatus().equals(Constant.SUBMITTED.toString())) {
            if (progressReport == null) {
                progressReport = new ProgressReport();
                progressReport.setCreatedUser(userObj);
            }
            progressReport.setReportName(progressReportRequest.getReportName());
            progressReport.setMonth(progressReportRequest.getMonth());
            progressReport.setYear(progressReportRequest.getYear());
            progressReport.setStartup(userObj.getStartup());
            progressReport.setSubmittedUser(userObj);
            progressReport.setStatus(progressReportRequest.getIsSubmitted() ? Constant.SUBMITTED.toString()
                : Constant.NOT_SUBMITTED.toString());
            progressReport.setJsonReportDetail(progressReportRequest.getJsonProgressReportDetail());
            progressReport = progessReportRepository.save(progressReport);
            return progressReport;
        }
        return null;
    }

    @Override
    @Transactional
    public Object createProgessReportMonthly(CurrentUserObject currentUserObject,
                                             @Valid PostProgressReportDto postProgressReportDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Map<String, Object> data = null;
        if (user.getStartup() != null) {
            ProgressReport pr = progessReportRepository.findByStartup_IdAndYearAndMonth(user.getStartup().getId(),
                postProgressReportDto.getYear(), postProgressReportDto.getMonth());
            if (pr == null) {
                List<ProgressReport> prs = progessReportRepository.findByIntakeProgram_IdAndStatusAndStartupIdIsNull(
                    user.getStartup().getIntakeProgram().getId(), Constant.PUBLISHED.toString());
                ProgressReport progressReport = new ProgressReport();
                intakeProgramRepository
                    .findById(user.getStartup().getIntakeProgram().getId()).ifPresent(progressReport::setIntakeProgram);
                progressReport.setMonth(postProgressReportDto.getMonth());
                progressReport.setYear(postProgressReportDto.getYear());
                if (!prs.isEmpty()) {
                    ProgressReport e = prs.get(0);
                    progressReport.setJsonReportDetail(e.getJsonReportDetail());
                    progressReport.setRefProgressReportId(e.getId());
                } else {
                    progressReport.setJsonReportDetail(ConstantUtility.PROGRESS_REPORT_BASIC_TEMPLATE);
                }
                progressReport.setReportName("Report_Startup_" + user.getStartup().getId() + "_"
                    + utility.getMonth(postProgressReportDto.getMonth()) + "_" + postProgressReportDto.getYear());
                progressReport.setStartup(user.getStartup());
                progressReport.setStatus(Constant.NOT_SUBMITTED.toString());
                progressReport.setCreatedUser(user);
                progressReport.setSubmittedUser(user);
                progressReport.setFundraiseInvestment(0);
                progressReport.setMarketValue(0);
                progressReport.setProfitLoss(0);
                progressReport.setProfitLossExpected(0);
                progressReport.setRevenue(0);
                progressReport.setRevenueExpected(0);
                progressReport.setSales(0);
                progressReport.setSalesExpected(0);
                progressReport.setUsers(0);
                progressReport.setUsersExpected(0);
                progressReport.setFteEmployees(0);
                progressReport.setFteEmployeesExpected(0);
                progressReport.setPteEmployees(0);
                progressReport.setPteEmployeesExpected(0);
                progressReport.setFreelancers(0);
                progressReport.setFreelancersExpected(0);
                progressReport.setLoans(0);
                progressReport.setHighGrossMerchandise(0);
                progessReportRepository.save(progressReport);
                data = new HashMap<>();
                data.put("progressReportId", progressReport.getId());
                data.put("month", progressReport.getMonth());
                data.put("year", progressReport.getYear());
                data.put("jsonForm", progressReport.getJsonReportDetail());
                data.put("startupId", user.getStartup().getId());
                data.put("status", progressReport.getStatus());
                data.put("reportName", progressReport.getReportName());
            } else {
                data = new HashMap<>();
                data.put("progressReportId", pr.getId());
                data.put("month", pr.getMonth());
                data.put("year", pr.getYear());
                data.put("jsonForm", pr.getJsonReportDetail());
                data.put("startupId", user.getStartup().getId());
                data.put("status", pr.getStatus());
                data.put("reportName", pr.getReportName());
            }
        }
        return data;
    }

    @Override
    @Transactional
    public Object updateProgessReportMonthly(CurrentUserObject currentUserObject,
                                             PutProgressReportDto putProgressReportDto, Long progressReportId) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (user.getStartup() != null) {
            progessReportRepository.findByIdAndStartup_IdAndYearAndMonth(progressReportId,
                    user.getStartup().getId(), putProgressReportDto.getYear(), putProgressReportDto.getMonth())
                .map(updatedReport -> {
                    if (!updatedReport.getStatus().equals(Constant.SUBMITTED.toString())) {
                        updatedReport.setJsonReportDetail(putProgressReportDto.getJsonForm());
                        updatedReport.setFundraiseInvestment(putProgressReportDto.getFundraiseInvestment());
                        updatedReport.setFundraiseInvestment(putProgressReportDto.getFundraiseInvestment());
                        updatedReport.setMarketValue(putProgressReportDto.getMarketValue());
                        updatedReport.setProfitLoss(putProgressReportDto.getProfitLoss());
                        updatedReport.setProfitLossExpected(putProgressReportDto.getProfitLossExpected());
                        updatedReport.setRevenue(putProgressReportDto.getRevenue());
                        updatedReport.setRevenueExpected(putProgressReportDto.getRevenueExpected());
                        updatedReport.setSales(putProgressReportDto.getSales());
                        updatedReport.setSalesExpected(putProgressReportDto.getSalesExpected());
                        updatedReport.setUsers(putProgressReportDto.getUsers());
                        updatedReport.setUsersExpected(putProgressReportDto.getUsersExpected());
                        updatedReport.setFteEmployees(putProgressReportDto.getFteEmployees());
                        updatedReport.setFteEmployeesExpected(putProgressReportDto.getFteEmployeesExpected());
                        updatedReport.setPteEmployees(putProgressReportDto.getPteEmployees());
                        updatedReport.setPteEmployeesExpected(putProgressReportDto.getPteEmployeesExpected());
                        updatedReport.setFreelancers(putProgressReportDto.getFreelancers());
                        updatedReport.setFreelancersExpected(putProgressReportDto.getFreelancersExpected());
                        updatedReport.setLoans(putProgressReportDto.getLoans());
                        updatedReport.setHighGrossMerchandise(putProgressReportDto.getHighGrossMerchandise());
                        if (putProgressReportDto.getIsSubmitted()) {
                            updatedReport.setStatus(Constant.SUBMITTED.toString());
                            updatedReport.setSubmittedUser(user);
                            notificationService.monthlyProgressReportSubmitted(user);
                        }
                        return progressReportMapper.toProgressReportDto(progessReportRepository.save(updatedReport));
                    }
                    return null;
                });
        }

        return null;
    }

    @Transactional
    @Override
    public ProgressReportDto getProgressReport(CurrentUserObject currentUserObject, Long progressReportId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Optional<ProgressReport> progressReport = progessReportRepository.findById(progressReportId);
            if (progressReport.isPresent()) {
                List<ProjectProgressReportFile> ls = progressReportFilesRepository
                    .findByProgressReport_Id(progressReportId);
                return progressReportMapper.toProgressReportDtoAlongWithReportFile(progressReport.get(), ls);
            }
        }
        return null;
    }

    @Transactional
    @Override
    public List<ProjectProgressReportFile> getProgressReportFiles(CurrentUserObject currentUserObject,
                                                                  Long progressReportId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        AtomicReference<List<ProjectProgressReportFile>> ls = new AtomicReference<>(new ArrayList<>());
        if (user.isPresent() && user.get().getStartup() != null) {
            progessReportRepository.findById(progressReportId)
                .ifPresent(progressReport -> ls.set(progressReportFilesRepository.findByProgressReport_Id(progressReportId)));
        }
        return ls.get();
    }

    @Transactional
    @Override
    public ResponseEntity<?> createManagementProgressReport(CurrentUserObject currentUserObject,
                                                            PostManagementProgressReportDto progressReportMngtDto, String status) {
        User user = userRepository.findById(currentUserObject.getUserId())
            .orElseThrow(() -> ItemNotFoundException.builder("User").build());
        ProgressReport progressReport = new ProgressReport();
        IntakeProgram intakeProgram = null;
        if (progressReportMngtDto.getIntakePgmId().equals(0L)) {
            intakeProgram = intakeProgramRepository.findFirstByOrderByCreatedOnDesc();
        } else {
            Optional<IntakeProgram> b = intakeProgramRepository.findById(progressReportMngtDto.getIntakePgmId());
            if (b.isPresent()) {
                intakeProgram = b.get();
            }
        }
        if (intakeProgram != null) {
            progressReport.setJsonReportDetail(progressReportMngtDto.getJsonForm());
            progressReport.setReportName(progressReportMngtDto.getReportName());
            progressReport.setStatus(status);
            progressReport.setCreatedUser(user);
            progressReport.setIntakeProgram(intakeProgram);
            progressReport.setFundraiseInvestment(0);
            progressReport.setMarketValue(0);
            progressReport.setProfitLoss(0);
            progressReport.setProfitLossExpected(0);
            progressReport.setRevenue(0);
            progressReport.setRevenueExpected(0);
            progressReport.setSales(0);
            progressReport.setSalesExpected(0);
            progressReport.setUsers(0);
            progressReport.setUsersExpected(0);
            progressReport.setFteEmployees(0);
            progressReport.setFteEmployeesExpected(0);
            progressReport.setPteEmployees(0);
            progressReport.setPteEmployeesExpected(0);
            progressReport.setFreelancers(0);
            progressReport.setFreelancersExpected(0);
            progressReport.setLoans(0);
            progressReport.setHighGrossMerchandise(0);
            ProgressReport d = progessReportRepository.save(progressReport);
            Map<String, Object> data = new HashMap<>();
            data.put("intakeProgramId", d.getId());
            data.put("name", d.getReportName());
            data.put("jsonForm", progressReportMngtDto.getJsonForm());
            data.put("status", d.getStatus());
            return ResponseWrapper.response(data);
        }
        return ResponseWrapper.response400("invalid intakeProgramId, may no intake created yet", "intakeProgramId");
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateManagementProgressReport(CurrentUserObject currentUserObject,
                                                            PostManagementProgressReportDto progressReportMngtDto, Long id) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        if (progessReportRepository.existsByIntakeProgram_IdAndReportName(progressReportMngtDto.getIntakePgmId(), progressReportMngtDto.getReportName())) {
            return ResponseWrapper.response400("Duplicate report name", "reportName");
        }
        Optional<ProgressReport> progressReportResult = progessReportRepository.findByIdAndIntakeProgram_Id(id,
            progressReportMngtDto.getIntakePgmId());
        if (progressReportResult.isPresent()) {
            progressReportResult.get().setReportName(progressReportMngtDto.getReportName());
            progressReportResult.get().setJsonReportDetail(progressReportMngtDto.getJsonForm());
            progessReportRepository.save(progressReportResult.get());
            return ResponseWrapper.response("updated", HttpStatus.OK);
        }
        return ResponseWrapper.response("Progress report not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public ResponseEntity<?> updateManagementProgressReportStatus(CurrentUserObject currentUserObject, String status,
                                                                  Long id) {
        userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        Optional<ProgressReport> progressReportResult = progessReportRepository.findById(id);
        if (progressReportResult.isPresent()) {
            ProgressReport progressReport = progressReportResult.get();
            progressReport.setStatus(status);
            progessReportRepository.save(progressReport);
            return ResponseWrapper.response("status updated", HttpStatus.OK);
        }
        return ResponseWrapper.response("Progress report not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public List<ProgressReportDto> getProgressReports(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            List<ProgressReport> list = progessReportRepository.findByStartup_Id(user.get().getStartup().getId());
            return progressReportMapper.toProgressReportDtoList(list);
        }
        return Collections.emptyList();
    }

    @Transactional
    @Override
    public ResponseEntity<?> getManagementProgressReportById(CurrentUserObject currentUserObject,
                                                             Long progressReportId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Optional<ProgressReport> progressReport = progessReportRepository.findById(progressReportId);
            if (progressReport.isPresent()) {
                return ResponseWrapper.response(progressReportMapper.toProgressReportDto(progressReport.get()));
            }
        }
        return null;
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteManagementProgressReportById(CurrentUserObject currentUserObject,
                                                                Long progressReportId) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Optional<ProgressReport> progressReport = progessReportRepository.findById(progressReportId);
            if (progressReport.isPresent()) {
                progessReportRepository.removeProgressReportById(progressReportId);
                return ResponseWrapper.response("deleted", HttpStatus.OK);
            }
        }
        return ResponseWrapper.response("Progress report not found", HttpStatus.NOT_FOUND);
    }

    @Transactional
    @Override
    public Page<ProgressReportDto> getProgressReportMonthlySubmissions(CurrentUserObject currentUserObject,
                                                                       Long intakeProgramId, String filterKeyword, String filterBy, Pageable paging) {
        Page<ProgressReportDto> list;
        Page<ProgressReport> ls;
        if (Objects.nonNull(filterKeyword) && !filterKeyword.isEmpty()) {
            if (filterBy.equals("reportName")) {
                ls = progessReportRepository.findByReportNameContainingIgnoreCaseAndIntakeProgram_IdAndStartupIdNotNull(
                    filterKeyword, intakeProgramId, paging);
            } else if (filterBy.equals("SubmittedBy")) {
                ls = progessReportRepository.getPRByKeyword(intakeProgramId, filterKeyword, paging);
            } else {
                ls = progessReportRepository.findByIntakeProgram_IdAndStartupIdNotNull(intakeProgramId, paging);
            }
        } else {
            ls = progessReportRepository.findByIntakeProgram_IdAndStartupIdNotNull(intakeProgramId, paging);
        }
        list = ls.map(progressReportMapper::toProgressReportDto);
        return list;
    }

    @Transactional
    @Override
    public Object putProgressReports(CurrentUserObject currentUserObject, PutProgressReportDto putProgressReportDto,
                                     Long progressReportId) {
        Optional<User> user = userRepository
            .findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null) {
            Optional<ProgressReport> pr = progessReportRepository.findByIdAndStartup_IdAndYearAndMonth(progressReportId,
                user.get().getStartup().getId(), putProgressReportDto.getYear(), putProgressReportDto.getMonth());
            if (pr.isPresent() && !pr.get().getStatus().equals(Constant.SUBMITTED.toString())) {
                ProgressReport updatedReport = pr.get();
                MultipartFile[] files = putProgressReportDto.getFiles();
                if (files != null) {
                    for (MultipartFile multipartFile : files) {
                        String x = fileAdapter.saveProgressReportFile(
                            user.get().getStartup().getIntakeProgram().getId(), user.get().getStartup().getId(),
                            pr.get().getId(), multipartFile);
                    }
                }
                updatedReport.setJsonReportDetail(putProgressReportDto.getJsonForm());
                updatedReport.setFundraiseInvestment(putProgressReportDto.getFundraiseInvestment());
                updatedReport.setFundraiseInvestment(putProgressReportDto.getFundraiseInvestment());
                updatedReport.setMarketValue(putProgressReportDto.getMarketValue());
                updatedReport.setProfitLoss(putProgressReportDto.getProfitLoss());
                updatedReport.setProfitLossExpected(putProgressReportDto.getProfitLossExpected());
                updatedReport.setRevenue(putProgressReportDto.getRevenue());
                updatedReport.setRevenueExpected(putProgressReportDto.getRevenueExpected());
                updatedReport.setSales(putProgressReportDto.getSales());
                updatedReport.setSalesExpected(putProgressReportDto.getSalesExpected());
                updatedReport.setUsers(putProgressReportDto.getUsers());
                updatedReport.setUsersExpected(putProgressReportDto.getUsersExpected());
                updatedReport.setFteEmployees(putProgressReportDto.getFteEmployees());
                updatedReport.setFteEmployeesExpected(putProgressReportDto.getFteEmployeesExpected());
                updatedReport.setPteEmployees(putProgressReportDto.getPteEmployees());
                updatedReport.setPteEmployeesExpected(putProgressReportDto.getPteEmployeesExpected());
                updatedReport.setFreelancers(putProgressReportDto.getFreelancers());
                updatedReport.setFreelancersExpected(putProgressReportDto.getFreelancersExpected());
                updatedReport.setLoans(putProgressReportDto.getLoans());
                updatedReport.setHighGrossMerchandise(putProgressReportDto.getHighGrossMerchandise());
                if (putProgressReportDto.getIsSubmitted()) {
                    updatedReport.setStatus(Constant.SUBMITTED.toString());
                    updatedReport.setSubmittedUser(user.get());
                }
                ProgressReport e = progessReportRepository.save(updatedReport);
                return progressReportMapper.toProgressReportDto(e);
            }
        }
        return null;

    }

    @Transactional
    @Override
    public ResponseEntity<?> getManagementProgressReports(CurrentUserObject currentUserObject, Pageable paging) {
        Page<ProgressReport> ls = progessReportRepository.findByStartupIdIsNull(paging);
        Page<ProgressReportDto> list = ls.map(progressReportMapper::toProgressReportDto);
        return ResponseWrapper.response(list);

    }

    @Transactional
    @Override
    public ResponseEntity<?> checkProgressReports(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null && user.get().getStartup().getIntakeProgram() != null) {
            List<ProgressReport> pr = progessReportRepository
                .findByIntakeProgram_IdAndStartupIdIsNull(user.get().getStartup().getIntakeProgram().getId());
            ProgressReport pReport;
            if (!pr.isEmpty()) {
                pReport = pr.get(0);
                if (pReport.getStatus().equals(Constant.PUBLISHED.toString())) {
                    return ResponseWrapper.response(progressReportMapper.toProgressReportDto(pReport));
                }
            }
        }
        return ResponseWrapper.response(null);
    }

    @Transactional
    @Override
    public ResponseEntity<?> notifyManagement(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent() && user.get().getStartup() != null && user.get().getStartup().getIntakeProgram() != null) {
            notificationService.notifyManagementToPublishProgressReportTemplate(user.get(),
                user.get().getStartup().getIntakeProgram());
        }
        return ResponseWrapper.response(null);
    }
}
