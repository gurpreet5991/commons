
package io.mosip.registration.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.registration.dao.MasterSyncDao;
import io.mosip.registration.dao.ValidDocumentDAO;
import io.mosip.registration.dto.mastersync.DocumentCategoryDto;
import io.mosip.registration.entity.ApplicantValidDocument;
import io.mosip.registration.entity.DocumentType;

@Service
public class ValidDocumentServiceImpl implements ValidDocumentService {

	@Autowired
	private ValidDocumentDAO validDocumentDAO;

	@Autowired
	private MasterSyncDao masterSyncDao;

	@Override
	public List<DocumentCategoryDto> getDocumentCategories(String applicantType, String docCode, String langCode) {

		List<ApplicantValidDocument> masterValidDocuments = validDocumentDAO.getValidDocuments(applicantType, docCode);

		List<String> validDocuments = new ArrayList<>();
		masterValidDocuments.forEach(docs -> {
			validDocuments.add(docs.getValidDocumentId().getDocTypeCode());
		});

		List<DocumentCategoryDto> documentsDTO = new ArrayList<>();
		List<DocumentType> masterDocuments = masterSyncDao.getDocumentTypes(validDocuments, langCode);

		masterDocuments.forEach(document -> {

			DocumentCategoryDto documents = new DocumentCategoryDto();
			documents.setDescription(document.getDescription());
			documents.setLangCode(document.getLangCode());
			documents.setName(document.getName());
			documentsDTO.add(documents);

		});

		return documentsDTO;
	}
}