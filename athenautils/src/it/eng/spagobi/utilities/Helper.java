package it.eng.spagobi.utilities;

import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

/**
 *
 * Helper class with useful methods.
 *
 */
public class Helper {

	private static MessageDigest md5instance;

	/**
	 * ISO format
	 */

	private Helper() {

	}

	/**
	 * Check if the value is not null and not empty (after a trim)
	 *
	 * @param value
	 * @param name
	 */
	public static void checkNotNullNotTrimNotEmpty(String value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(String.format("%s must not be null.", name));
		}

		if (value.trim().length() != value.length()) {
			throw new IllegalArgumentException(String.format("%s must not contain trailing/leading spaces.", name));
		}

		if (value.isEmpty()) {
			throwEmpty(name);
		}

	}

	private static void throwEmpty(String name) {
		throw new IllegalArgumentException(String.format("%s must not be empty.", name));
	}

	/**
	 * check if value is not negative
	 *
	 * @param value
	 * @param name
	 */
	public static void checkNotNegative(double value, String name) {
		if (value < 0) {
			throwNotNegative(name);
		}

	}

	/**
	 * check if value is not negative
	 *
	 * @param value
	 * @param name
	 */
	public static void checkNotNegative(long value, String name) {
		if (value < 0) {
			throwNotNegative(name);
		}

	}

	private static void throwNotNegative(String name) {
		throw new IllegalArgumentException(String.format("%s must not be negative.", name));
	}

	/**
	 * check if o is not null
	 *
	 * @param o
	 * @param name
	 */
	public static void checkNotNull(Object o, String name) {
		if (o == null) {
			throw new IllegalArgumentException(String.format("%s must not be null.", name));
		}
	}

	/**
	 * check if from is before to
	 *
	 * @param from
	 * @param to
	 * @param fromName
	 * @param toName
	 */
	public static void checkGreater(Date from, Date to, String fromName, String toName) {
		if (from.getTime() >= to.getTime()) {
			throwGreater(fromName, toName);
		}
	}

	/**
	 * check if b is greater than a
	 *
	 * @param a
	 * @param b
	 * @param aName
	 * @param bName
	 */
	public static void checkGreater(long a, long b, String aName, String bName) {
		if (a >= b) {
			throwGreater(aName, bName);
		}
	}

	private static void throwGreater(String fromName, String toName) {
		throw new IllegalArgumentException(String.format("%s must be greather than %s.", toName, fromName));
	}

	/**
	 * check if value is positive, greater than 0
	 *
	 * @param value
	 * @param name
	 */
	public static void checkPositive(double value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(String.format("%s must be greather than 0.", name));
		}
	}

	/**
	 * check if the collection is not empty
	 *
	 * @param coll
	 * @param name
	 */
	public static void checkNotEmpty(Collection<?> coll, String name) {
		if (coll.isEmpty()) {
			throw new IllegalArgumentException(String.format("%s must be not empty.", name));
		}

	}

	/**
	 * check if the collection is without null values
	 *
	 * @param coll
	 * @param name
	 */
	public static void checkWithoutNulls(Collection<?> coll, String name) {
		for (Object o : coll) {
			if (o == null) {
				throw new IllegalArgumentException(String.format("%s must not contain null items.", name));
			}
		}

	}

	/**
	 * check if the value is not empty (after a trim)
	 *
	 * @param value
	 * @param name
	 */
	public static void checkNotTrimNotEmpty(String value, String name) {
		assert value != null;

		if (value.trim().isEmpty()) {
			throwEmpty(name);
		}

	}

	/**
	 * convert the xml object to a printable {@link String}
	 *
	 * @param xmlE
	 * @return
	 */
	public static String toString(Object xmlE) {
		try {
			JAXBContext context = JAXBContext.newInstance(xmlE.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter out = new StringWriter();
			marshaller.marshal(xmlE, out);
			String xml = out.toString();
			return xml;
		} catch (PropertyException e) {
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public static void checkArgument(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}

	}

	public static void checkNotEmpty(String value, String name) {
		Assert.assertTrue(value != null, "value!=null");
		if (value.isEmpty()) {
			throw new IllegalArgumentException(String.format("%s must not be empty", name));
		}

	}

	public static String md5(String res) {
		try {
			byte[] bytesOfMessage = res.getBytes("UTF-8");
			MessageDigest md = getMD5Instance();
			byte[] thedigest = md.digest(bytesOfMessage);
			return new String(thedigest, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SpagoBIRuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private static synchronized MessageDigest getMD5Instance() throws NoSuchAlgorithmException {
		if (md5instance==null) {
			md5instance=MessageDigest.getInstance("MD5");
		}
		return md5instance;
	}

	public static String toNullIfempty(String s) {
		if (s != null && s.isEmpty()) {
			s = null;
		}
		return s;
	}

}
