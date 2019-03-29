package io.mosip.registration.processor.core.abstractverticle;

import java.io.Serializable;

/**
 * This class contains the address values to be used in Registration process.
 *
 * @author Pranav Kumar
 * @author Mukul Puspam
 * @since 0.0.1
 */
public class MessageBusAddress implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new message bus address.
	 */
	public MessageBusAddress() {
	}

	/** The address. */
	private String address;

	/**
	 * Instantiates a new message bus address.
	 *
	 * @param address
	 *            The bus address
	 */
	public MessageBusAddress(String address) {
		this.address = address;
	}

	/**
	 * Gets the address.
	 *
	 * @return The address
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Sets the address.
	 *
	 * @param address
	 *            the new address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/** The Constant BATCH_BUS. */
	public static final MessageBusAddress BATCH_BUS = new MessageBusAddress("batch-bus");

	/** The Constant STRUCTURE_BUS_IN. */
	public static final MessageBusAddress PACKET_VALIDATOR_BUS_IN = new MessageBusAddress("packet-validator-bus-in");

	/** The Constant STRUCTURE_BUS_OUT. */
	public static final MessageBusAddress PACKET_VALIDATOR_BUS_OUT = new MessageBusAddress("packet-validator-bus-out");

	/** The Constant BIOMETRIC_BUS_IN. */
	public static final MessageBusAddress BIO_DEDUPE_BUS_IN = new MessageBusAddress("bio-dedupe-bus-in");

	/** The Constant BIOMETRIC_BUS_OUT. */
	public static final MessageBusAddress BIO_DEDUPE_BUS_OUT = new MessageBusAddress("bio-dedupe-bus-out");

	/** The Constant FAILURE_BUS. */
	public static final MessageBusAddress FAILURE_BUS = new MessageBusAddress("failure-bus-in");

	/** The Constant RETRY_BUS. */
	public static final MessageBusAddress RETRY_BUS = new MessageBusAddress("retry-bus-in");

	/** The Constant ERROR. */
	public static final MessageBusAddress ERROR = new MessageBusAddress("error-bus-in");

	/** The Constant QUALITY_CHECK_BUS. */
	public static final MessageBusAddress QUALITY_CHECK_BUS = new MessageBusAddress("quality_check_bus-in");

	/** The Constant VIRUS_SCAN_BUS_IN. */
	public static final MessageBusAddress VIRUS_SCAN_BUS_IN = new MessageBusAddress("virus-scanner-bus-in");

	/** The Constant VIRUS_SCAN_BUS_OUT. */
	public static final MessageBusAddress VIRUS_SCAN_BUS_OUT = new MessageBusAddress("virus-scanner-bus-out");

	/** The Constant FTP_SCAN_BUS. */
	public static final MessageBusAddress FTP_SCAN_BUS_OUT = new MessageBusAddress("ftp-scanner-stage");

	/** The Constant PACKET_RECEIVER_OUT. */
	public static final MessageBusAddress PACKET_RECEIVER_OUT = new MessageBusAddress("packet-receiver-bus-out");

	/** The Constant STRUCTURE_BUS_IN. */
	public static final MessageBusAddress OSI_BUS_IN = new MessageBusAddress("osi-validator-bus-in");

	/** The Constant STRUCTURE_BUS_OUT. */
	public static final MessageBusAddress OSI_BUS_OUT = new MessageBusAddress("osi-validator-bus-out");

	/** The Constant DEMODEDUPE_BUS_IN. */
	public static final MessageBusAddress DEMO_DEDUPE_BUS_IN = new MessageBusAddress("demo-dedupe-bus-in");

	/** The Constant DEMODEDUPE_BUS_OUT. */
	public static final MessageBusAddress DEMO_DEDUPE_BUS_OUT = new MessageBusAddress("demo-dedupe-bus-out");

	/** The Constant MANUAL_VERIFICATION_BUS. */
	public static final MessageBusAddress MANUAL_VERIFICATION_BUS = new MessageBusAddress("manual-verification-bus-in");

	/** The Constant UIN_GENERATION_BUS_IN. */
	public static final MessageBusAddress UIN_GENERATION_BUS_IN = new MessageBusAddress("uin-generator-bus-in");

	/** The Constant UIN_GENERATION_BUS_OUT. */
	public static final MessageBusAddress UIN_GENERATION_BUS_OUT = new MessageBusAddress("uin-generator-bus-out");

	/** The Constant PACKET_UPLOADER_IN. */
	public static final MessageBusAddress PACKET_UPLOADER_IN = new MessageBusAddress("packet-uploader-bus-in");

	/** The Constant PACKET_UPLOADER_OUT. */
	public static final MessageBusAddress PACKET_UPLOADER_OUT = new MessageBusAddress("packet-uploader-bus-out");
	
	/** The Constant MESSAGE_SENDER_BUS_IN. */
	public static final MessageBusAddress MESSAGE_SENDER_BUS = new MessageBusAddress("message-sender-bus-in");

	/** The Constant REGISTRATION_CONNECTOR_BUS_OUT. */
	public static final MessageBusAddress REGISTRATION_CONNECTOR_BUS_OUT = new MessageBusAddress(
			"registration-connector-bus-out");

	/** The Constant PRINTING_BUS_IN. */
	public static final MessageBusAddress PRINTING_BUS = new MessageBusAddress("printing-bus-in");

}
