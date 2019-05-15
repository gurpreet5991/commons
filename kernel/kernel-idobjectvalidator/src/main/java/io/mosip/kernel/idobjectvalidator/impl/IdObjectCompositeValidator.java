package io.mosip.kernel.idobjectvalidator.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idobjectvalidator.exception.FileIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectSchemaIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationProcessingException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;

/**
 * The Class IdObjectCompositeValidator.
 *
 * @author Manoj SP
 */
@Component
@Primary
public class IdObjectCompositeValidator implements IdObjectValidator {
	
	/** The schema validator. */
	@Autowired
	private IdObjectSchemaValidator schemaValidator;
	
	/** The pattern validator. */
	@Autowired
	private IdObjectPatternValidator patternValidator;
	
	/** The master data validator. */
	@Autowired
	@Lazy
	private IdObjectMasterDataValidator masterDataValidator;

	/* (non-Javadoc)
	 * @see io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator#validateIdObject(java.lang.Object)
	 */
	@Override
	public boolean validateIdObject(Object identityObject) throws IdObjectValidationProcessingException,
			IdObjectIOException, IdObjectSchemaIOException, FileIOException {
		schemaValidator.validateIdObject(identityObject);
		patternValidator.validateIdObject(identityObject);
		masterDataValidator.validateIdObject(identityObject);
		return true;
	}


}
