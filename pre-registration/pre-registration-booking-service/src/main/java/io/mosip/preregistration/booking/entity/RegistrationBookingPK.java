package io.mosip.preregistration.booking.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 
 * To define the composite primary key
 * @author M1046129
 *
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationBookingPK implements Serializable{
	
	private static final long serialVersionUID = -4604149554069906933L;

	/**
	 * Pre registration Id
	 */
	@Column(name="prereg_id")
	private String preregistrationId;
	
	/**
	 * Booking date and time
	 */
	@Column(name="booking_dtimes")
	private LocalDateTime bookingDateTime;
	
}
