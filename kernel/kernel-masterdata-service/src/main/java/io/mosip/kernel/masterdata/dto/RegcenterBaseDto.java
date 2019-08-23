package io.mosip.kernel.masterdata.dto;

import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * This request DTO to hold the numeric fields common for create and update
 * Registration center by Admin, hold the numeric common fields.
 * 
 * @author Megha Tanga
 * 
 * 
 *
 */
@Data
public class RegcenterBaseDto {

	@NotBlank
	@Size(min = 1, max = 36)
	private String centerTypeCode;

	@NotBlank
	@Size(min = 1, max = 32)
	private String latitude;

	@NotBlank
	@Size(min = 1, max = 32)
	private String longitude;

	@NotBlank
	@Size(min = 1, max = 36)
	private String locationCode;

	@NotBlank
	@Size(min = 1, max = 36)
	private String holidayLocationCode;

	@Size(min = 0, max = 16)
	private String contactPhone;

	@NotBlank
	@Size(min = 1, max = 32)
	private String workingHours;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime perKioskProcessTime;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerStartTime;

	@NotNull
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime centerEndTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchStartTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime lunchEndTime;

	@Size(min = 0, max = 64)
	private String timeZone;

	@NotNull
	@Size(min = 1, max = 36)
	private String zoneCode;

}
