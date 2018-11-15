package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.IdTypeResponseDto;
import io.mosip.kernel.masterdata.service.IdTypeService;

/**
 * This controller class provides id types details based on user provided data.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@RestController
public class IdTypeController {
	/**
	 * Autowired reference to IdService.
	 */
	@Autowired
	IdTypeService idService;

	/**
	 * This method returns the list of id types present for a specific language
	 * code.
	 * 
	 * @param languageCode
	 *            the language code against which id types are to be fetched.
	 * @return the list of id types.
	 */
	@GetMapping("/idtypes/{languagecode}")
	public IdTypeResponseDto getIdTypeDetailsByLanguageCode(@PathVariable("languagecode") String languageCode) {
		return idService.getIdTypeByLanguageCode(languageCode);
	}
}
