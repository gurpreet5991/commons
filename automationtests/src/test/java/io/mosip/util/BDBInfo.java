/**
 * 
 */
package io.mosip.util;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Ramadurai Pandian
 *
 */
public class BDBInfo {

	private byte[] challengeResponse;
	private String index;
	private Long formatOwner;
	private Long formatType;
	private Boolean encryption;
	private LocalDateTime creationDate;
	private LocalDateTime notValidBefore;
	private LocalDateTime notValidAfter;
	private List<SingleType> type;
	private List<String> subtype;
	private ProcessedLevelType level;
	private Long productOwner;
	private Long productType;
	private PurposeType purpose;
	private Integer quality;

	public BDBInfo(BDBInfoBuilder bDBInfoBuilder) {
		this.challengeResponse = bDBInfoBuilder.challengeResponse;
		this.index = bDBInfoBuilder.index;
		this.formatOwner = bDBInfoBuilder.formatOwner;
		this.formatType = bDBInfoBuilder.formatType;
		this.encryption = bDBInfoBuilder.encryption;
		this.creationDate = bDBInfoBuilder.creationDate;
		this.notValidBefore = bDBInfoBuilder.notValidBefore;
		this.notValidAfter = bDBInfoBuilder.notValidAfter;
		this.type = bDBInfoBuilder.type;
		this.subtype = bDBInfoBuilder.subtype;
		this.level = bDBInfoBuilder.level;
		this.productOwner = bDBInfoBuilder.productOwner;
		this.productType = bDBInfoBuilder.productType;
		this.purpose = bDBInfoBuilder.purpose;
		this.quality = bDBInfoBuilder.quality;
	}

	public byte[] getChallengeResponse() {
		return challengeResponse;
	}

	public String getIndex() {
		return index;
	}

	public Long getFormatOwner() {
		return formatOwner;
	}

	public Long getFormatType() {
		return formatType;
	}

	public Boolean getEncryption() {
		return encryption;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public LocalDateTime getNotValidBefore() {
		return notValidBefore;
	}

	public LocalDateTime getNotValidAfter() {
		return notValidAfter;
	}

	public List<SingleType> getType() {
		return type;
	}

	public List<String> getSubtype() {
		return subtype;
	}

	public ProcessedLevelType getLevel() {
		return level;
	}

	public Long getProductOwner() {
		return productOwner;
	}

	public Long getProductType() {
		return productType;
	}

	public PurposeType getPurpose() {
		return purpose;
	}

	public Integer getQuality() {
		return quality;
	}

	public static class BDBInfoBuilder {
		private byte[] challengeResponse;
		private String index;
		private Long formatOwner;
		private Long formatType;
		private Boolean encryption;
		private LocalDateTime creationDate;
		private LocalDateTime notValidBefore;
		private LocalDateTime notValidAfter;
		private List<SingleType> type;
		private List<String> subtype;
		private ProcessedLevelType level;
		private Long productOwner;
		private Long productType;
		private PurposeType purpose;
		private Integer quality;

		public BDBInfoBuilder withChallengeResponse(byte[] challengeResponse) {
			this.challengeResponse = challengeResponse;
			return this;
		}

		public BDBInfoBuilder withIndex(String index) {
			this.index = index;
			return this;
		}

		public BDBInfoBuilder withFormatOwner(Long formatOwner) {
			this.formatOwner = formatOwner;
			return this;
		}

		public BDBInfoBuilder withFormatType(Long formatType) {
			this.formatType = formatType;
			return this;
		}

		public BDBInfoBuilder withEncryption(Boolean encryption) {
			this.encryption = encryption;
			return this;
		}

		public BDBInfoBuilder withCreationDate(LocalDateTime creationDate) {
			this.creationDate = creationDate;
			return this;
		}

		public BDBInfoBuilder withNotValidBefore(LocalDateTime notValidBefore) {
			this.notValidBefore = notValidBefore;
			return this;
		}

		public BDBInfoBuilder withNotValidAfter(LocalDateTime notValidAfter) {
			this.notValidAfter = notValidAfter;
			return this;
		}

		public BDBInfoBuilder withType(List<SingleType> type) {
			this.type = type;
			return this;
		}

		public BDBInfoBuilder withSubtype(List<String> subtype) {
			this.subtype = subtype;
			return this;
		}

		public BDBInfoBuilder withLevel(ProcessedLevelType level) {
			this.level = level;
			return this;
		}

		public BDBInfoBuilder withProductOwner(Long productOwner) {
			this.productOwner = productOwner;
			return this;
		}

		public BDBInfoBuilder withProductType(Long productType) {
			this.productType = productType;
			return this;
		}

		public BDBInfoBuilder withPurpose(PurposeType purpose) {
			this.purpose = purpose;
			return this;
		}

		public BDBInfoBuilder withQuality(Integer quality) {
			this.quality = quality;
			return this;
		}

		public BDBInfo build() {
			return new BDBInfo(this);
		}
	}

	public BDBInfoType toBDBInfo() {
		BDBInfoType bDBInfoType = new BDBInfoType();
		if (getChallengeResponse() != null && getChallengeResponse().length > 0) {
			bDBInfoType.setChallengeResponse(getChallengeResponse());
		}
		if (getIndex() != null && getIndex().length() > 0) {
			bDBInfoType.setIndex(getIndex());
		}
		if (getFormatOwner() != null && getFormatOwner() > 0) {
			bDBInfoType.setFormatOwner(getFormatOwner());
		}
		if (getFormatType() != null && getFormatType() > 0) {
			bDBInfoType.setFormatType(getFormatType());
		}
		if (getEncryption() != null) {
			bDBInfoType.setEncryption(getEncryption());
		}
		if (getCreationDate() != null) {
			bDBInfoType.setCreationDate(getCreationDate());
		}
		if (getNotValidBefore() != null) {
			bDBInfoType.setNotValidBefore(getNotValidBefore());
		}
		if (getNotValidAfter() != null) {
			bDBInfoType.setNotValidAfter(getNotValidAfter());
		}
		if (getType() != null) {
			bDBInfoType.setType(getType());
		}
		if (getSubtype() != null) {
			bDBInfoType.setSubtype(getSubtype());
		}
		if (getLevel() != null) {
			bDBInfoType.setLevel(getLevel());
		}
		if (getProductOwner() != null && getProductOwner() > 0) {
			bDBInfoType.setProductOwner(getProductOwner());
		}
		if (getProductType() != null && getProductType() > 0) {
			bDBInfoType.setProductType(getProductType());
		}
		if (getPurpose() != null) {
			bDBInfoType.setPurpose(getPurpose());
		}
		if (getQuality() != null) {
			bDBInfoType.setQuality(getQuality());
		}
		return bDBInfoType;
	}
}
