package it.eng.spagobi.tools.license;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

//Verify the signature of jar and files inside jar
public class Check {

	public static final String ERROR_MESSAGE = "Error while checking the server manager integrity.";

	// the certificate
	private static final String CERTIFICATE_FILE_NAME = "/it/eng/knowage/tools/servermanager/importexport/configs.bin";

	// the sha1 checksum of certificate
	private static final String CHECKSUM_CERTIFICATE = "a874b95af664029ae211e24ffd61b015c19f8ffe";

	// Flag for avoiding unnecessary self-integrity checking.
	private static boolean verifiedSelfIntegrity = false;

	// Provider's signing cert which is used to sign the jar.
	private static X509Certificate providerCert = null;

	/**
	 * Perform self-integrity checking. Call this method in all the constructors of your SPI implementation classes. NOTE: The following implementation assumes
	 * that all your provider implementation is packaged inside ONE jar.
	 */
	public static synchronized boolean selfIntegrityChecking() {
		return true;
		/*
		 * if (verifiedSelfIntegrity) { return true; }
		 * 
		 * URL providerURL = AccessController.doPrivileged(new PrivilegedAction<URL>() {
		 * 
		 * @Override public URL run() { CodeSource cs = Check.class.getProtectionDomain().getCodeSource(); return cs.getLocation(); } });
		 * 
		 * if (providerURL == null) { return false; }
		 * 
		 * // Open a connnection to the provider JAR file JarVerifier jv = new JarVerifier(providerURL);
		 * 
		 * // Make sure that the provider JAR file is signed with // provider's own signing certificate. try { if (providerCert == null) { providerCert =
		 * setupProviderCert(); if (providerCert == null) { // cert replaced return false; } } jv.verify(providerCert); } catch (Exception e) {
		 * e.printStackTrace(); return false; }
		 * 
		 * verifiedSelfIntegrity = true; return true;
		 */
	}

	/*
	 * Set up 'providerCert' with the certificate bytes.
	 */
	private static X509Certificate setupProviderCert() throws IOException, CertificateException, NoSuchAlgorithmException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream inStream = getCertificate();
		if (inStream == null) {
			return null;
		}
		X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
		inStream.close();
		return cert;
	}

	private static InputStream getCertificate() throws NoSuchAlgorithmException, IOException {
		if (!verifyCertificateChecksum()) {
			return null;
		}
		return Check.class.getResourceAsStream(CERTIFICATE_FILE_NAME);
	}

	// verify the checksum of certificate
	private static boolean verifyCertificateChecksum() throws NoSuchAlgorithmException, IOException {
		try (InputStream fis = Check.class.getResourceAsStream(CERTIFICATE_FILE_NAME)) {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] dataBytes = new byte[1024];

			int nread = 0;

			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}

			byte[] mdbytes = md.digest();

			// convert the byte to hex format
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			if (!sb.toString().equals(CHECKSUM_CERTIFICATE)) {
				return false;
			}
		}
		return true;
	}

	static class JarVerifier {

		private URL jarURL = null;
		private JarFile jarFile = null;

		JarVerifier(URL jarURL) {
			this.jarURL = jarURL;
		}

		/**
		 * Retrive the jar file from the specified url.
		 */
		private JarFile retrieveJarFileFromURL(URL url) throws PrivilegedActionException, MalformedURLException {
			JarFile jf = null;

			// Prep the url with the appropriate protocol.
			jarURL = url.getProtocol().equalsIgnoreCase("jar") ? url : new URL("jar:" + url.toString() + "!/");
			// Retrieve the jar file using JarURLConnection
			jf = AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {
				@Override
				public JarFile run() throws Exception {
					JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
					// Always get a fresh copy, so we don't have to
					// worry about the stale file handle when the
					// cached jar is closed by some other application.
					conn.setUseCaches(false);
					return conn.getJarFile();
				}
			});
			return jf;
		}

		/**
		 * First, retrieve the jar file from the URL passed in constructor. Then, compare it to the expected X509Certificate. If everything went well and the
		 * certificates are the same, no exception is thrown.
		 */
		@SuppressWarnings("rawtypes")
		public void verify(X509Certificate targetCert) throws IOException {
			// Sanity checking
			if (targetCert == null) {
				throw new SecurityException("Provider certificate is invalid");
			}

			try {
				if (jarFile == null) {
					jarFile = retrieveJarFileFromURL(jarURL);
				}
			} catch (Exception ex) {
				SecurityException se = new SecurityException();
				se.initCause(ex);
				throw se;
			}

			Vector<JarEntry> entriesVec = new Vector<JarEntry>();

			// Ensure the jar file is signed.
			Manifest man = jarFile.getManifest();
			if (man == null) {
				throw new SecurityException("The provider is not signed");
			}

			// Ensure all the entries' signatures verify correctly
			byte[] buffer = new byte[8192];
			Enumeration entries = jarFile.entries();

			while (entries.hasMoreElements()) {
				JarEntry je = (JarEntry) entries.nextElement();

				// Skip directories.
				if (je.isDirectory())
					continue;
				entriesVec.addElement(je);
				InputStream is = jarFile.getInputStream(je);

				// Read in each jar entry. A security exception will
				// be thrown if a signature/digest check fails.
				while (is.read(buffer, 0, buffer.length) != -1) {
					// Don't care
				}
				is.close();
			}

			// Get the list of signer certificates
			Enumeration e = entriesVec.elements();

			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();

				// Every file must be signed except files in META-INF.
				Certificate[] certs = je.getCertificates();
				if ((certs == null) || (certs.length == 0)) {
					if (!je.getName().startsWith("META-INF"))
						throw new SecurityException("The provider " + "has unsigned " + "class files.");
				} else {
					// Check whether the file is signed by the expected
					// signer. The jar may be signed by multiple signers.
					// See if one of the signers is 'targetCert'.
					int startIndex = 0;
					X509Certificate[] certChain;
					boolean signedAsExpected = false;

					while ((certChain = getAChain(certs, startIndex)) != null) {
						if (certChain[0].equals(targetCert)) {
							// Stop since one trusted signer is found.
							signedAsExpected = true;
							break;
						}
						// Proceed to the next chain.
						startIndex += certChain.length;
					}

					if (!signedAsExpected) {
						throw new SecurityException("The provider " + "is not signed by a " + "trusted signer");
					}
				}
			}
		}

		/**
		 * Extracts ONE certificate chain from the specified certificate array which may contain multiple certificate chains, starting from index 'startIndex'.
		 */
		private static X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
			if (startIndex > certs.length - 1)
				return null;

			int i;
			// Keep going until the next certificate is not the
			// issuer of this certificate.
			for (i = startIndex; i < certs.length - 1; i++) {
				if (!((X509Certificate) certs[i + 1]).getSubjectDN().equals(((X509Certificate) certs[i]).getIssuerDN())) {
					break;
				}
			}
			// Construct and return the found certificate chain.
			int certChainSize = (i - startIndex) + 1;
			X509Certificate[] ret = new X509Certificate[certChainSize];
			for (int j = 0; j < certChainSize; j++) {
				ret[j] = (X509Certificate) certs[startIndex + j];
			}
			return ret;
		}

		// Close the jar file once this object is no longer needed.
		@Override
		protected void finalize() throws Throwable {
			jarFile.close();
		}
	}

	public static void main(String[] args) {
		System.out.println(selfIntegrityChecking());
	}
}