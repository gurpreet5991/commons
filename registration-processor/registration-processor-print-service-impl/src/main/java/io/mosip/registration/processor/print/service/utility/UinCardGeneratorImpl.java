package io.mosip.registration.processor.print.service.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.UinCardType;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.spi.uincardgenerator.UinCardGenerator;

/**
 * The Class UinCardGeneratorImpl.
 * 
 * @author M1048358 Alok
 */
@Component
public class UinCardGeneratorImpl implements UinCardGenerator<ByteArrayOutputStream> {

	/** The pdf generator. */
	@Autowired
	private PDFGenerator pdfGenerator;

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(UinCardGeneratorImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.uincardgenerator.UinCardGenerator#
	 * generateUinCard(java.io.InputStream,
	 * io.mosip.registration.processor.core.constant.UinCardType)
	 */
	@Override
	public ByteArrayOutputStream generateUinCard(InputStream in, UinCardType type) {
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"UinCardGeneratorImpl::generateUinCard()::entry");

		ByteArrayOutputStream out = null;
		try {
			out = (ByteArrayOutputStream) pdfGenerator.generate(in);
		} catch (IOException | PDFGeneratorException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", PlatformErrorMessages.RPR_PRT_PDF_NOT_GENERATED.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.getStackTrace(e));
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"UinCardGeneratorImpl::generateUinCard()::exit");

		return out;
	}

}
