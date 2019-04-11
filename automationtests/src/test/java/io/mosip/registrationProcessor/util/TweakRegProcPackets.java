package io.mosip.registrationProcessor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.mosip.dbdto.DecrypterDto;
import io.mosip.util.EncrypterDecrypter;
import net.lingala.zip4j.exception.ZipException;

/**
 * 
 * @author M1047227
 *
 */
public class TweakRegProcPackets {
	private static Logger logger = Logger.getLogger(TweakRegProcPackets.class);
	StringBuilder osiValidatorStageString=new StringBuilder();
	
	StringBuilder packetValidatorStageString=new StringBuilder();
	StringBuilder demoDedupeStageString=new StringBuilder();
	static String filesToBeDestroyed = null;
	String centerId = "";
	String machineId = "";
	String packetName = "";
	EncrypterDecrypter encryptDecrypt = new EncrypterDecrypter();
	DecrypterDto decrypterDto = new DecrypterDto();
	PacketValidator validate = new PacketValidator();

	/**
	 * 
	 * @param testCaseName
	 * @param parameterToBeChanged
	 * @param parameterValue       generates invalid packet which should fail in the
	 *                             osi stage packets are created by changing the
	 *                             values of metaInfo fields inside the packet
	 */
	@SuppressWarnings("unchecked")
	public void generateInvalidPacketForOsiValidator(String testCaseName, String parameterToBeChanged,
			String parameterValue, String validPacketPath, String invalidPacketPath) {
		
		File decryptedFile = null;
		JSONObject metaInfo = null;
		String configPath = System.getProperty("user.dir") + "/" + validPacketPath;
		String invalidPacketsPath = System.getProperty("user.dir") + "/" + invalidPacketPath +"OsiValidation/"+ testCaseName;
		File file = new File(configPath);
		File[] listOfFiles = file.listFiles();
		for (File f : listOfFiles) {
			if (f.getName().contains(".zip")) {
				JSONObject jsonObject = encryptDecrypt.generateCryptographicData(f);
					//decryptedFile = encryptDecrypt.decryptFile(jsonObject, configPath, f.getName());
					decryptedFile= encryptDecrypt.extractFromDecryptedPacket(configPath,f.getName());
				
				filesToBeDestroyed = configPath;
				File[] packetFiles = decryptedFile.listFiles();
				for (File info : packetFiles) {

					if (info.getName().toLowerCase().equals("packet_meta_info.json")) {
						try {
							FileReader metaFileReader = new FileReader(info.getPath());
							metaInfo = (JSONObject) new JSONParser().parse(metaFileReader);
							metaFileReader.close();
						} catch (IOException | ParseException e) {
							logger.error("Packet_meta_info.json file not found inside the packet", e);
						}
						JSONObject identity = (JSONObject) metaInfo.get("identity");
						JSONArray metaData = (JSONArray) identity.get("metaData");
						JSONArray updatedData = updateMetaInfo(metaData, parameterToBeChanged, parameterValue);
						metaInfo.put("identity", identity);
						try (FileWriter updatedFile = new FileWriter(info.getAbsolutePath())) {
							try {
								updatedFile.write(metaInfo.toString());
								updatedFile.close();
							} catch (IOException e) {
								logger.error("Could not update the packet_meta_info.json as file was not found", e);
							}
							logger.info("Successfully updated json object to file...!!");

						} catch (IOException e1) {
							logger.error("Could not update the packet_meta_info.json as file was not found", e1);
						}
					}

				}
			}
		}
		logger.info("packetName :: ======================" + packetName);
		try {
			
			encryptDecrypt.encryptFile(decryptedFile, configPath, invalidPacketsPath, packetName);
		
		} catch (ZipException | IOException e) {
			logger.error("Packet encryption Failed", e);
		}

	}

	/**
	 * 
	 * @param fileName
	 * @param validPacketPath
	 * @param invalidPacketPath generates invalid packet which should fail in packet
	 *                          validator stage packets are created by changing the
	 *                          packet structure itself
	 */
	public void generateInvalidPacketForPacketValidator(String fileName, String validPacketPath,
			String invalidPacketPath) {
		File decryptedPacket = null;
		JSONObject metaInfo = null;
		String configPath = System.getProperty("user.dir") + "/" + validPacketPath;
		filesToBeDestroyed = configPath;
		File file = new File(configPath);
		File[] listOfFiles = file.listFiles();
		String invalidPacketsPath = System.getProperty("user.dir") + "/" + invalidPacketPath+"PacketValidator/";
		for (File f : listOfFiles) {
			if (f.getName().contains(".zip")) {
				centerId = f.getName().substring(0, 5);
				machineId = f.getName().substring(5, 10);
				String regId = generateRegId(centerId, machineId);
				JSONObject requestBody = encryptDecrypt.generateCryptographicData(f);
				try {
					//decryptedPacket = encryptDecrypt.decryptFile(requestBody, configPath, f.getName());
					decryptedPacket= encryptDecrypt.extractFromDecryptedPacket(configPath,f.getName());
					for (File info : decryptedPacket.listFiles()) {
						if (info.getName().toLowerCase().equals("packet_meta_info.json")) {
							try {
								FileReader metaFileReader = new FileReader(info.getPath());
								metaInfo = (JSONObject) new JSONParser().parse(metaFileReader);
								metaFileReader.close();
							} catch (IOException | ParseException e) {
								logger.error("Could not find the packet_meta_info.json", e);
							}
							JSONObject identity = (JSONObject) metaInfo.get("identity");
							JSONArray metaData = (JSONArray) identity.get("metaData");
							JSONArray updatedData = updateRegId(metaData, regId);
							metaInfo.put("identity", identity);
							try (FileWriter updatedFile = new FileWriter(info.getAbsolutePath())) {
								try {
									updatedFile.write(metaInfo.toString());
									updatedFile.close();
								} catch (IOException e) {
									logger.error("Could not update the packet_meta_info.json as file was not found", e);
								}
								logger.info("Successfully updated json object to file...!!");

							} catch (IOException e1) {
								logger.error("Could not find the packet_meta_info.json", e1);
							}
						}
					}
					String fileDeleted = "";
					String str = "";
					for (File info : decryptedPacket.listFiles()) {
						if (info.getName().equalsIgnoreCase("Demographic")) {
							for (File demographicFiles : info.listFiles()) {
								if (demographicFiles.getName().equals(fileName)) {
									fileDeleted = demographicFiles.getName() + "DeletedPacket";
									FileDeleteStrategy.FORCE.delete(demographicFiles);
									byte[] checkSum = encryptDecrypt.generateCheckSum(decryptedPacket.listFiles());
									str = new String(checkSum, StandardCharsets.UTF_8);

								}
							}
						} else if (info.getName().equalsIgnoreCase(fileName)) {
							if (info.isDirectory()) {
								fileDeleted = info.getName() + "Deletedpacket";
								FileUtils.deleteDirectory(info);
								byte[] checkSum = encryptDecrypt.generateCheckSum(decryptedPacket.listFiles());
								str = new String(checkSum, StandardCharsets.UTF_8);

							} else if (info.isFile()) {
								fileDeleted = info.getName() + "Deletedpacket";
								FileDeleteStrategy.FORCE.delete(info);
								byte[] checkSum = encryptDecrypt.generateCheckSum(decryptedPacket.listFiles());
								str = new String(checkSum, StandardCharsets.UTF_8);

							}
						}
					}
					for (File info : decryptedPacket.listFiles()) {
						if (info.getName().equals("packet_data_hash.txt")) {
							PrintWriter writer = new PrintWriter(info);
							writer.print("");
							writer.print(str);
							writer.close();
							encryptDecrypt.encryptFile(decryptedPacket, configPath,
									invalidPacketsPath + "/" + fileDeleted, regId);
						}
					}

				} catch (IOException | ZipException | ParseException e) {
					logger.error("Error while generating the packets", e);
				}
			}
		}

	}

	/**
	 * 
	 * @param testCaseName
	 * @param property
	 * @param validPacketPath
	 * @param invalidPacketpath generates invalid packets which should fail in demo
	 *                          dedupe stage creates two packets with same
	 *                          demographic details
	 */

	public void generatInvalidPacketForDemoDedupe(String testCaseName, String property, String validPacketPath,
			String invalidPacketpath) {
		JSONObject metaInfo = null;
		JSONObject metaInfoBio = null;
		File decryptedPacket = null;
		JSONObject identity = null;
		String configPath = System.getProperty("user.dir") + "/" + validPacketPath;
		String invalidPacketsPath = System.getProperty("user.dir") + "/" + invalidPacketpath +"DemoDedupe/"+ testCaseName;
		filesToBeDestroyed = configPath;
		File file = new File(configPath);
		File[] listOfFiles = file.listFiles();
		for (File f : listOfFiles) {

			if (f.getName().contains(".zip")) {
				centerId = f.getName().substring(0, 5);
				machineId = f.getName().substring(5, 10);
				String regId = generateRegId(centerId, machineId);
				JSONObject requestBody = encryptDecrypt.generateCryptographicData(f);
				try {

					//decryptedPacket = encryptDecrypt.decryptFile(requestBody, configPath, f.getName());
					decryptedPacket= encryptDecrypt.extractFromDecryptedPacket(configPath,f.getName());
					for (File info : decryptedPacket.listFiles()) {
						if (info.getName().equals("Demographic")) {
							File[] demographic = info.listFiles();
							for (File metaFile : demographic) {
								if (metaFile.getName().equals("ID.json")) {
									FileReader metaFileReader = new FileReader(metaFile.getPath());
									metaInfo = (JSONObject) new JSONParser().parse(metaFileReader);
									metaFileReader.close();
									identity = (JSONObject) metaInfo.get("identity");
									identity.put("dateOfBirth", property);
									logger.info("Identity is :: " + identity.get("dateOfBirth"));
									try (FileWriter updatedFile = new FileWriter(metaFile.getAbsolutePath())) {
										try {
											updatedFile.write(metaInfo.toString());
											updatedFile.close();
										} catch (IOException e) {
											logger.error("Could not find ID.json", e);
										}
										logger.info("Successfully updated json object to file...!!");

									} catch (IOException e1) {
										logger.error("Could not update ID.json", e1);
									}

								}

							}
						} else if (info.getName().toLowerCase().equals("packet_meta_info.json")) {
							try {
								FileReader metaFileReader = new FileReader(info.getPath());
								metaInfoBio = (JSONObject) new JSONParser().parse(metaFileReader);
								metaFileReader.close();
							} catch (IOException | ParseException e) {
								logger.error("packet_meta_info.json was not found", e);
							}
							JSONObject identityObject = (JSONObject) metaInfoBio.get("identity");
							JSONArray metaData = (JSONArray) identityObject.get("metaData");
							JSONArray updatedData = updateRegId(metaData, regId);
							logger.info("updatedData : " + updatedData);
							metaInfoBio.put("identity", identityObject);
							logger.info("metaInfoBio : " + metaInfoBio);
							try (FileWriter updatedFile = new FileWriter(info.getAbsolutePath())) {
								try {
									updatedFile.write(metaInfoBio.toString());
									updatedFile.close();
								} catch (IOException e) {
									logger.error("Could not update the packet_meta_info.json as file was not found", e);
								}
								logger.info("Successfully updated json object to file...!!");

							} catch (IOException e1) {

								logger.error("Could not update the packet_meta_info.json as file was not found", e1);
							}
						}

					}
					for (File info : decryptedPacket.listFiles()) {
						byte[] checksum = encryptDecrypt.generateCheckSum(decryptedPacket.listFiles());

						String str = new String(checksum, StandardCharsets.UTF_8);
						logger.info("checksum is  :: " + str);
						if (info.getName().equals("packet_data_hash.txt")) {
							PrintWriter writer = new PrintWriter(info);
							writer.print("");
							writer.print(str);
							writer.close();
							encryptDecrypt.encryptFile(decryptedPacket, configPath, invalidPacketsPath, regId);
						}

					}

				} catch (IOException | ZipException | ParseException e) {
					logger.error("Could not create invalid demo dedupe packet", e);
				}
			}
		}

	}

	/**
	 * 
	 * @param metaData
	 * @param parameter
	 * @param value
	 * @return updated metaInfo updates the metaInfo paramaters of a packet to
	 *         create an invalid packet
	 */
	public JSONArray updateMetaInfo(JSONArray metaData, String parameter, String value) {
		for (int i = 0; i < metaData.size(); i++) {
			JSONObject labels = (JSONObject) metaData.get(i);
			if (labels.get("label").equals("centerId")) {
				centerId = labels.get("value").toString();
				logger.info("centreId :: " + centerId);
			} else if (labels.get("label").equals("machineId")) {
				machineId = labels.get("value").toString();
				logger.info("machineId :: " + machineId);
			}

			if (labels.get("label").equals(parameter)) {
				labels.put("value", value);
			}
		}
		for (int i = 0; i < metaData.size(); i++) {
			JSONObject labels = (JSONObject) metaData.get(i);
			if (labels.get("label").equals("registrationId")) {
				packetName = generateRegId(centerId, machineId);
				labels.put("value", packetName);
			}
		}
		logger.info("Meta Data Updated Is  ::  ===================" + metaData);
		return metaData;
	}

	/**
	 * 
	 * @param propertyFiles
	 * @param validPacketpath
	 * @param invalidPacketPath reads property files containing set of properties to
	 *                          be updated osi validation stage
	 */

	public void osiValidatorPropertyFileReader(String propertyFiles, String validPacketpath, String invalidPacketPath) {
		EncrypterDecrypter encryptDecrypt = new EncrypterDecrypter();
		TweakRegProcPackets e = new TweakRegProcPackets();
		Properties prop = new Properties();
		String propertyFilePath = System.getProperty("user.dir") + "/src/config/" + propertyFiles;
		try {
			prop.load(new FileReader(new File(propertyFilePath)));
		} catch (IOException e1) {
			logger.info("Could not find property file with name :: " + propertyFiles + "at location :: "
					+ propertyFilePath);
		}
		for (String property : prop.stringPropertyNames()) {
			logger.info("invalid" + property);
			logger.info(prop.getProperty(property));
			e.generateInvalidPacketForOsiValidator("invalid" + property, property, prop.getProperty(property),
					validPacketpath, invalidPacketPath);
		}
		
		try {
			osiValidatorStageString.append("1110000");
			OutputStream stageBitFile =null;
			stageBitFile=new FileOutputStream(invalidPacketPath+"/OsiValidation/StageBits.properties");
			Properties property=new Properties();
			property.setProperty("StageBits", osiValidatorStageString.toString());
			property.store(stageBitFile, null);
			stageBitFile.close();
			encryptDecrypt.destroyFiles(filesToBeDestroyed);
		} catch (IOException e1) {
			logger.info("Could not find any decrypted packet at :: " + filesToBeDestroyed);
		}
	}

	/**
	 * 
	 * @param propertyFiles
	 * @param validPacketPath
	 * @param invalidPacketPath reads property files containing set of properties to
	 *                          be updated demo dedupe stage
	 */

	public void demoDedupePropertyFileReader(String propertyFiles, String validPacketPath, String invalidPacketPath) {
		Properties prop = new Properties();
		TweakRegProcPackets e = new TweakRegProcPackets();
		String propertyFilePath = System.getProperty("user.dir") + "/src/config/" + propertyFiles;
		try {
			FileReader readFile = new FileReader(new File(propertyFilePath));
			prop.load(readFile);
			for (String property : prop.stringPropertyNames()) {
				logger.info("invalid" + property);
				logger.info(prop.getProperty(property));

				e.generatInvalidPacketForDemoDedupe(property, prop.getProperty(property), validPacketPath,
						invalidPacketPath);
			}
			readFile.close();
		} catch (IOException exc) {
			logger.error("Could not find the file :: " + propertyFiles);
		}
		try {
			demoDedupeStageString.append("1111000");
			OutputStream stageBitFile =null;
			stageBitFile=new FileOutputStream(invalidPacketPath+"/DemoDedupe/StageBits.properties");
			Properties property=new Properties();
			property.setProperty("StageBits", demoDedupeStageString.toString());
			property.store(stageBitFile, null);
			stageBitFile.close();
			encryptDecrypt.destroyFiles(filesToBeDestroyed);
		} catch (IOException e1) {
			logger.info("Could not find any decrypted packet at :: " + filesToBeDestroyed);
		}
	}

	/**
	 * 
	 * @param propertyFile
	 * @param validPacketPath
	 * @param invalidPacketPath reads property files containing set of properties to
	 *                          be updated packet validator stage
	 */

	public void packetValidatorPropertyFileReader(String propertyFile, String validPacketPath,
			String invalidPacketPath) {
		Properties prop = new Properties();
		TweakRegProcPackets e = new TweakRegProcPackets();
		String propertyFilePath = System.getProperty("user.dir") + "/src/config/" + propertyFile;
		try {
			FileReader readFile = new FileReader(new File(propertyFilePath));
			prop.load(readFile);

			for (String property : prop.stringPropertyNames()) {
				logger.info(prop.getProperty(property));

				e.generateInvalidPacketForPacketValidator(prop.getProperty(property), validPacketPath,
						invalidPacketPath);
			}
			readFile.close();
		} catch (IOException exc) {
			logger.error("Could not find the file :: " + propertyFile);
		}
		try {
			packetValidatorStageString.append("1100000");
			OutputStream stageBitFile =null;
			stageBitFile=new FileOutputStream(invalidPacketPath+"/PacketValidator/StageBits.properties");
			Properties property=new Properties();
			property.setProperty("StageBits", packetValidatorStageString.toString());
			property.store(stageBitFile, null);
			stageBitFile.close();
			encryptDecrypt.destroyFiles(filesToBeDestroyed);
		} catch (IOException e1) {
			logger.info("Could not find any decrypted packet at :: " + filesToBeDestroyed);
		}
	}

	/**
	 * 
	 * @param centerId
	 * @param machineId
	 * @return registration id generates registration id
	 */
	public String generateRegId(String centerId, String machineId) {
		String regID = "";
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		timeStamp.replaceAll(".", "");
		int n = 10000 + new Random().nextInt(90000);
		String randomNumber = String.valueOf(n);
		regID = centerId + machineId + randomNumber + timeStamp;
		return regID;
	}

	/**
	 * 
	 * @param metaData
	 * @param regID
	 * @return updated metaInfo with new registration id updates the registration id
	 *         inside the packet so as to mach with the packet name
	 */
	public JSONArray updateRegId(JSONArray metaData, String regID) {

		for (int i = 0; i < metaData.size(); i++) {
			JSONObject labels = (JSONObject) metaData.get(i);
			if (labels.get("label").equals("registrationId")) {
				labels.put("value", regID);
			}
		}

		return metaData;
	}

	public static void main(String[] args)
			throws IOException, ZipException, InterruptedException, java.text.ParseException, ParseException {
		TweakRegProcPackets e = new TweakRegProcPackets();

		Properties property = new Properties();
		String propertyFilePath = System.getProperty("user.dir") + "/src/config/folderPaths.properties";
		FileReader reader = new FileReader(new File(propertyFilePath));
		property.load(reader);
		String validPacketPath = property.getProperty("pathForValidRegProcPackets");
		String invalidPacketFolderPath = property.getProperty("invalidPacketFolderPath");
		e.packetValidatorPropertyFileReader("packetValidator.properties", validPacketPath, invalidPacketFolderPath);
		for (int i = 0; i < 2; i++) {
			e.demoDedupePropertyFileReader("IDjson.properties", validPacketPath, invalidPacketFolderPath);
		}
		e.osiValidatorPropertyFileReader("packetProperties.properties", validPacketPath, invalidPacketFolderPath);
		reader.close();

	}
}