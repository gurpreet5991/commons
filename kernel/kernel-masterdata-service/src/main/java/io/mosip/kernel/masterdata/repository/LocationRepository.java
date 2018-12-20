package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Location;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
/**
 * This interface is JPA repository class which interacts with database and does the CRUD function. It is 
 * extended from {@link BaseRepository}
 * @author Srinivasan
 *
 */
@Repository
public interface LocationRepository extends BaseRepository<Location, CodeAndLanguageCodeID> {
	
	List<Location> findLocationHierarchyByIsDeletedIsNullOrIsDeletedFalse();
	@Query(value="FROM Location l where l.code=?1 and l.langCode=?2 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findLocationHierarchyByCodeAndLanguageCode(String locCode,String languagecode);
	@Query(value="FROM Location l where parentLocCode=?1 and langCode=?2 and (l.isDeleted is null or l.isDeleted=false)")
	List<Location> findLocationHierarchyByParentLocCodeAndLanguageCode(String parentLocCode,
			String languageCode);
	@Query(value="select distinct hierarchy_level, hierarchy_level_name, is_active from master.location where lang_code='ENG' and (is_deleted='f' or is_deleted=null)",nativeQuery=true)
	List<Object[]> findDistinctLocationHierarchyByIsDeletedFalse(String langCode);
	
	/**
	 * @author M1043226
	 *
	 */
	List<Location> findAllByHierarchyNameIgnoreCase(String hierarchyName);

}
