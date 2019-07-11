package io.mosip.dbentity;

public enum TransactionStatus {

	/** The packet uploaded to landing zone. */
	PACKET_UPLOADED_TO_LANDING_ZONE,

    /** The packet uploaded to virus scan. */
    PACKET_UPLOADED_TO_VIRUS_SCAN,

    /** The virus scan failed. */
    VIRUS_SCAN_FAILED,

    /** The virus scan successful. */
    VIRUS_SCAN_SUCCESS,

    /** The packet uploaded to filesystem. */
    PACKET_UPLOADED_TO_FILESYSTEM,

    /** The duplicate packet recieved. */
    DUPLICATE_PACKET_RECIEVED,

    /** The invalid packet format. */
    INVALID_PACKET_FORMAT,

    /** The packet not present in request. */
    PACKET_NOT_PRESENT_IN_REQUEST,

    /** The packet size greater than limit. */
    PACKET_SIZE_GREATER_THAN_LIMIT,

    /** The packet decryption successful. */
    PACKET_DECRYPTION_SUCCESS,

    /** The packet decryption failed. */
    PACKET_DECRYPTION_FAILED,

    /** The structure validation success. */
    STRUCTURE_VALIDATION_SUCCESS,

    /** The structure validation failed. */
    STRUCTURE_VALIDATION_FAILED,

    /** The packet not yet sync. */
    PACKET_NOT_YET_SYNC,

    /** The packet data store successful. */
    PACKET_DATA_STORE_SUCCESS,

    /** The packet data store failed. */
    PACKET_DATA_STORE_FAILED,

    /** The packet osi validation successful. */
    PACKET_OSI_VALIDATION_SUCCESS,

    /** The packet osi validation failed. */
    PACKET_OSI_VALIDATION_FAILED,

    /** The packet demo dedupe successful. */
    PACKET_DEMO_DEDUPE_SUCCESS,

    /** The packet demo potential match. */
    PACKET_DEMO_POTENTIAL_MATCH,

    /** The packet demo dedupe failed. */
    PACKET_DEMO_DEDUPE_FAILED,

    /** The packet bio dedupe successful. */
    PACKET_BIO_DEDUPE_SUCCESS,

    /** The packet bio potential match. */
    PACKET_BIO_POTENTIAL_MATCH,

    /** The packet bio dedupe failed. */
    PACKET_BIO_DEDUPE_FAILED,

    /** The uin generated. */
    UIN_GENERATED,

    /** The manual adjudication success. */
    MANUAL_ADJUDICATION_SUCCESS,

    /** The manual adjudication failed. */
    MANUAL_ADJUDICATION_FAILED,

    /** The virus scan success. */
    VIRUS_SCAN_SUCCESSFUL,
    
    /** The Packet Upload Failed */
    PACKET_UPLOAD_TO_VIRUS_SCAN_FAILED,
    
    /** Previous Stage prior to current stage failed*/
    PREVIOUS_STAGE_FAILED,
    
    /** Packet UIN Updated */
    PACKET_UIN_UPDATION_SUCCESS,
    
    /** UIN generation Failed */
    UIN_GENERATION_FAILED

}
