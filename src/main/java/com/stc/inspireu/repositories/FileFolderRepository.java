package com.stc.inspireu.repositories;

import com.stc.inspireu.models.FileFolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
public interface FileFolderRepository
		extends PagingAndSortingRepository<FileFolder, Long>, JpaSpecificationExecutor<FileFolder> {

	Page<FileFolder> findByIdIn(Set<Long> fileFolderIds, Pageable paging);

	Page<FileFolder> findByRefFileFolder_IdIsNull(Pageable paging);

	Page<FileFolder> findByRefFileFolder_Id(Long parentFolderId, Pageable paging);

	Page<FileFolder> findByRefFileFolderIsNull(Pageable paging);

	@Modifying // to mark delete or update query
	@Query(value = "DELETE FROM FileFolder e WHERE e.id = :fileFolderId")
	void removeById(Long fileFolderId);

	List<FileFolder> findByNameAndParentFolder(String capitalize, String string);

	Optional<FileFolder> findByUid(String parentFolderId);

	default Page<FileFolder> findFileFolder(Long parentId, Long userId, Set<Long> rIds, String filterBy,
			String filterKeyword, Pageable paging) {
		return (Page<FileFolder>) findAll(
				FileFolderSpecification.findFileFolder(parentId, userId, rIds, filterBy, filterKeyword), paging);
	}

	default Page<FileFolder> findStartupFileFolder(Long parentId, Long userId, Set<Long> rIds, String filterBy,
			String filterKeyword, Pageable paging) {
		return (Page<FileFolder>) findAll(
				FileFolderSpecification.findStartupFileFolder(parentId, userId, rIds, filterBy, filterKeyword), paging);
	}

	long countByRefFileFolderIsNull();

	long countByRefFileFolder_Id(Long id);

}

class FileFolderSpecification {

	public static Specification<FileFolder> findFileFolder(Long parentId, Long userId, Set<Long> rIds, String filterBy,
			String filterKeyword) {

		return new Specification<FileFolder>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<FileFolder> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<Predicate>();

				if (parentId == null) {

					Predicate p1 = criteriaBuilder.isNull(root.get("refFileFolder"));

					Predicate p2 = criteriaBuilder.equal(root.get("createdUser").get("id"), userId);

					Predicate p3 = criteriaBuilder.and(p1, p2);

					if (rIds == null || rIds.size() == 0) {
						rIds.add(0L);
					}

					Path<Long> ids = root.<Long>get("id");
					Predicate p4 = ids.in(rIds);

					Predicate p5 = criteriaBuilder.isTrue(root.get("isPublic"));

					Predicate p6 = criteriaBuilder.or(p3, p4, p5);

					if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
						Predicate p10 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
								"%" + filterKeyword.toLowerCase() + "%");

						Predicate p7 = criteriaBuilder.and(p6, p10);

						predicates.add(p7);
					} else {
						predicates.add(p6);
					}

				} else {

					Predicate p1 = criteriaBuilder.equal(root.get("refFileFolder").get("id"), parentId);

					if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
						Predicate p10 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
								"%" + filterKeyword.toLowerCase() + "%");

						Predicate p7 = criteriaBuilder.and(p1, p10);

						predicates.add(p7);
					} else {
						predicates.add(p1);
					}

				}

				Predicate[] predicatesArray = new Predicate[predicates.size()];

				return criteriaBuilder.and(predicates.toArray(predicatesArray));
			}

		};

	}

	public static Specification<FileFolder> findStartupFileFolder(Long parentId, Long userId, Set<Long> rIds,
			String filterBy, String filterKeyword) {

		return new Specification<FileFolder>() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<FileFolder> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<Predicate>();

				if (parentId == null) {

					Predicate p1 = criteriaBuilder.isNull(root.get("refFileFolder"));

					Predicate p2 = criteriaBuilder.equal(root.get("createdUser").get("id"), userId);

					Predicate p3 = criteriaBuilder.and(p1, p2);

					if (rIds == null || rIds.size() == 0) {
						rIds.add(0L);
					}

					Path<Long> ids = root.<Long>get("id");
					Predicate p4 = ids.in(rIds);

					Predicate p5 = criteriaBuilder.isTrue(root.get("isPublic"));

					Predicate p6 = criteriaBuilder.or(p3, p4, p5);

					if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
						Predicate p10 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
								"%" + filterKeyword.toLowerCase() + "%");

						Predicate p7 = criteriaBuilder.and(p6, p10);

						predicates.add(p7);
					} else {
						predicates.add(p6);
					}
				} else {

					Predicate p1 = criteriaBuilder.equal(root.get("refFileFolder").get("id"), parentId);

					if (!StringUtils.isEmpty(filterBy) && !StringUtils.isEmpty(filterKeyword)) {
						Predicate p10 = criteriaBuilder.like(criteriaBuilder.lower(root.get(filterBy)),
								"%" + filterKeyword.toLowerCase() + "%");

						Predicate p7 = criteriaBuilder.and(p1, p10);

						predicates.add(p7);
					} else {
						predicates.add(p1);
					}
				}

				Predicate[] predicatesArray = new Predicate[predicates.size()];

				return criteriaBuilder.and(predicates.toArray(predicatesArray));
			}

		};

	}

}
