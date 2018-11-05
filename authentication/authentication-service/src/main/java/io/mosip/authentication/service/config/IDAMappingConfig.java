package io.mosip.authentication.service.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.mosip.authentication.service.factory.IDAMappingFactory;
import lombok.Data;

@Configuration
@PropertySource(value = "classpath:ida-mapping.json", factory = IDAMappingFactory.class)
@ConfigurationProperties
@Data
public class IDAMappingConfig {

	private List<String> name;
	private List<String> dob;
	private List<String> dobType;
	private List<String> age;
	private List<String> gender;
	private List<String> phoneNumber;
	private List<String> emailId;
	private List<String> addressLine1;
	private List<String> addressLine2;
	private List<String> addressLine3;
	private List<String> location1;
	private List<String> location2;
	private List<String> location3;
	private List<String> pinCode;
	private List<String> fullAddress;
	private List<String> otp;
	private List<String> pin;
	private List<String> iris;
	private List<String> fingerprint;

}
