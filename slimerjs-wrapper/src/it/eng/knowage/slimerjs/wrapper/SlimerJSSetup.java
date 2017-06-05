package it.eng.knowage.slimerjs.wrapper;

import it.eng.knowage.slimerjs.wrapper.beans.OperatingSystem;
import it.eng.spagobi.commons.utilities.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;

class SlimerJSSetup {

	private static Logger logger = Logger.getLogger(SlimerJSSetup.class);

	// this will store a reference to the executable slimerjs binary after we unzip the resource
	private static final File SLIMER_JS_BINARY = initializeBinaries();

	// get a reference to the executable binary and store it in SLIMER_JS_BINARY
	private static File initializeBinaries() throws IllegalStateException {
		final String resourcePath = getZipPath(SlimerJSConstants.SLIMER_BINARIES_RESOURCEPATH.concat("/".concat(SlimerJSConstants.SLIMER_BINARIES_PACKAGENAME)));

		logger.info("Initializing SlimerJS with resource path: " + resourcePath);

		// As long as we have a resource path, and that the binaries have not already been initialized, initialize them
		if (null != resourcePath && null == SLIMER_JS_BINARY) {
			initializeShutDownHook();
			return unzipSlimerJSbin(SlimerJSConstants.TEMP_DIR, resourcePath);
		} else {
			throw new IllegalStateException("Instantiation mechanism was unable to determine platform type for SlimerJS extraction.");
		}
	}

	static boolean isInitialized() {
		return SLIMER_JS_BINARY != null && SLIMER_JS_BINARY.exists() && SLIMER_JS_BINARY.canExecute();
	}

	static File getSlimerJsBinary() {
		return SLIMER_JS_BINARY;
	}

	/**
	 * Get the name of the bin we expect in the unzipped file
	 *
	 * @return the name of the bin in the unzipped file
	 */
	private static String getSlimerJSBinName() {
		OperatingSystem.OS os = OperatingSystem.get();
		if (os == null) {
			return null;
		}

		String ext = "";
		if (OperatingSystem.OS.WINDOWS.equals(os)) {
			ext = ".bat";
		}

		return String.format(SlimerJSConstants.SLIMER_BINARIES_BIN, ext);
	}

	/**
	 * Get the name of the bin we expect in the unzipped file
	 *
	 * @return the name of the bin in the unzipped file
	 */
	private static String getXulRunnerBinName() {
		OperatingSystem.OS os = OperatingSystem.get();
		if (os == null) {
			return null;
		}

		String ext = "";
		if (OperatingSystem.OS.WINDOWS.equals(os)) {
			ext = ".exe";
		}

		return String.format(SlimerJSConstants.XULRUNNER_BINARIES_BIN, ext);
	}

	private static String getXulRunnerDirName() {
		return SlimerJSConstants.XULRUNNER_DIR;
	}

	/**
	 * Unzips the zipped resource to the destination
	 *
	 * @param destination
	 *            for zip contents
	 * @param resourceName
	 *            name of the java resource
	 */
	private static File unzipSlimerJSbin(final Path destination, final String resourceName) throws IllegalStateException {
		final Path absoluteResource = Paths.get(destination.toString().concat(
				File.separator.concat(getZipPath(SlimerJSConstants.SLIMER_BINARIES_PACKAGENAME).replace(SlimerJSConstants.ZIP_EXTENSION, "")
						.concat(File.separator).concat(getSlimerJSBinName()))));
		final Path xulRunnerResource = Paths.get(destination.toString().concat(
				File.separator.concat(getZipPath(SlimerJSConstants.SLIMER_BINARIES_PACKAGENAME).replace(SlimerJSConstants.ZIP_EXTENSION, "")
						.concat(File.separator).concat(getXulRunnerDirName()).concat(File.separator).concat(getXulRunnerBinName()))));

		logger.info("Verifying existence of SlimerJS executable at: " + absoluteResource.toString());

		if (!Files.exists(absoluteResource)) {
			if (Files.exists(destination)) {
				try {
					Files.walkFileTree(destination, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					throw new IllegalStateException("Failed to remove " + destination + " folder", e);
				}
			}
			File binary = null;

			logger.info("Getting resource: " + resourceName);
			try (InputStream fileStream = SlimerJSSetup.class.getClassLoader().getResourceAsStream(resourceName)) {

				logger.info("Unzipping SlimerJS to resource path: " + destination);
				Files.createDirectories(destination);

				String slimerJSbin = getSlimerJSBinName();
				if (slimerJSbin == null) {
					throw new IllegalStateException("Unable to get SlimerJS bin name.");
				}

				logger.info("Unzipping file stream: " + fileStream);
				ZipUtils.unzip(fileStream, destination.toFile());

				logger.info("Checking file executability: " + absoluteResource);
				binary = absoluteResource.toFile();
				if (!binary.canExecute()) {
					if (!binary.setExecutable(true)) {
						throw new IllegalStateException("SlimerJSSetup failed to make SlimerJS binary executable");
					}
				}

				logger.info("Checking file executability: " + xulRunnerResource);
				File xulrunner = xulRunnerResource.toFile();
				if (!xulrunner.canExecute()) {
					if (!xulrunner.setExecutable(true)) {
						throw new IllegalStateException("SlimerJSSetup failed to make XulRunner binary executable");
					}
				}

			} catch (IOException e) {
				throw new IllegalStateException("Failed to read zip file from resources", e);
			}

			return binary;
		} else {
			logger.info("SlimerJS exists under resource path: " + destination);
			return absoluteResource.toFile();
		}
	}

	/**
	 * Gets the name of the resource for the zip based on the OS
	 *
	 * @param resourceName
	 *            the name of the zip resource in the resources directory
	 * @return the name of the appropriate zipped slimerjs
	 */
	private static String getZipPath(final String resourceName) {
		final OperatingSystem.OS os = OperatingSystem.get();
		if (os == null) {
			return null;
		}

		String osString = "";

		switch (os) {
		case WINDOWS:
			osString = SlimerJSConstants.SLIMER_BINARIES_WINDOWS;
			break;

		case MAC:
			osString = SlimerJSConstants.SLIMER_BINARIES_MAC;
			break;

		case UNIX:
			osString = SlimerJSConstants.SLIMER_BINARIES_UNIX;
			break;
		}

		return String.format(resourceName.concat(SlimerJSConstants.ZIP_EXTENSION), osString);
	}

	/**
	 * Shutdown hook in charge of cleaning of JVM specific folders during JVM shutdown. This hook needs to be added during initialization of the class.
	 */
	private static void initializeShutDownHook() {
		final Runtime runtime = Runtime.getRuntime();

		final Thread shutdownThread = new Thread(SlimerJSConstants.SHUTDOWN_HOOK_THREAD_NAME) {
			@Override
			public void run() {
				try {
					Files.deleteIfExists(SlimerJSConstants.TEMP_SCRIPT_DIR);
					Files.deleteIfExists(SlimerJSConstants.TEMP_RENDER_DIR);

				} catch (Exception e) {
					logger.warn("SlimerJSSetup was unable to clean up temporary directories under: " + SlimerJSConstants.TEMP_DIR + ". Caused by: "
							+ e.getMessage());
				}
			}
		};

		runtime.addShutdownHook(shutdownThread);
	}

}
