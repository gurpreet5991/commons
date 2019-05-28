package io.mosip.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.css.media.MediaDeviceDescription;
import com.itextpdf.html2pdf.css.media.MediaType;
import com.itextpdf.html2pdf.css.util.CssUtils;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import io.mosip.registrationProcessor.util.PrintingStage;

/**
 * The PdfGeneratorImpl is the class you will use most when converting processed
 * Template to PDF. It contains a series of methods that accept processed
 * Template as a {@link String}, {@link File}, or {@link InputStream}, and
 * convert it to PDF in the form of an {@link OutputStream}, {@link File}
 * 
 * @author M1046571
 * @author Neha
 * 
 * @since 1.0.0
 *
 */
@Component
public class PDFGeneratorImpl implements PDFGenerator {
	private static Logger logger = Logger.getLogger(PDFGeneratorImpl.class);
	private static final String OUTPUT_FILE_EXTENSION = ".pdf";
	//private static final String FILE_SEPERATOR = System.getProperty("file.separator");

	/**
	 * This method is used to convert Template obtained from an {@link InputStream}
	 * to a PDF file and written to an {@link OutputStream}.
	 *
	 * @param is
	 *            The {@link InputStream} with the source processed Template
	 * @return It will return generated PDF file as {@link OutputStream}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred
	 *
	 */
	@Override
	public OutputStream generate(InputStream is) throws IOException {
		OutputStream os = new ByteArrayOutputStream();
		Objects.requireNonNull(is, "Stream cannot be null");
		try {
			HtmlConverter.convertToPdf(is, os);
		} catch (Exception e) {
			logger.error("exception occurred.... ",e);
		}
		return os;
	}

	/**
	 * 
	 * Converts a {@link String} containing processed template to an
	 * {@link OutputStream} containing PDF
	 *
	 * @param template
	 *            the processedTemplate in the form of a {@link String}
	 * 
	 * @return It will return generated PDF file as {@link OutputStream}
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred
	 * 
	 * 
	 */

	@Override
	public OutputStream generate(String template) throws IOException {
		OutputStream os = new ByteArrayOutputStream();
		try {
			HtmlConverter.convertToPdf(template, os);
		} catch (Exception e) {
			logger.error("exception occurred ",e);
		}
		return os;
	}

	/**
	 * Converts processed Template stored in a {@link String} to a PDF {@link File}.
	 * 
	 * @param templatePath
	 *            The {@link String} containing the source Processed Template
	 * @param outpuFilePath
	 *            The {@link File} containing the resulting PDF
	 * @param outputFileName
	 *            The {@link String} name of output file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred
	 */
	@Override
	public void generate(String templatePath, String outpuFilePath, String outputFileName) throws IOException {
		File outputFile = new File(outpuFilePath + outputFileName + OUTPUT_FILE_EXTENSION);

		try {
			HtmlConverter.convertToPdf(new File(templatePath), outputFile);

		} catch (Exception e) {
			logger.error("exception occurred ",e);
		}

	}

	/**
	 * This method is used to convert Template obtained from an {@link InputStream}
	 * to a PDF file and written to an {@link OutputStream}.
	 *
	 * @param is
	 *            The {@link InputStream} with the source processed Template
	 * @param resourceLoc
	 *            The {@link String} resourceLocation
	 * @return It will return generated PDF file as {@link OutputStream}
	 * @throws IOException
	 *             Signals that an I/O exception has occurred
	 *
	 */
	@Override
	public OutputStream generate(InputStream is, String resourceLoc) throws IOException {
		Objects.requireNonNull(is, "Stream cannot be null");
		OutputStream os = new ByteArrayOutputStream();
		PdfWriter pdfWriter = new PdfWriter(os);
		PdfDocument pdfDoc = new PdfDocument(pdfWriter);
		ConverterProperties converterProperties = new ConverterProperties();
		pdfDoc.setTagged();
		PageSize pageSize = PageSize.A4.rotate();
		pdfDoc.setDefaultPageSize(pageSize);
		float screenWidth = CssUtils.parseAbsoluteLength("" + pageSize.getWidth());
		MediaDeviceDescription mediaDescription = new MediaDeviceDescription(MediaType.SCREEN);
		mediaDescription.setWidth(screenWidth);
		DefaultFontProvider dfp = new DefaultFontProvider(true, true, false);
		converterProperties.setMediaDeviceDescription(mediaDescription);
		converterProperties.setFontProvider(dfp);
		converterProperties.setBaseUri(resourceLoc);
		converterProperties.setCreateAcroForm(true);
		try {
			HtmlConverter.convertToPdf(is, pdfDoc, converterProperties);
		} catch (Exception e) {
			logger.error("exception occurred ",e);
		}
		return os;
	}

	
	/* (non-Javadoc)
	 * @see io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator#asPDF(java.util.List)
	 */
	@Override
	public byte[] asPDF(List<BufferedImage> bufferedImages) throws IOException {
		byte[] scannedPdfFile = null;

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

			PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);
			Document document = new Document(new PdfDocument(pdfWriter));

			for (BufferedImage bufferedImage : bufferedImages) {
				document.add(new Image(ImageDataFactory.create(getImageBytesFromBufferedImage(bufferedImage))));
			}

			document.close();
			pdfWriter.close();
			scannedPdfFile = byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			logger.error("IO exception occurred..", e);
		}
		return scannedPdfFile;
	}

	private byte[] getImageBytesFromBufferedImage(BufferedImage bufferedImage) throws IOException {
		byte[] imageInByte;

		ByteArrayOutputStream imagebyteArray = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", imagebyteArray);
		imagebyteArray.flush();
		imageInByte = imagebyteArray.toByteArray();
		imagebyteArray.close();

		return imageInByte;
	}

	/* (non-Javadoc)
	 * @see io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator#mergePDF(java.util.List)
	 */
	@Override
	public byte[] mergePDF(List<URL> pdfFiles) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy pdfCopy = new PdfCopy(document, byteArrayOutputStream);
			document.open();
			for (URL file : pdfFiles) {
				PdfReader reader = new PdfReader(file);
				pdfCopy.addDocument(reader);
				pdfCopy.freeReader(reader);
				reader.close();
			}
			document.close();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException | DocumentException e) {
			logger.error("IOException | DocumentException occurred ",e);
			return null;
		}
	}
}
