package com.stc.inspireu.repositories;

import com.stc.inspireu.models.Attendance2022;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface Attendance2022Repository extends PagingAndSortingRepository<Attendance2022, Long> {

    List<Attendance2022> findByIntakeProgram_IdAndStartup_IdAndAcademyRoom_IdAndWorkshopSession_Id(Long intakeProgramId,
                                                                                                   Long startupId, Long academyRoomId, Long workshopSessionId);

    boolean existsByIntakeProgram_IdAndStartup_IdAndAcademyRoom_IdAndWorkshopSession_Id(Long intakeProgramId,
                                                                                        Long startupId, Long academyRoomId, Long workshopSessionId);

    List<Attendance2022> findByIntakeProgram_IdAndStartup_IdAndAcademyRoom_Id(Long id, Long id2, Long academyRoomId);
}
