package it.eng.knowage.engine.cockpit.api.export.pdf.nodejs;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.export.pdf.FrontpageDetails;
import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * @author albnale
 * @since 2020/11/04
 *
 */
public abstract class AbstractNodeJSBasedExporter {

	private static final Logger LOGGER = Logger.getLogger(AbstractNodeJSBasedExporter.class);

	static class SheetImageFileVisitor extends SimpleFileVisitor<Path> {

		/**
		 * Path matcher for sheets' screenshots.
		 *
		 * It's not static just because i think it's not thread-safe.
		 */
		private final PathMatcher imagePathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.png");

		/**
		 * Reference to the array of input streams.
		 */
		private final List<InputStream> imagesInputStreams;

		public SheetImageFileVisitor(List<InputStream> imagesInputStreams) {
			super();
			this.imagesInputStreams = imagesInputStreams;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
			LOGGER.debug("Visit file " + filePath);
			if (imagePathMatcher.matches(filePath)) {
				LOGGER.debug("File " + filePath + " matches the filter!");
				try {
					InputStream currImageInputStream = Files.newInputStream(filePath, StandardOpenOption.DELETE_ON_CLOSE);
					imagesInputStreams.add(currImageInputStream);
				} catch (IOException e) {
					throw new IllegalStateException("Cannot open input stream on file " + filePath, e);
				}
			}

			return FileVisitResult.CONTINUE;
		}
	}

	protected static final String SCRIPT_NAME = "cockpit-export.js";

	protected static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";

	protected abstract byte[] handleFile(final Path outputDir, BIObject document, final List<InputStream> imagesInputStreams) throws IOException;

	protected final int documentId;

	protected final String userId;
	protected final String requestUrl;
	protected final String pageOrientation;
	protected final boolean pdfFrontPage;
	protected final boolean pdfBackPage;
	protected final RenderOptions renderOptions;

	protected AbstractNodeJSBasedExporter(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pdfPageOrientation,
			boolean pdfFrontPage, boolean pdfBackPage) {
		super();
		this.documentId = documentId;
		this.userId = userId;
		this.requestUrl = requestUrl;
		this.renderOptions = renderOptions;
		this.pageOrientation = pdfPageOrientation;
		this.pdfFrontPage = pdfFrontPage;
		this.pdfBackPage = pdfBackPage;
	}

	protected int getSheetCount(BIObject document) {
		try {
			int numOfPages = 0;
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				numOfPages = 1;
				return numOfPages;
			case "knowagecockpitengine":
			case "knowagedashboardengine":
				ObjTemplate objTemplate = document.getActiveTemplate();
				if (objTemplate == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				numOfPages = sheets.length();
				return numOfPages;

			default:
				return numOfPages;
			}

		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected int getSheetHeight(BIObject document) {
		try {
			int sheetHeight = Integer.parseInt(renderOptions.getDimensions().getHeight());
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				break;
			case "knowagecockpitengine":

				ObjTemplate objTemplate = document.getActiveTemplate();
				if (objTemplate == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				int sheetLabelHeigth = (sheets.length() > 0) ? 48 : 0;
				for (int sheetIndex = 0; sheetIndex < sheets.length(); sheetIndex++) {
					JSONObject sheet = (JSONObject) sheets.get(sheetIndex);
					if (sheet.has("widgets")) {
						JSONArray widgets = sheet.getJSONArray("widgets");
						for (int widgetIndex = 0; widgetIndex < widgets.length(); widgetIndex++) {
							JSONObject widget = (JSONObject) widgets.get(widgetIndex);
							int row = widget.getInt("row");
							int sizeY = widget.getInt("sizeY");
							int widgetHeight = (row + sizeY) * 30 + sheetLabelHeigth; // scaling by cockpitModule_gridsterOptions.rowHeight
							sheetHeight = Math.max(sheetHeight, widgetHeight);
						}
					}
				}
				break;
			case "knowagedashboardengine":
				ObjTemplate objTemplateNew = document.getActiveTemplate();
				if (objTemplateNew == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateStringNew = new String(objTemplateNew.getContent());
				JSONObject templateNew = new JSONObject(templateStringNew);
				JSONArray sheetsNew = templateNew.getJSONArray("sheets");
				int sheetLabelHeigthNew = (sheetsNew.length() > 0) ? 48 : 0;
				for (int sheetIndex = 0; sheetIndex < sheetsNew.length(); sheetIndex++) {
					JSONObject sheet = (JSONObject) sheetsNew.get(sheetIndex);
					if (sheet.has("widgets")) {
						if (sheet.get("widgets") instanceof JSONArray) {
							JSONArray widgets = sheet.getJSONArray("widgets");
							for (int widgetIndex = 0; widgetIndex < widgets.length(); widgetIndex++) {
								JSONObject widget = (JSONObject) widgets.get(widgetIndex);
								int row = widget.getInt("x");
								int sizeY = widget.getInt("y");
								int widgetHeight = (row + sizeY) * 30 + sheetLabelHeigthNew; // scaling by cockpitModule_gridsterOptions.rowWidth
								sheetHeight = Math.max(sheetHeight, widgetHeight);
							}
						} else {
							JSONObject widget = sheet.getJSONObject("widgets");
							JSONArray widgetLG = widget.getJSONArray("lg");
							for (int widgetIndex = 0; widgetIndex < widgetLG.length(); widgetIndex++) {
								JSONObject widgetToAnalyze = (JSONObject) widgetLG.get(widgetIndex);
								int row = widgetToAnalyze.getInt("x");
								int sizeY = widgetToAnalyze.getInt("y");
								int widgetHeight = (row + sizeY) * 30 + sheetLabelHeigthNew; // scaling by cockpitModule_gridsterOptions.rowWidth
								sheetHeight = Math.max(sheetHeight, widgetHeight);
							}
						}
					}
				}
				break;
			}
			return sheetHeight;
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected int getSheetWidth(BIObject document) {
		try {
			int sheetWidth = Integer.parseInt(renderOptions.getDimensions().getWidth());
			switch (document.getEngineLabel()) {
			case "knowagechartengine":
				break;
			case "knowagecockpitengine":
				ObjTemplate objTemplate = document.getActiveTemplate();
				if (objTemplate == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateString = new String(objTemplate.getContent());
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				int sheetLabelHeigth = (sheets.length() > 0) ? 48 : 0;
				for (int sheetIndex = 0; sheetIndex < sheets.length(); sheetIndex++) {
					JSONObject sheet = (JSONObject) sheets.get(sheetIndex);
					if (sheet.has("widgets")) {
						JSONArray widgets = sheet.getJSONArray("widgets");
						for (int widgetIndex = 0; widgetIndex < widgets.length(); widgetIndex++) {
							JSONObject widget = (JSONObject) widgets.get(widgetIndex);
							int row = widget.getInt("row");
							int sizeY = widget.getInt("sizeY");
							int widgetWidth = (row + sizeY) * 30 + sheetLabelHeigth; // scaling by cockpitModule_gridsterOptions.rowWidth
							sheetWidth = Math.max(sheetWidth, widgetWidth);
						}
					}
				}
				break;
			case "knowagedashboardengine":
				ObjTemplate objTemplateNew = document.getActiveTemplate();
				if (objTemplateNew == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				String templateStringNew = new String(objTemplateNew.getContent());
				JSONObject templateNew = new JSONObject(templateStringNew);
				JSONArray sheetsNew = templateNew.getJSONArray("sheets");
				int sheetLabelHeigthNew = (sheetsNew.length() > 0) ? 48 : 0;
				for (int sheetIndex = 0; sheetIndex < sheetsNew.length(); sheetIndex++) {
					JSONObject sheet = (JSONObject) sheetsNew.get(sheetIndex);
					if (sheet.has("widgets")) {
						if (sheet.get("widgets") instanceof JSONArray) {
							JSONArray widgets = sheet.getJSONArray("widgets");
							for (int widgetIndex = 0; widgetIndex < widgets.length(); widgetIndex++) {
								JSONObject widget = (JSONObject) widgets.get(widgetIndex);
								int row = widget.getInt("x");
								int sizeY = widget.getInt("y");
								int widgetWidth = (row + sizeY) * 30 + sheetLabelHeigthNew; // scaling by cockpitModule_gridsterOptions.rowWidth
								sheetWidth = Math.max(sheetWidth, widgetWidth);
							}
						} else {
							JSONObject widget = sheet.getJSONObject("widgets");
							JSONArray widgetLG = widget.getJSONArray("lg");
							for (int widgetIndex = 0; widgetIndex < widgetLG.length(); widgetIndex++) {
								JSONObject widgetToAnalyze = (JSONObject) widgetLG.get(widgetIndex);
								int row = widgetToAnalyze.getInt("x");
								int sizeY = widgetToAnalyze.getInt("y");
								int widgetWidth = (row + sizeY) * 30 + sheetLabelHeigthNew; // scaling by cockpitModule_gridsterOptions.rowWidth
								sheetWidth = Math.max(sheetWidth, widgetWidth);
							}
						}
					}
				}
				break;
			}
			return sheetWidth;
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}

	protected double getDeviceScaleFactor(BIObject document) {
		return Double.valueOf(renderOptions.getDimensions().getDeviceScaleFactor());
	}

	protected boolean getIsMultiSheet(BIObject document) {
		return Boolean.parseBoolean(renderOptions.getDimensions().getIsMultiSheet());
	}

	protected FrontpageDetails getFrontpageDetails(boolean includeFrontPage, BIObject document) {
		FrontpageDetails toReturn = null;

		if (includeFrontPage) {
			String name = document.getName();
			String description = document.getDescription();
			if (name == null || description == null) {
				throw new SpagoBIRuntimeException(
						"Unable to get name [" + name + "] or description [" + description + "] for document with id [" + documentId + "]");
			}
			toReturn = new FrontpageDetails(name, description, new Date());
		}
		return toReturn;
	}

	public byte[] getBinaryData() throws IOException, InterruptedException, EMFUserError {

		final Path outputDir = Files.createTempDirectory("knowage-exporter-2");

		Files.createDirectories(outputDir);
		LOGGER.info("Files will be placed in: " + outputDir);

		BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
		int sheetCount = getSheetCount(document);
		int sheetWidth = getSheetWidth(document);
		int sheetHeight = getSheetHeight(document);
		double deviceScaleFactor = getDeviceScaleFactor(document);
		boolean isMultiSheet = getIsMultiSheet(document);
		String encodedUserId = Base64.encodeBase64String(userId.getBytes(UTF_8));
		LOGGER.debug("Encoded User Id: " + encodedUserId);

		URI url = UriBuilder.fromUri(requestUrl).replaceQueryParam("outputType_description", "HTML").replaceQueryParam("outputType", "HTML")
				.replaceQueryParam("export", (Object) null).build();
		LOGGER.debug("URL: " + url);

		// Script
		String cockpitExportScriptPath = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
		Path exportScriptFullPath = Paths.get(cockpitExportScriptPath, SCRIPT_NAME);
		LOGGER.info("Script Path: " + cockpitExportScriptPath);

		if (!Files.isRegularFile(exportScriptFullPath)) {
			String msg = String.format("Cannot find export script at \"%s\": did you set the correct value for %s configuration?", exportScriptFullPath,
					CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
			IllegalStateException ex = new IllegalStateException(msg);
			LOGGER.error(msg, ex);
			throw ex;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("node", exportScriptFullPath.toString(), url.toString(), encodedUserId, outputDir.toString(),
				Integer.toString(sheetCount), Integer.toString(sheetWidth), Integer.toString(sheetHeight), Double.toString(deviceScaleFactor),
				Boolean.toString(isMultiSheet));

		setWorkingDirectory(cockpitExportScriptPath, processBuilder);

		LOGGER.info("Node complete command line: " + processBuilder.command());

		LOGGER.info("Starting export script");
		Process exec = processBuilder.start();

		logOutputToCoreLog(exec);

		LOGGER.info("Waiting...");
		exec.waitFor();
		LOGGER.warn("Exit value: " + exec.exitValue());

		final List<InputStream> imagesInputStreams = new ArrayList<>();

		try {
			Files.walkFileTree(outputDir, new SheetImageFileVisitor(imagesInputStreams));

			if (imagesInputStreams.isEmpty()) {
				throw new IllegalStateException("No files in " + outputDir + ": see main log file of the AS");
			}

			// replace images in doc
			return handleFile(outputDir, document, imagesInputStreams);

		} finally {
			for (InputStream currImageinputStream : imagesInputStreams) {
				IOUtils.closeQuietly(currImageinputStream);
			}
			try {
				Files.delete(outputDir);
			} catch (Exception e) {
				// Yes, it's mute!
			}
		}
	}

	private void logOutputToCoreLog(Process exec) throws IOException {
		InputStreamReader isr = new InputStreamReader(exec.getInputStream());
		BufferedReader b = new BufferedReader(isr);
		String line = null;
		LOGGER.warn("Process output");
		while ((line = b.readLine()) != null) {
			LOGGER.warn(line);
		}
	}

	private void setWorkingDirectory(String cockpitExportScriptPath, ProcessBuilder processBuilder) {
		// Required by puppeteer v19
		processBuilder.directory(new File(cockpitExportScriptPath));
	}

}
