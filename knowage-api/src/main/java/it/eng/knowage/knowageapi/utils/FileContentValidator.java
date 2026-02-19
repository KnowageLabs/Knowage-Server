/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.boot.error.KnowageRuntimeException;

/**
 * Utility class to validate uploaded files:
 * 1. Verify file extension matches actual content
 * 2. Detect corrupted files by attempting partial parsing
 * <p>
 * Supported formats: XML, JSON, CSV, XLS, XLSX
 *
 * @author Knowage
 */
public class FileContentValidator {

	private static final Logger logger = Logger.getLogger(FileContentValidator.class);

	// Magic bytes signatures for file type detection
	private static final byte[] ZIP_SIGNATURE = new byte[] { 0x50, 0x4B, 0x03, 0x04 }; // PK.. (ZIP/XLSX)
	private static final byte[] OLE2_SIGNATURE = new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 }; // XLS

	private static final int BUFFER_SIZE = 8192; // 8KB for initial read

	/**
	 * Validates that the file content matches its declared extension and is not corrupted.
	 *
	 * @param inputStream The file input stream (will be consumed and recreated)
	 * @param fileName The declared filename with extension
	 * @return A new InputStream with the same content (caller must close it)
	 * @throws KnowageRuntimeException if validation fails
	 */
	public static InputStream validateFileContent(InputStream inputStream, String fileName) throws IOException {
		if (inputStream == null || fileName == null) {
			throw new KnowageRuntimeException("Invalid parameters: inputStream and fileName cannot be null");
		}

		String extension = getFileExtension(fileName).toLowerCase();

		// Skip validation for non-supported formats
		if (!isSupportedFormat(extension)) {
			logger.debug("File extension '" + extension + "' not in validation scope, skipping validation");
			return inputStream;
		}

		// Read the entire stream into a byte array so we can re-use it
		byte[] fileContent = readFullStream(inputStream);

		try {
			// Validate based on extension
			switch (extension) {
				case "xml":
					validateXML(fileContent);
					break;
				case "json":
					validateJSON(fileContent);
					break;
				case "csv":
					validateCSV(fileContent);
					break;
				case "xls":
					validateXLS(fileContent);
					break;
				case "xlsx":
					validateXLSX(fileContent);
					break;
				default:
					// Already checked in isSupportedFormat, should not happen
					break;
			}
		} catch (Exception e) {
			logger.error("File validation failed for " + fileName + ": " + e.getMessage(), e);
			throw new KnowageRuntimeException("Invalid or corrupted file: " + e.getMessage(), e);
		}

		// Return a new InputStream from the byte array
		return new ByteArrayInputStream(fileContent);
	}

	/**
	 * Checks if the file format is supported for validation
	 */
	private static boolean isSupportedFormat(String extension) {
		return Arrays.asList("xml", "json", "csv", "xls", "xlsx").contains(extension);
	}

	/**
	 * Extracts file extension from filename
	 */
	private static String getFileExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
			return fileName.substring(lastDotIndex + 1);
		}
		return "";
	}

	/**
	 * Reads entire InputStream into byte array
	 */
	private static byte[] readFullStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int nRead;
		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	/**
	 * Validates XML file: checks XML header and attempts parsing
	 */
	private static void validateXML(byte[] content) throws Exception {
		if (content.length == 0) {
			throw new KnowageRuntimeException("File is empty");
		}

		// Check for XML declaration or root element
		String header = new String(content, 0, Math.min(1000, content.length), StandardCharsets.UTF_8);
		String trimmedHeader = header.trim();

		if (!trimmedHeader.startsWith("<?xml") && !trimmedHeader.startsWith("<")) {
			throw new KnowageRuntimeException("File does not appear to be valid XML (missing XML header or root element)");
		}

		// Attempt to parse with a basic XML parser (SAX for security)
		try {
			javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			javax.xml.parsers.SAXParser parser = factory.newSAXParser();
			parser.parse(new ByteArrayInputStream(content), new org.xml.sax.helpers.DefaultHandler());
		} catch (org.xml.sax.SAXException e) {
			throw new KnowageRuntimeException("XML file is corrupted or malformed: " + e.getMessage());
		}
	}

	/**
	 * Validates JSON file: attempts parsing with Jackson
	 */
	private static void validateJSON(byte[] content) {
		if (content.length == 0) {
			throw new KnowageRuntimeException("File is empty");
		}

		// Check if content starts with { or [ (allowing whitespace)
		String preview = new String(content, 0, Math.min(100, content.length), StandardCharsets.UTF_8).trim();
		if (!preview.startsWith("{") && !preview.startsWith("[")) {
			throw new KnowageRuntimeException("File does not appear to be valid JSON (must start with { or [)");
		}

		// Attempt to parse JSON
		try {
			String jsonString = new String(content, StandardCharsets.UTF_8);
			// Try as JSONObject first
			if (preview.startsWith("{")) {
				new JSONObject(jsonString);
			} else {
				// Try as JSONArray
				new JSONArray(jsonString);
			}
		} catch (Exception e) {
			// Fallback to Jackson for better error messages
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.readTree(content);
			} catch (Exception je) {
				throw new KnowageRuntimeException("JSON file is corrupted or malformed: " + je.getMessage());
			}
		}
	}

	/**
	 * Validates CSV file: checks basic structure and attempts reading first rows
	 */
	private static void validateCSV(byte[] content) throws Exception {
		if (content.length == 0) {
			throw new KnowageRuntimeException("File is empty");
		}

		// Check for binary content that doesn't match CSV expectations
		if (startsWithSignature(content, ZIP_SIGNATURE) || startsWithSignature(content, OLE2_SIGNATURE)) {
			throw new KnowageRuntimeException("File appears to be binary (ZIP/XLS) but declared as CSV");
		}

		// Attempt to read first few lines with SuperCSV
		try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
			 CsvMapReader csvReader = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE)) {

			// Try to read header
			String[] header = csvReader.getHeader(true);
			if (header == null || header.length == 0) {
				throw new KnowageRuntimeException("CSV file has no valid header");
			}

			// Try to read at least one data row if available
			csvReader.read(header);

		} catch (Exception e) {
			throw new KnowageRuntimeException("CSV file is corrupted or malformed: " + e.getMessage());
		}
	}

	/**
	 * Validates XLS file: checks OLE2 signature and attempts to open with POI
	 */
	private static void validateXLS(byte[] content) throws Exception {
		if (content.length == 0) {
			throw new KnowageRuntimeException("File is empty");
		}

		// Check for OLE2 signature
		if (!startsWithSignature(content, OLE2_SIGNATURE)) {
			throw new KnowageRuntimeException("File does not have valid XLS signature (expected OLE2 format)");
		}

		// Attempt to open with Apache POI
		try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(content))) {
			// If we can instantiate it, the file is valid
			if (workbook.getNumberOfSheets() == 0) {
				logger.warn("XLS file has no sheets but is structurally valid");
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException("XLS file is corrupted or cannot be opened: " + e.getMessage());
		}
	}

	/**
	 * Validates XLSX file: checks ZIP signature and attempts to open with POI
	 */
	private static void validateXLSX(byte[] content) throws Exception {
		if (content.length == 0) {
			throw new KnowageRuntimeException("File is empty");
		}

		// Check for ZIP signature (XLSX is a ZIP archive)
		if (!startsWithSignature(content, ZIP_SIGNATURE)) {
			throw new KnowageRuntimeException("File does not have valid XLSX signature (expected ZIP format)");
		}

		// Attempt to open with Apache POI
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
			// If we can instantiate it, the file is valid
			if (workbook.getNumberOfSheets() == 0) {
				logger.warn("XLSX file has no sheets but is structurally valid");
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException("XLSX file is corrupted or cannot be opened: " + e.getMessage());
		}
	}

	/**
	 * Checks if byte array starts with a specific signature
	 */
	private static boolean startsWithSignature(byte[] content, byte[] signature) {
		if (content.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (content[i] != signature[i]) {
				return false;
			}
		}
		return true;
	}
}

