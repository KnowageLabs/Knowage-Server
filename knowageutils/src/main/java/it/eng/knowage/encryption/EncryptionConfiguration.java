package it.eng.knowage.encryption;

public class EncryptionConfiguration {

	private final EncryptionConfigurationType type;
	private String pmUrl;
	private String pmUser;
	private String pmPwd;
	private String pmApplication;

	private String algorithm;
	private String encryptionPwd;

	public EncryptionConfiguration(EncryptionConfigurationType type) {
		super();
		this.type = type;
	}

	/**
	 * @return the pmUrl
	 */
	public String getPmUrl() {
		return pmUrl;
	}

	/**
	 * @param pmUrl the pmUrl to set
	 */
	public void setPmUrl(String pmUrl) {
		this.pmUrl = pmUrl;
	}

	/**
	 * @return the pmUser
	 */
	public String getPmUser() {
		return pmUser;
	}

	/**
	 * @param pmUser the pmUser to set
	 */
	public void setPmUser(String pmUser) {
		this.pmUser = pmUser;
	}

	/**
	 * @return the pmPwd
	 */
	public String getPmPwd() {
		return pmPwd;
	}

	/**
	 * @param pmPwd the pmPwd to set
	 */
	public void setPmPwd(String pmPwd) {
		this.pmPwd = pmPwd;
	}

	/**
	 * @return the pmApplication
	 */
	public String getPmApplication() {
		return pmApplication;
	}

	/**
	 * @param pmApplication the pmApplication to set
	 */
	public void setPmApplication(String pmApplication) {
		this.pmApplication = pmApplication;
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @return the type
	 */
	public EncryptionConfigurationType getType() {
		return type;
	}

	/**
	 * @return the encryptionPwd
	 */
	public String getEncryptionPwd() {
		return encryptionPwd;
	}

	/**
	 * @param encryptionPwd the encryptionPwd to set
	 */
	public void setEncryptionPwd(String encryptionPwd) {
		this.encryptionPwd = encryptionPwd;
	}

}
