package io.mosip.registration.cipher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.kernel.crypto.jce.constant.SecurityMethod;
import io.mosip.kernel.crypto.jce.processor.SymmetricProcessor;

/**
 * Encrypt the Client Jar with Symmetric Key
 * 
 * @author Omsai Eswar M.
 *
 */
public class ClientJarEncryption {
	private static final String SLASH = "/";
	private static final String AES_ALGORITHM = "AES";
	private static final String REGISTRATION = "registration";
	private static final String MOSIP_APPLICATION_PROPERTIES_PATH = "props/mosip-application.properties";
	private static final String MOSIP_EXE_JAR = "run.jar";
	private static final String MOSIP_LIB = "lib";
	private static final String MOSIP_DB = "db";
	private static final String MOSIP_ZIP = ".zip";
	private static final String MOSIP_JAR = ".jar";
	private static final String MOSIP_LOG_PARAM = "mosip.logpath= ";
	private static final String MOSIP_DB_PARAM = "mosip.dbpath= ";
	private static final String MOSIP_PACKET_STORE_PARAM = "mosip.packetstorepath= ";
	private static final String MOSIP_PACKET_STORE_PATH = "../PacketStore";
	private static final String MOSIP_LOG_PATH = "../logs";
	private static final String MOSIP_DB_PATH = "/db/reg";
	private static final String MOSIP_REG_LIBS = "registration-libs-";
	private static final String MANIFEST_FILE_NAME = "MANIFEST";
	private static final String MANIFEST_FILE_FORMAT = ".MF";
	private static final String MOSIP_BIN = "bin";
	private static final String MOSIP_LOG = "log";
	private static final String MOSIP_SERVICES = "mosip-services.jar";
	private static final String MOSIP_CLIENT = "mosip-client.jar";
	private static final String MOSIP_CER = "cer";
	

	/**
	 * Encrypt the bytes
	 * 
	 * @param Jar
	 *            bytes
	 * @throws UnsupportedEncodingException
	 */
	public byte[] encyrpt(byte[] data, byte[] encodedString) {
		// Generate AES Session Key
		SecretKey symmetricKey = new SecretKeySpec(encodedString, AES_ALGORITHM);

		return SymmetricProcessor.process(SecurityMethod.AES_WITH_CBC_AND_PKCS5PADDING, symmetricKey, data,
				Cipher.ENCRYPT_MODE);
	}

	/**
	 * Encrypt and save the file in client module
	 * 
	 * args[0]/args[1] --> To provide the ciennt jar args[2] --> Secret key String
	 * args[3] --> project version
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		ClientJarEncryption aes = new ClientJarEncryption();
		if (args != null && args.length > 2) {
			File file = args[1] != null && new File(args[1]).exists() ? new File(args[1])
					: (args[0] != null && new File(args[0]).exists() ? new File(args[0]) : null);

			File clientJar = new File(args[0]);

			try (FileOutputStream fileOutputStream = new FileOutputStream(
					new File(file.getParent() + SLASH + MANIFEST_FILE_NAME + MANIFEST_FILE_FORMAT))) {

				Manifest manifest = new Manifest();

				/* Add Version to Manifest */
				manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, args[3]);

				System.out.println("Zip Creation started");

				if (file != null && file.exists()) {
					String propertiesFile = MOSIP_APPLICATION_PROPERTIES_PATH;
					String libraries = MOSIP_LIB + SLASH;

					String zipFilename = file.getParent() + SLASH + "mosip-sw-" + args[3] + MOSIP_ZIP;

					byte[] propertiesBytes = (MOSIP_LOG_PARAM + MOSIP_LOG_PATH + "\n" + MOSIP_DB_PARAM + MOSIP_DB_PATH
							+ "\n" + MOSIP_PACKET_STORE_PARAM + MOSIP_PACKET_STORE_PATH).getBytes();
					byte[] runExecutbale = FileUtils
							.readFileToByteArray(new File(args[4] + MOSIP_REG_LIBS + args[3] + MOSIP_JAR));
					File listOfJars = new File(file.getParent() + SLASH + MOSIP_LIB).getAbsoluteFile();

					// Add files to be archived into zip file
					Map<String, byte[]> fileNameByBytes = new HashMap<>();

					// fileNameByBytes.put(encryptedFileToSave, encryptedFileBytes);
					fileNameByBytes.put(propertiesFile, propertiesBytes);
					fileNameByBytes.put(MOSIP_DB + SLASH, new byte[] {});
					fileNameByBytes.put(MOSIP_LIB + SLASH, new byte[] {});
					fileNameByBytes.put(MOSIP_BIN + SLASH, new byte[] {});
					fileNameByBytes.put(MOSIP_LOG + SLASH, new byte[] {});
				
					fileNameByBytes.put(MOSIP_EXE_JAR, runExecutbale);
					
					//Certificate file
					File mosipCertificateFile = new File(args[5]);
					
					if(mosipCertificateFile.exists()) {
						fileNameByBytes.put(MOSIP_CER + SLASH+ mosipCertificateFile.getName(), FileUtils.readFileToByteArray(mosipCertificateFile));
					}

					String path = new File(args[4]).getPath();

					File regLibFile = new File(path + SLASH + libraries);
					regLibFile.mkdir();

					byte[] clientJarEncryptedBytes = aes.getEncryptedBytes(Files.readAllBytes(clientJar.toPath()),
							Base64.getDecoder().decode(args[2].getBytes()));

					String filePath = listOfJars.getAbsolutePath() + SLASH + MOSIP_CLIENT;

					try (FileOutputStream regFileOutputStream = new FileOutputStream(new File(filePath))) {
						regFileOutputStream.write(clientJarEncryptedBytes);

					}
					/* Add To Manifest */
					addToManifest(MOSIP_CLIENT, clientJarEncryptedBytes, manifest);

					boolean isAssistSaved = false;

					// /* Save Client jar to registration-libs */
					// saveLibJars(clientJarEncryptedBytes, clientJar.getName(), regLibFile);

					// Adding lib files into map
					for (File files : listOfJars.listFiles()) {

						if (files.getName().contains(REGISTRATION)) {

							String regpath = files.getParentFile().getAbsolutePath() + SLASH;
							if (files.getName().contains("client")) {
								regpath += MOSIP_CLIENT;
							} else {
								regpath += MOSIP_SERVICES;
							}
							byte[] encryptedRegFileBytes = aes.encyrpt(FileUtils.readFileToByteArray(files),
									Base64.getDecoder().decode(args[2].getBytes()));
							// fileNameByBytes.put(libraries + files.getName(), encryptedRegFileBytes);

							File servicesJar = new File(regpath);
							try (FileOutputStream regFileOutputStream = new FileOutputStream(servicesJar)) {
								regFileOutputStream.write(encryptedRegFileBytes);
								files.deleteOnExit();

							}

							/* Add To Manifest */
							addToManifest(servicesJar.getName(), encryptedRegFileBytes, manifest);

							// saveLibJars(encryptedRegFileBytes, files.getName(), regLibFile);
						} else if (files.getName().contains("pom")
								|| (files.getName().contains("javassist") && isAssistSaved)) {
							FileUtils.forceDelete(files);

						} else {
							// fileNameByBytes.put(libraries + files.getName(),
							// FileUtils.readFileToByteArray(files));

							if (files.getName().contains("javassist")) {
								isAssistSaved = true;
							}
							/* Add To Manifest */
							addToManifest(files.getName(), Files.readAllBytes(files.toPath()), manifest);

							// saveLibJars(files, regLibFile);
						}
					}

					writeManifest(fileOutputStream, manifest);

					fileNameByBytes.put(MANIFEST_FILE_NAME + MANIFEST_FILE_FORMAT, FileUtils.readFileToByteArray(
							new File(file.getParent() + SLASH + MANIFEST_FILE_NAME + MANIFEST_FILE_FORMAT)));

					aes.writeFileToZip(fileNameByBytes, zipFilename);

					System.out.println("Zip Creation ended with path :::" + zipFilename);
				}
			}
		}
	}

	/**
	 * Write file to zip.
	 * 
	 * @param files
	 * @param zipFilename
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void writeFileToZip(Map<String, byte[]> fileNameByBytes, String zipFilename) throws IOException {
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(zipFilename)))) {

			fileNameByBytes.forEach((key, value) -> {
				ZipEntry zipEntry = new ZipEntry(key);
				try {
					zipOutputStream.putNextEntry(zipEntry);
					zipOutputStream.write(value);
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			});
		}
	}

	private static void addToManifest(String fileName, byte[] bytes, Manifest manifest) {

		String hashText = HMACUtils.digestAsPlainText(HMACUtils.generateHash(bytes));

		Attributes attribute = new Attributes();
		attribute.put(Attributes.Name.CONTENT_TYPE, hashText);

		manifest.getEntries().put(fileName, attribute);

	}

	private static void writeManifest(FileOutputStream fileOutputStream, Manifest manifest) {

		try {

			manifest.write(fileOutputStream);

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	/*
	 * private static void saveLibJars(File srcFile, File destFile) {
	 * 
	 * try {
	 * 
	 * String val =
	 * srcFile.getName().split("\\.")[srcFile.getName().split("\\.").length - 1]; if
	 * ("jar".equals(val)) { FileUtils.copyFileToDirectory(srcFile, destFile); } }
	 * catch (NullPointerException | IOException exception) {
	 * exception.printStackTrace(); }
	 * 
	 * }
	 */

	/*
	 * private static void saveLibJars(byte[] jarBytes, String srcFileName, File
	 * destDir) {
	 * 
	 * File file = new File(destDir.getAbsolutePath() + SLASH + srcFileName);
	 * 
	 * try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
	 * fileOutputStream.write(jarBytes); } catch (NullPointerException | IOException
	 * exception) { exception.printStackTrace(); }
	 * 
	 * }
	 */

	private byte[] getEncryptedBytes(byte[] jarBytes, byte[] decodeBytes) {
		return encyrpt(jarBytes, decodeBytes);
	}
}