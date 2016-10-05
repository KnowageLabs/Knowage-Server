package it.eng.spagobi.tools.license;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.license4j.ActivationStatus;
import com.license4j.HardwareID;
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
import com.license4j.util.FileUtils;

public class LicenseManager {

	static private Logger logger = Logger.getLogger(LicenseManager.class);

	public static final String ERROR_MESSAGE = "Error while checking licenses for the server manager.";
	public static final String FILE_LICENSE_PATTERN = "-license.lic"; // e.g. KnowageBD-license.lic

	static private final String NUMBER_OF_PROCESSORS = "Processors";
	static private final String LICENSE_PATH_SUFFIX = "lic";
	static private final String PUBLIC_KEY = "30819f300d06092a864886f70d010101050003818d003081893032301006"
			+ "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0" + "00408aa9c586fe1056b9f35d9482ef73dcd1cd9a1288da2aa7693e23cf3G"
			+ "028181009afb2381cd9787b60f4b6e42e5fc5deb3c1b65ea38b7938cea1d" + "176921475ba8a6627d7ca39bd21fef8ff11e2d4ec04af21d17eea994dc97"
			+ "b3569e5302f84e3da416229e661af6af746478618a20274b8b8ab5a3e5e7" + "2f10f4ce0cf9c619b47b03RSA4102413SHA512withRSAe93df72fa936550"
			+ "575f5a05b41aaf39d05ad212066eec923c95902a0fc17d61f0203010001";

	static private String hardwareFingerprint;

	static {
		hardwareFingerprint = HardwareID.getHardwareIDFromHostName() + HardwareID.getHardwareIDFromVolumeSerialNumber()
				+ Runtime.getRuntime().availableProcessors();
	}

	private static boolean checkProcessors(String customSignedFeature) {
		logger.debug("IN");
		int licensedProcessors = 0;
		try {
			licensedProcessors = Integer.parseInt(customSignedFeature);
		} catch (NumberFormatException e) {
			return false;
		}
		int actualProcessors = Runtime.getRuntime().availableProcessors();
		if (licensedProcessors != actualProcessors) {
			return false;
		} else {
			return true; // number of processors is the same as in the license
		}
	}

	public static Map<String, License> getLicenses(boolean onlyValid) {
		Map<String, License> licenses = new HashMap<String, License>();
		logger.debug("IN");
		String licensePath = getLicensePath();
		if (licensePath != null && !licensePath.isEmpty()) {
			File dir = new File(licensePath);
			if (dir.isDirectory()) {
				File[] fileLicenses = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						return filename.endsWith(FILE_LICENSE_PATTERN);
					}
				});

				licenses = getLicensesFromFiles(fileLicenses, onlyValid);
			}
		}
		logger.debug("OUT");
		return licenses;
	}

	public static Map<String, String[]> checkCustomFeaturesValidity(Collection<License> licenses, Locale locale) {
		// <license id, <msg status, msg>>
		Map<String, String[]> errors = new HashMap<String, String[]>();
		for (License license : licenses) {
			String[] msgs = new String[2];
			if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
				if (license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS) == null
						|| license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS).equals("")) {
					String msg = MessageBuilderFactory.getMessageBuilder().getMessage("sbi.tools.license.noProcessors", locale);
					msgs[0] = "OK";
					msgs[1] = msg;
					errors.put(license.getLicenseText().getLicenseValidProductID(), msgs);
				} else if (!checkProcessors(license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS))) {
					String msg = MessageBuilderFactory.getMessageBuilder().getMessage("sbi.tools.license.processorsNumberError", locale);
					msgs[0] = "KO";
					msgs[1] = msg;
					errors.put(license.getLicenseText().getLicenseValidProductID(), msgs);
				}
			}
		}
		return errors;
	}

	public static String[] checkCustomFeatureValidity(License license, Locale locale) {
		// contains a code OK, KO and the message
		String[] messageToReturn = new String[2];

		if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
			if (license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS) == null
					|| license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS).equals("")) {
				String msg = MessageBuilderFactory.getMessageBuilder().getMessage("sbi.tools.license.noProcessors", locale);
				messageToReturn[0] = "OK";
				messageToReturn[1] = msg;
			} else if (!checkProcessors(license.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS))) {
				String msg = MessageBuilderFactory.getMessageBuilder().getMessage("sbi.tools.license.processorsNumberError", locale);
				messageToReturn[0] = "KO";
				messageToReturn[1] = msg;
			}
		}
		return messageToReturn;
	}

	private static Map<String, License> getLicensesFromFiles(File[] fileLicenses, boolean onlyValid) {
		Map<String, License> licenses = new HashMap<String, License>();
		logger.debug("IN");
		for (int i = 0; i < fileLicenses.length; i++) {
			if (fileLicenses[i].isFile() && fileLicenses[i].canRead()) {
				File currentLicense = fileLicenses[i];
				try {
					logger.debug("Reading license file...");
					String activatedLicense = FileUtils.readFile(currentLicense.getAbsolutePath());
					logger.debug("Validating license...");
					License licenseOnDisk = LicenseValidator.validateWithCustomHardwareID(activatedLicense, PUBLIC_KEY, null, null, null, getHardwareID(),
							null, null);
					if (onlyValid) {
						logger.debug("Insering only valid licenses in the list...");
						if (licenseOnDisk.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
							if (licenseOnDisk.getActivationStatus() == ActivationStatus.ACTIVATION_NOT_REQUIRED) {
								// developers licenses does not require activation
								String productId = licenseOnDisk.getLicenseText().getLicenseValidProductID();
								licenses.put(productId, licenseOnDisk);
							} else {
								logger.debug("Checking number of processors...");
								if (checkProcessors(licenseOnDisk.getLicenseText().getCustomSignedFeature(NUMBER_OF_PROCESSORS))) {
									String productId = licenseOnDisk.getLicenseText().getLicenseValidProductID();
									licenses.put(productId, licenseOnDisk);
								}
							}
						}
					} else {
						logger.debug("Insering all the licenses in the list...");
						String productId = licenseOnDisk.getLicenseText().getLicenseValidProductID();
						licenses.put(productId, licenseOnDisk);
					}
				} catch (IOException e) {
					// no log
				}
			}
		}
		logger.debug("OUT");
		return licenses;
	}

	public static String getLicensePath() {
		return SpagoBIUtilities.getRootResourcePath() + (SpagoBIUtilities.getRootResourcePath().endsWith(File.separatorChar + "") ? "" : File.separatorChar)
				+ LICENSE_PATH_SUFFIX;
	}

	public static String getHardwareID() {
		logger.debug("IN");
		String hardwareID = "";
		logger.debug("Converting to binary contents");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(hardwareFingerprint.getBytes(StandardCharsets.UTF_8));
			hardwareID = javax.xml.bind.DatatypeConverter.printHexBinary(hash);
		} catch (Exception e) {
			// do nothing
		}
		logger.debug("OUT");
		return hardwareID;
	}

	public static Set<String> readFunctionalityByLicense(SpagoBIUserProfile user) {
		logger.debug("IN");
		Map<String, License> licenses = getLicenses(true);
		Set<String> superadminLicenseFunctionalities = new HashSet<String>();
		Boolean isSuperAdm = user.getIsSuperadmin();
		if (licenses != null && !licenses.isEmpty()) {
			for (String key : licenses.keySet()) {
				License license = licenses.get(key);
				if (license.getValidationStatus() == ValidationStatus.LICENSE_VALID) {
					Map<String, String> licenseFunctionalities = license.getLicenseText().getCustomSignedFeatures();
					if (licenseFunctionalities != null && !licenseFunctionalities.isEmpty()) {
						if (isSuperAdm != null && isSuperAdm) {
							Set<String> functionalities = licenseFunctionalities.keySet();
							functionalities.remove(NUMBER_OF_PROCESSORS);
							superadminLicenseFunctionalities.addAll(functionalities);
						}
					}
				}
			}
		}
		logger.debug("OUT");
		return superadminLicenseFunctionalities;
	}

	public static boolean hasOneOrMoreLicenses() {
		Map<String, License> licenses = getLicenses(true);
		if (licenses == null || licenses.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public static String getPublicKey() {
		return PUBLIC_KEY;
	}

}
