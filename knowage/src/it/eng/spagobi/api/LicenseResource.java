package it.eng.spagobi.api;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.license.Check;
import it.eng.spagobi.tools.license.LicenseManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.util.FileUtils;

@Path("/1.0/license")
@ManageAuthorization
public class LicenseResource extends AbstractSpagoBIResource {

	private static Logger logger = Logger.getLogger(LicenseResource.class);

	public static final String ERROR_MESSAGE_FILE_NAME = "License name file not correct. Please contact the administrator";
	public static final String ERROR_MESSAGE_LICENSE_NOT_FOUND = "Error. The license file was not found";
	public static final String ERROR_MESSAGE_LICENSE_NOT_DELETED = "Error. The license file was not deleted";
	public static final String ERROR_MESSAGE_LICENSE_EXISTS = "Error. The license already exists";

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.LICENSE_MANAGEMENT })
	public Response getLicenses(@QueryParam("onlyValid") boolean onlyValid) {

		if (!Check.selfIntegrityChecking()) {
			throw new SpagoBIRuntimeException(Check.ERROR_MESSAGE);
		}

		JSONArray response = new JSONArray();

		try {
			Map<String, License> licenses = LicenseManager.getLicenses(onlyValid);
			Locale locale = buildLocaleFromSession();
			Map<String, String[]> errors = LicenseManager.checkCustomFeaturesValidity(licenses.values(), locale);

			for (String productName : licenses.keySet()) {
				License license = licenses.get(productName);
				JSONObject product = new JSONObject();
				product.put("product", productName);
				product.put("status", license.getValidationStatus().toString());
				String statusExt = MessageBuilderFactory.getMessageBuilder().getMessage(license.getValidationStatus().toString(), locale);
				product.put("status_ext", statusExt);
				product.put("expiration_date", license.getLicenseText().getLicenseExpireDate() == null ? "" : license.getLicenseText().getLicenseExpireDate()
						.toString());
				product.put("other_info_type", errors.get(productName)[0] == null ? "" : errors.get(productName)[0]);
				product.put("other_info", errors.get(productName)[1] == null ? "" : errors.get(productName)[1]);
				response.put(product);
			}
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Error while recovering licenses detail", e);
		}

		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/hardware-detail")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.LICENSE_MANAGEMENT })
	public Response getHardwareDetail() {

		if (!Check.selfIntegrityChecking()) {
			throw new SpagoBIRuntimeException(Check.ERROR_MESSAGE);
		}

		JSONObject response = new JSONObject();

		try {
			response.put("hostname", InetAddress.getLocalHost().getHostName());
			response.put("hardware-id", LicenseManager.getHardwareID());
			response.put("processors", Runtime.getRuntime().availableProcessors());
		} catch (Exception e) {
			logger.error("Error downloading license file");
			throw new SpagoBIRuntimeException("Error downloading license file ", e);
		}

		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/download/{product}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@UserConstraint(functionalities = { SpagoBIConstants.LICENSE_MANAGEMENT })
	public Response downloadLicense(@PathParam("product") String product) {

		if (!Check.selfIntegrityChecking()) {
			throw new SpagoBIRuntimeException(Check.ERROR_MESSAGE);
		}

		File file = null;
		Map<String, License> licenses = LicenseManager.getLicenses(true);
		if (licenses.containsKey(product)) {
			String licenseResourcePath = LicenseManager.getLicensePath();
			String licensePath = licenseResourcePath + File.separator + product + LicenseManager.FILE_LICENSE_PATTERN;
			file = new File(licensePath);
		}

		if (file == null || !file.exists()) {
			logger.error(ERROR_MESSAGE_LICENSE_NOT_FOUND);
			throw new SpagoBIRuntimeException(ERROR_MESSAGE_LICENSE_NOT_FOUND);
		}

		ResponseBuilder response = Response.ok(file);
		String fileName = file.getName();
		response.header("Content-Disposition", "attachment; fileName=" + fileName + "; fileType=text; extensionFile=lic");

		return response.build();
	}

	@GET
	@Path("/delete/{product}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@UserConstraint(functionalities = { SpagoBIConstants.LICENSE_MANAGEMENT })
	public Response deleteLicense(@PathParam("product") String product) {

		boolean deleted = false;

		if (!Check.selfIntegrityChecking()) {
			throw new SpagoBIRuntimeException(Check.ERROR_MESSAGE);
		}

		File file = null;
		Map<String, License> licenses = LicenseManager.getLicenses(true);
		if (licenses.containsKey(product)) {
			String licenseResourcePath = LicenseManager.getLicensePath();
			String licensePath = licenseResourcePath + File.separator + product + LicenseManager.FILE_LICENSE_PATTERN;
			file = new File(licensePath);
			logger.debug("Delete file");
			deleted = file.delete();
			if (deleted == true) {
				logger.debug("File deleted");
			} else {
				logger.error(ERROR_MESSAGE_LICENSE_NOT_DELETED);
				throw new SpagoBIRuntimeException(ERROR_MESSAGE_LICENSE_NOT_DELETED);
			}
		}

		JSONObject response = new JSONObject();
		try {
			response = new JSONObject();
			response.put("deleted", deleted);
			response.put("product", product);
		} catch (JSONException e) {
			logger.error("Error converting in JSON uploaded license file");
			throw new SpagoBIRuntimeException("Error converting in JSON uploaded license file", e);
		}

		return Response.status(200).entity(response.toString()).build();

		// ResponseBuilder response = Response.ok(deleted);
		// return response.build();
	}

	@POST
	@Path("/upload")
	@UserConstraint(functionalities = { SpagoBIConstants.LICENSE_MANAGEMENT })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	public Response uploadFile(@MultipartForm MultipartFormDataInput input) {
		logger.debug("IN");
		byte[] bytes = null;

		if (!Check.selfIntegrityChecking()) {
			throw new SpagoBIRuntimeException(Check.ERROR_MESSAGE);
		}

		JSONObject licenseToSend = null;
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		for (String key : uploadForm.keySet()) {

			List<InputPart> inputParts = uploadForm.get(key);

			for (InputPart inputPart : inputParts) {

				try {

					MultivaluedMap<String, String> header = inputPart.getHeaders();
					String fileName = getFileName(header);
					if (fileName != null) {
						if (!fileName.endsWith(LicenseManager.FILE_LICENSE_PATTERN)) {
							logger.error(ERROR_MESSAGE_FILE_NAME);
							throw new SpagoBIRuntimeException(ERROR_MESSAGE_FILE_NAME);
						}
						// convert the uploaded file to input stream
						InputStream inputStream = inputPart.getBody(InputStream.class, null);

						bytes = IOUtils.toByteArray(inputStream);

						String saveDirectoryPath = LicenseManager.getLicensePath();
						File saveDirectory = new File(saveDirectoryPath);
						if (!(saveDirectory.exists() && saveDirectory.isDirectory())) {
							saveDirectory.mkdirs();
						}
						String tempFile = saveDirectoryPath + File.separator + fileName;
						File tempFileToSave = new File(tempFile);
						if (tempFileToSave.exists()) {
							logger.error(ERROR_MESSAGE_LICENSE_EXISTS);
							throw new SpagoBIRuntimeException(ERROR_MESSAGE_LICENSE_EXISTS);
						}
						tempFileToSave.createNewFile();
						DataOutputStream os = new DataOutputStream(new FileOutputStream(tempFileToSave));
						os.write(bytes);
						os.close();

						// Create new uploaded license
						logger.debug("Reading license file...");
						String activatedLicense = FileUtils.readFile(tempFileToSave.getAbsolutePath());
						logger.debug("Validating license...");
						License licenseOnDisk = LicenseValidator.validateWithCustomHardwareID(activatedLicense, LicenseManager.getPublicKey(), null, null,
								null, LicenseManager.getHardwareID(), null, null);
						logger.debug("Insering all the licenses in the list...");
						String productId = licenseOnDisk.getLicenseText().getLicenseValidProductID();

						Locale locale = buildLocaleFromSession();

						licenseToSend = new JSONObject();
						licenseToSend.put("product", productId);
						licenseToSend.put("status", licenseOnDisk.getValidationStatus().toString());

						String statusExt = MessageBuilderFactory.getMessageBuilder().getMessage(licenseOnDisk.getValidationStatus().toString(), locale);
						licenseToSend.put("status_ext", statusExt);
						licenseToSend.put("expiration_date", licenseOnDisk.getLicenseText().getLicenseExpireDate() == null ? "" : licenseOnDisk
								.getLicenseText().getLicenseExpireDate().toString());
						String[] error = LicenseManager.checkCustomFeatureValidity(licenseOnDisk, locale);
						licenseToSend.put("other_info_type", error[0] == null ? "" : error[0]);
						licenseToSend.put("other_info", error[1] == null ? "" : error[1]);
					}

				} catch (IOException e) {
					logger.error("Error uploading license file");
					throw new SpagoBIRuntimeException("Error uploading license file", e);
				} catch (JSONException e) {
					logger.error("Error converting in JSON uploaded license file");
					throw new SpagoBIRuntimeException("Error converting in JSON uploaded license file", e);
				}

			}

		}
		logger.debug("OUT");
		// return Response.status(200).build();
		return Response.status(200).entity(licenseToSend.toString()).build();

	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().contains("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return null;
	}

}
