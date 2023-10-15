package com.stc.inspireu.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

//import com.stc.inspireu.dtos.AcademyRoomGetRequestDto;
import com.stc.inspireu.models.AcademyRoom;
import com.stc.inspireu.models.Startup;
import com.stc.inspireu.models.User;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.utils.RoleName;

public class AcademyRoomsSpecification {

	public static Specification<AcademyRoom> getAcademyRoomsForManagementByStatus(User user, String academyRoomStatus,
			Set<Long> academyRoomIds) {
		return (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();
			List<Predicate> subPredicates = new ArrayList<>();

			Predicate predicateForNew = criteriaBuilder.equal(root.get("status"), Constant.NEW.toString());
			Predicate predicateForDraft = criteriaBuilder.equal(root.get("status"), Constant.DRAFT.toString());

			Predicate predicateForNewOrDraft = criteriaBuilder.or(predicateForNew, predicateForDraft);

			Subquery<Long> sub = query.subquery(Long.class);
			Root<AcademyRoom> subRoot = sub.from(AcademyRoom.class);
			Predicate predicateForRefAcademyRoom = criteriaBuilder.equal(root.get("id"),
					subRoot.get("refAcademyRoom").get("id"));
			Predicate predicateForRefAcademyRoomInprogress = criteriaBuilder.equal(subRoot.get("status"),
					Constant.IN_PROGRESS.toString());

			Predicate predicateForRefAcademyRoomComplete = criteriaBuilder.equal(subRoot.get("status"),
					Constant.COMPLETE.toString());

			Predicate predicateForNullStartup = criteriaBuilder.isNull(subRoot.get("startup"));
			Predicate predicateForNotNullRefAcademyRoomStartup = criteriaBuilder.not(predicateForNullStartup);

//            Predicate predicateForCreator = criteriaBuilder.equal(root.get("createdUser").get("id"), user.getId());
//
//            Predicate predicateForSharedMembersPermission = criteriaBuilder.isMember(user, root.get("sharedUser"));
//
//            Predicate predicateForCreatorOrSharedMember = criteriaBuilder.or(predicateForCreator,
//                    predicateForSharedMembersPermission);

			Predicate predicateForCreatorOrSharedMember = criteriaBuilder.and(root.get("id").in(academyRoomIds));

			Predicate predicateForSuperAdmin = criteriaBuilder
					.equal(criteriaBuilder.literal(user.getRole().getRoleName().toString()), RoleName.ROLE_SUPER_ADMIN);

			Predicate predicateForCreatorOrSharedMemberOrSuperAdmin = criteriaBuilder
					.or(predicateForCreatorOrSharedMember, predicateForSuperAdmin);

			Subquery<Long> subStartupCount = query.subquery(Long.class);
			Root<Startup> subStartupRoot = subStartupCount.from(Startup.class);

			subStartupCount.select(criteriaBuilder.count(subStartupRoot.get("id")));

			if (academyRoomStatus.equals(Constant.NEW.toString())
					|| academyRoomStatus.equals(Constant.DRAFT.toString())) {

				subPredicates.add(predicateForRefAcademyRoom);
				subPredicates.add(predicateForNotNullRefAcademyRoomStartup);
				sub.select(subRoot.get("refAcademyRoom").get("id"));
				sub.where(subPredicates.toArray(new Predicate[subPredicates.size()]));
				Predicate predicateForExludeReferences = criteriaBuilder.in(root.get("id")).value(sub).not();
				predicates.add(predicateForNewOrDraft);
				predicates.add(predicateForExludeReferences);
				predicates.add(criteriaBuilder.isNull(root.get("startup")));
				predicates.add(predicateForCreatorOrSharedMemberOrSuperAdmin);

			}

			if (academyRoomStatus.equals(Constant.IN_PROGRESS.toString())) {
				Subquery<Long> subInProgessCount = query.subquery(Long.class);
				Root<AcademyRoom> subInProgessRoot = subInProgessCount.from(AcademyRoom.class);
				Predicate predicateForRefAcademyRoomInProgress = criteriaBuilder.equal(root.get("id"),
						subInProgessRoot.get("refAcademyRoom").get("id"));

				Predicate predicateForNotNullRefAcademyRoomStartupInprogressCount = criteriaBuilder
						.not(criteriaBuilder.isNull(subInProgessRoot.get("startup")));
				subInProgessCount.select(criteriaBuilder.count(subInProgessRoot.get("refAcademyRoom").get("id")));
				subInProgessCount.where(criteriaBuilder.and(predicateForRefAcademyRoomInProgress,
						predicateForNotNullRefAcademyRoomStartupInprogressCount));
				// subInProgessCount.where(criteriaBuilder.and(predicateForRefAcademyRoomInProgress,predicateForRefAcademyRoomInprogressCount,predicateForNotNullRefAcademyRoomStartupInprogressCount));
				Predicate predicateForStartupCountCheck = criteriaBuilder.greaterThan(subStartupCount,
						subInProgessCount);
				subPredicates.add(predicateForRefAcademyRoom);
				subPredicates.add(predicateForNotNullRefAcademyRoomStartup);
				subPredicates
						.add(criteriaBuilder.or(predicateForRefAcademyRoomInprogress, predicateForStartupCountCheck));

				sub.select(subRoot.get("refAcademyRoom").get("id"));
				sub.where(subPredicates.toArray(new Predicate[subPredicates.size()]));
				Predicate predicateForIncludeReferences = criteriaBuilder.in(root.get("id")).value(sub);
				predicates.add(predicateForNew);
				predicates.add(predicateForIncludeReferences);
				predicates.add(criteriaBuilder.isNull(root.get("startup")));
				predicates.add(predicateForCreatorOrSharedMemberOrSuperAdmin);
			}

			if (academyRoomStatus.equals(Constant.COMPLETE.toString())) {

				subPredicates.add(predicateForRefAcademyRoom);
				subPredicates.add(predicateForRefAcademyRoomComplete);
				subPredicates.add(predicateForNotNullRefAcademyRoomStartup);
				sub.select(criteriaBuilder.count(subRoot.get("refAcademyRoom").get("id")));
				sub.where(subPredicates.toArray(new Predicate[subPredicates.size()]));
				Predicate predicateForStartupMatchingCount = criteriaBuilder.equal(subStartupCount, sub);
				predicates.add(predicateForNew);
				predicates.add(predicateForStartupMatchingCount);
				predicates.add(criteriaBuilder.isNull(root.get("startup")));
				predicates.add(predicateForCreatorOrSharedMemberOrSuperAdmin);
			}

			if (academyRoomStatus.equals(Constant.ALL.toString())) {
				predicates.add(criteriaBuilder.isNull(root.get("startup")));
				predicates.add(predicateForNew);
				predicates.add(predicateForCreatorOrSharedMemberOrSuperAdmin);
			}

			if (predicates == null || predicates.size() == 0) {
				return null;
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		};

	}

}
