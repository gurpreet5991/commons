package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Title;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;

/**
 * Repository class for fetching titles from master db
 * 
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@Repository
public interface TitleRepository extends BaseRepository<Title, CodeAndLanguageCodeID> {
	/**
	 * method to get titles for a particular language code
	 * 
	 * @param languageCode
	 *            input from user
	 * @return list of all titles for a particular language code
	 */
	@Query
	List<Title> getThroughLanguageCode(@Param("lang_code") String languageCode);

}
