package com.shelflife.project.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Storage;
import java.util.List;

@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
	List<Storage> findByOwnerId(long ownerId);

	/**
	 * @param storageId
	 * @param userId
	 * @return True when you are an accepted member or an owner
	 */
	@Query("""
			SELECT case when (count(s) > 0) then true else false end
			FROM Storage s
			LEFT JOIN s.members sm
			WHERE s.id = :storageId AND (s.owner.id = :userId OR (sm.user.id = :userId AND sm.isAccepted))
			""")
	boolean isMemberOrOwner(long storageId, long userId);

	/**
	 * @param userId
	 * @return A list of storages that you own or you are an accepted member of
	 */
	@Query("""
			SELECT distinct s
			FROM Storage s
			LEFT JOIN s.members sm
			WHERE s.owner.id = :userId OR (sm.user.id = :userId AND sm.isAccepted)
			""")
	List<Storage> findAccessibleStorages(long userId);

	/**
	 * @param userId
	 * @return A list of storages that you own or you are an accepted member of
	 */
	@Query("""
			SELECT distinct s
			FROM Storage s
			LEFT JOIN s.members sm
			WHERE s.owner.id = :userId OR (sm.user.id = :userId AND sm.isAccepted)
			""")
	List<Storage> findAccessibleStorages(long userId, Pageable pageable);
}