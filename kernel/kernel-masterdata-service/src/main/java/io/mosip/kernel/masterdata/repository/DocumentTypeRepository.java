package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.DocumentType;

/**
 * 
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Repository
public interface DocumentTypeRepository extends BaseRepository<DocumentType, String> {
	@Query(value = "select dt.code, dt.name, dt.descr , dt.lang_code , dt.is_active ,dt.cr_by ,dt.cr_dtimes ,dt.upd_by ,dt.upd_dtimes ,dt.is_deleted ,dt.del_dtimes from master.valid_document vd , master.doc_type dt , master.doc_category dc where vd.doctyp_code = dt.code and vd.doccat_code = dc.code and dc.code = ?1 and dc.lang_code = ?2", nativeQuery = true)
	List<DocumentType> findByCodeAndLangCode(String code, String langCode);

}
