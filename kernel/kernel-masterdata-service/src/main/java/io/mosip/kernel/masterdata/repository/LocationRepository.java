package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;

/**
 * This interface is JPA repository class which interacts with database and does
 * the CRUD function. It is extended from {@link BaseRepository}
 * 
 * @author Srinivasan
 * @author uday kumar
 *
 */
@Repository
public interface LocationRepository extends BaseRepository<Location, CodeAndLanguageCodeID> {

	List<Location> findLocationHierarchyByIsDeletedIsNullOrIsDeletedFalse();

	@Query(value = "FROM Location l where l.code=?1 and l.langCode=?2 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findLocationHierarchyByCodeAndLanguageCode(String locCode, String languagecode);

	@Query(value = "FROM Location l where parentLocCode=?1 and langCode=?2 and (l.isDeleted is null or l.isDeleted=false) and l.isActive=true")
	List<Location> findLocationHierarchyByParentLocCodeAndLanguageCode(String parentLocCode, String languageCode);

	@Query(value = "select distinct hierarchy_level, hierarchy_level_name, is_active from master.location where lang_code=?1 and (is_deleted='f' or is_deleted is null) and is_active='t' ", nativeQuery = true)
	List<Object[]> findDistinctLocationHierarchyByIsDeletedFalse(String langCode);

	@Query(value = "FROM Location l where l.code=?1 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findByCode(String locationCode);

	/**
	 *
	 * @param hierarchyName
	 *            - hierarchy name
	 * @return List
	 */
	@Query(value = "FROM Location l where LOWER(l.hierarchyName)=LOWER(?1) AND (l.isDeleted is null OR l.isDeleted=false) AND l.isActive=true")
	List<Location> findAllByHierarchyNameIgnoreCase(String hierarchyName);

	/**
	 *
	 * @param langCode
	 *            language code
	 * @param level
	 *            hierarchy level
	 * @return List of Locations
	 * 
	 */
	@Query(value = "FROM Location l where l.langCode=?1 and l.hierarchyLevel >=?2 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> getAllLocationsByLangCodeAndLevel(String langCode, Integer level);

	/**
	 * checks whether the location name is valid location or not
	 * @param locationName
	 * @return boolean
	 */
	@Query(value="SELECT EXISTS(select name FROM master.location where (LOWER(name)=LOWER(?1)) and (is_active=true) and (is_deleted is null or is_deleted =false))",nativeQuery=true)
    boolean isLocationNamePresent(String locationName);
}
