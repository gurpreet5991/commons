package io.mosip.authentication.partnerdemo.service.controller;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

//import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ArunBose S
 * @author DineshKaruppiah
 * The Class Encode is used to encode the String.
 */
@RestController
public class Encode {

	/**
	 * Encode.
	 *
	 * @param stringToEncode the string to encode
	 * @return the string
	 */
	@PostMapping(path = "/encode", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String encode(@RequestBody String stringToEncode) {
		return Base64.encodeBase64URLSafeString(stringToEncode.getBytes(StandardCharsets.UTF_8));
	}
}
