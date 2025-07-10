package it.eng.spagobi.commons.utilities;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrData.Builder;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.util.Utils;

public class TOTPService {

	/**
	 * Generate a new TOTP secret key. This secret is used to generate time-based one-time passwords.
	 *
	 * @return A base32-encoded secret string.
	 */
	public static String generateSecret() {
		DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
		return secretGenerator.generate();
	}

	/**
	 * Verify a TOTP code using the provided secret. This method uses a strict time window (no time drift allowed).
	 *
	 * @param secret The shared secret key.
	 * @param code   The TOTP code to verify.
	 * @return True if the code is valid, false otherwise.
	 */
	public static boolean verifyCode(String secret, String code) {
		if (code == null) {
			return false;
		}

		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());

		verifier.setTimePeriod(30); // 30-second time window
		verifier.setAllowedTimePeriodDiscrepancy(0); // No time drift allowed

		return verifier.isValidCode(secret, code);
	}

	/**
	 * Generate a Base64-encoded QR code image for TOTP setup. This QR code can be scanned by authenticator apps like Google Authenticator.
	 *
	 * @param username The label (usually the username or email).
	 * @param issuer   The issuer name (e.g., your application name).
	 * @param secret   The shared secret key.
	 * @return A Base64-encoded data URI representing the QR code image.
	 */
	public static String getQRBarcodeURL(String username, String issuer, String secret) {
		QrData data = new Builder().label(username).secret(secret).issuer(issuer).algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();

		QrGenerator generator = new ZxingPngQrGenerator();

		try {
			return Utils.getDataUriForImage(generator.generate(data), generator.getImageMimeType());
		} catch (Exception e) {
			throw new RuntimeException("Unable to generate QR code", e);
		}
	}
}
