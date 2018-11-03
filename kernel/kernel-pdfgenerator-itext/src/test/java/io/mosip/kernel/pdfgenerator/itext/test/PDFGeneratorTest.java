package io.mosip.kernel.pdfgenerator.itext.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.pdfgenerator.exception.PdfGeneratorException;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.pdfgenerator.itext.impl.PDFGeneratorImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PDFGeneratorImpl.class })
@SuppressWarnings("resource")
public class PDFGeneratorTest {
	@Autowired
	private PDFGenerator pdfGenerator;

	@Test
	public void testPdfGenerationWithInputStream() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFile = classLoader.getResource("csshtml.html").getFile();
		InputStream is = new FileInputStream(inputFile);
		ByteArrayOutputStream bos = (ByteArrayOutputStream) pdfGenerator.generate(is);
		String outputPath = System.getProperty("user.dir");
		String fileSepetator = System.getProperty("file.separator");
		File OutPutPdfFile = new File(outputPath + fileSepetator + "csshtml.pdf");
		FileOutputStream op = new FileOutputStream(OutPutPdfFile);
		op.write(bos.toByteArray());
		op.flush();
		assertTrue(OutPutPdfFile.exists());
		if (op != null) {
			op.close();
		}

	}

	@Test(expected = PdfGeneratorException.class)
	public void testPdfGeneratorExceptionInInputStream() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFileName = classLoader.getResource("emptyFile.html").getFile();
		File inputFile = new File(inputFileName);
		InputStream inputStream = new FileInputStream(inputFile);
		pdfGenerator.generate(inputStream);
	}

	@Test
	public void testPdfGenerationWithTemplateAsStringAndOutStream() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFileName = classLoader.getResource("test.html").getFile();
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}

		ByteArrayOutputStream bos = (ByteArrayOutputStream) pdfGenerator.generate(sb.toString());
		String outputPath = System.getProperty("user.dir");
		String fileSepetator = System.getProperty("file.separator");
		File OutPutPdfFile = new File(outputPath + fileSepetator + "test.pdf");
		FileOutputStream op = new FileOutputStream(OutPutPdfFile);
		op.write(bos.toByteArray());
		op.flush();
		assertTrue(OutPutPdfFile.exists());
		if (op != null) {
			op.close();

		}
	}

	@Test(expected = PdfGeneratorException.class)
	public void testPDFGeneratorGenericExceptionWithTemplateAsString() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFileName = classLoader.getResource("emptyFile.html").getFile();
		BufferedReader br = new BufferedReader(new FileReader(inputFileName));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}
		pdfGenerator.generate(sb.toString());
	}

	@Test
	public void testPdfGenerationWithFile() throws IOException {
		String outputPath = System.getProperty("user.dir");
		String outputFileExtension = ".pdf";
		String fileSepetator = System.getProperty("file.separator");
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFile = classLoader.getResource("textcontant.txt").getFile();
		String generatedPdfFileName = "textcontant";
		pdfGenerator.generate(inputFile, outputPath, generatedPdfFileName);
		File tempoutFile = new File(outputPath + fileSepetator + generatedPdfFileName + outputFileExtension);
		assertTrue(tempoutFile.exists());
	}

	@Test(expected = PdfGeneratorException.class)
	public void testPdfGeneratorExceptionInFile() throws IOException {
		String outputPath = System.getProperty("user.dir");
		ClassLoader classLoader = getClass().getClassLoader();
		String inputFile = classLoader.getResource("").getFile();
		String generatedPdfFileName = "Wiki";
		pdfGenerator.generate(inputFile, outputPath, generatedPdfFileName);

	}

	@AfterClass
	public static void deleteOutputFile() {
		String outputFilePath = System.getProperty("user.dir");
		String outputFileExtension = ".pdf";
		String fileSepetator = System.getProperty("file.separator");
		File temp2 = new File(outputFilePath + fileSepetator + "test" + outputFileExtension);
		if (temp2.exists()) {
			temp2.delete();
		}
		File temp3 = new File(outputFilePath + fileSepetator + "textcontant" + outputFileExtension);
		if (temp3.exists()) {
			temp3.delete();
		}
		File temp4 = new File(outputFilePath + fileSepetator + "Wiki" + outputFileExtension);
		if (temp4.exists()) {
			temp4.delete();
		}
		File temp5 = new File(outputFilePath + fileSepetator + "csshtml" + outputFileExtension);
		if (temp5.exists()) {
			temp5.delete();
		}
		File temp1 = new File(outputFilePath + fileSepetator + "emptyFile" + outputFileExtension);
		if (temp1.exists()) {
			temp1.delete();
		}

	}

}
