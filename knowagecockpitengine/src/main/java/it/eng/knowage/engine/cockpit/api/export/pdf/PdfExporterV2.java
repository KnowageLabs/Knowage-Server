package it.eng.knowage.engine.cockpit.api.export.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.export.pdf.ExportDetails;
import it.eng.knowage.export.pdf.PDFCreator;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;

public class PdfExporterV2 extends AbstractPdfExporter {

	private static final String SCRIPT_NAME = "cockpit-export.js";

	private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";

	private static final String JVM_UUID = UUID.randomUUID().toString();

	static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "/tmp")).resolve("knowage-pdf-exporter-2");

	public static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output-" + JVM_UUID);

	private static class SheetImageFileVisitor extends SimpleFileVisitor<Path> {

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
			logger.debug("Visit file " + filePath);
			if (imagePathMatcher.matches(filePath)) {
				logger.debug("File " + filePath + " matches the filter!");
				try {
					InputStream currImageInputStream = Files.newInputStream(filePath, StandardOpenOption.DELETE_ON_CLOSE);
					imagesInputStreams.add(currImageInputStream);
				} catch (IOException e) {
					throw new IllegalStateException("Cannot open input stream on file " + filePath, e);
				}
			}

			return FileVisitResult.CONTINUE;
		}
	};

	private static final Logger logger = Logger.getLogger(PdfExporterV2.class);

	final ServletContext servletContext;

	public PdfExporterV2(int documentId, String userId, String requestUrl, RenderOptions renderOptions, String pageOrientation, boolean pdfFrontPage,
			boolean pdfBackPage, ServletContext servletContext) {
		super(documentId, userId, requestUrl, renderOptions, pageOrientation, pdfFrontPage, pdfBackPage);
		this.servletContext = servletContext;
	}

	@Override
	public byte[] getBinaryData() throws IOException, InterruptedException, EMFUserError {

		final Path outputDir = Files.createTempDirectory("knowage-pdf-exporter-2");
		final Path nodeOutput = Files.createTempFile("knowage-pdf-exporter-2", "nodejs-output");
		final Path outputFile = outputDir.resolve("output.pdf");

		Files.createDirectories(outputDir);

		BIObject document = DAOFactory.getBIObjectDAO().loadBIObjectById(documentId);
		int sheetCount = getSheetCount(document);
		int sheetWidth = getSheetWidth(document);
		int sheetHeight = getSheetHeight(document);

		String encodedUserId = Base64.encodeBase64String(userId.getBytes("UTF-8"));

		URI url = UriBuilder.fromUri(requestUrl).replaceQueryParam("outputType_description", "HTML").replaceQueryParam("outputType", "HTML")
				.replaceQueryParam("export", null).build();

		String cockpitExportScriptPath = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);

		Path exportScriptFullPath = Paths.get(cockpitExportScriptPath, SCRIPT_NAME);

		if (!Files.isRegularFile(exportScriptFullPath)) {
			String msg = String.format("Cannot find export script at \"%s\": did you set the correct value for %s configuration?", exportScriptFullPath,
					CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
			IllegalStateException ex = new IllegalStateException(msg);
			logger.error(msg, ex);
			throw ex;
		}

		ProcessBuilder processBuilder = new ProcessBuilder("node", exportScriptFullPath.toString(), url.toString(), encodedUserId, outputDir.toString(),
				Integer.toString(sheetCount), Integer.toString(sheetWidth), Integer.toString(sheetHeight));

		processBuilder.redirectOutput(nodeOutput.toFile());

		Process exec = processBuilder.start();

		Executors.defaultThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
				File file = nodeOutput.toFile();
				try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {

					String line = null;
					while ((line = br.readLine()) != null) {
						logger.warn(line);
					}

				} catch (Exception e) {
					logger.error("Error reading NodeJS output", e);
				}
			}
		}).start();

		exec.waitFor();

		final List<InputStream> imagesInputStreams = new ArrayList<InputStream>();

		try {
			Files.walkFileTree(outputDir, new SheetImageFileVisitor(imagesInputStreams));

			if (imagesInputStreams.isEmpty()) {
				throw new IllegalStateException("No files in " + outputDir + ": see main log file of the AS");
			}

			PDFCreator.createPDF(imagesInputStreams, outputFile, false, false);
			ExportDetails details = new ExportDetails(getFrontpageDetails(pdfFrontPage, document), null);
			PDFCreator.addInformation(outputFile, details);

			try (InputStream is = Files.newInputStream(outputFile, StandardOpenOption.DELETE_ON_CLOSE)) {
				return IOUtils.toByteArray(is);
			}
		} finally {
			for (InputStream currImageinputStream : imagesInputStreams) {
				IOUtils.closeQuietly(currImageinputStream);
			}
			try {
				Files.delete(nodeOutput);
			} catch (Exception e) {
				// Yes, it's mute!
			}
			try {
				Files.delete(outputDir);
			} catch (Exception e) {
				// Yes, it's mute!
			}
		}
	}

}
