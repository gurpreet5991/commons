package io.mosip.kernel.masterdata.entity;

import java.io.Serializable;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@NamedNativeQueries({
		@NamedNativeQuery(name = "RegistrationCenter.findRegistrationCentersByLat", query = "SELECT id, name, cntr_typ_code, addr_line1, addr_line2, addr_line3,latitude, longitude, location_code,holiday_loc_code,contact_phone, number_of_stations,working_hours, lang_code, is_active, cr_by, cr_dtimesz, upd_by,upd_dtimesz, is_deleted, del_dtimesz FROM (SELECT r.id, r.name, r.cntr_typ_code, r.addr_line1, r.addr_line2, r.addr_line3,r.latitude, r.longitude, r.location_code,r.holiday_loc_code,r.contact_phone, r.number_of_stations, r.working_hours, r.lang_code,r.is_active, r.cr_by, r.cr_dtimesz, r.upd_by,r.upd_dtimesz, r.is_deleted, r.del_dtimesz,(2 * 3961 * asin(sqrt((sin(radians((:latitude - CAST(r.latitude AS FLOAT)) / 2))) ^ 2 + cos(radians(CAST(r.latitude AS FLOAT))) * cos(radians(:latitude)) * (sin(radians((:longitude - CAST(r.longitude AS FLOAT)) / 2))) ^ 2))) AS distance FROM master.registration_center r) ss where distance < :proximitydistance and lang_code = :langcode order by distance asc;", resultClass = RegistrationCenter.class) })

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registration_center", schema = "master")
public class RegistrationCenter extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -8541947587557590379L;

	@Id
	@Column(name = "id", unique = true, nullable = false, length = 36)
	private String id;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "cntr_typ_code", length = 36)
	private String centerTypeCode;

	@Column(name = "addr_line1", length = 256)
	private String addressLine1;

	@Column(name = "addr_line2", length = 256)
	private String addressLine2;

	@Column(name = "addr_line3", length = 256)
	private String addressLine3;

	@Column(name = "latitude", length = 32)
	private String latitude;

	@Column(name = "longitude", length = 32)
	private String longitude;

	@Column(name = "location_code", nullable = false, length = 36)
	private String locationCode;

	@Column(name = "contact_phone", length = 16)
	private String contactPhone;

	@Column(name = "number_of_kiosks")
	private Short numberOfKiosks;

	@Column(name = "holiday_loc_code", nullable = false, length = 36)
	private String holidayLocationCode;

	@Column(name = "working_hours", length = 32)
	private String workingHours;

	@Column(name = "per_kiosk_process_time")
	private LocalTime perKioskProcessTime;

	@Column(name = "process_start_time")
	private LocalTime processStartTime;

	@Column(name = "process_end_time")
	private LocalTime processEndTime;

	@Column(name = "lang_code", nullable = false, length = 3)
	private String languageCode;

}
