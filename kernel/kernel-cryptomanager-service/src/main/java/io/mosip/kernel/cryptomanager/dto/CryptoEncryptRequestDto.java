package io.mosip.kernel.cryptomanager.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.mosip.kernel.cryptomanager.constant.CryptomanagerConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoEncryptRequestDto {

	/**
	 * Application id of decrypting module
	 */
	@ApiModelProperty(notes = "Application id of decrypting module", example = "REGISTRATION", required = true)
	@NotBlank(message = CryptomanagerConstant.INVALID_REQUEST)
	private String applicationId;
	/**
	 * Refrence Id
	 */
	@ApiModelProperty(notes = "Refrence Id", example = "REF01")
	private String referenceId;
	/**
	 * Timestamp
	 */
	@ApiModelProperty(notes = "Timestamp as metadata", example = "2018-12-10T06:12:52.994Z", required = true)

	@NotNull
	private String timeStamp;
	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */
	@ApiModelProperty(notes = "Data in BASE64 encoding to encrypt/decrypt", required = true)
	@NotBlank(message = CryptomanagerConstant.INVALID_REQUEST)
	private String data;
}
