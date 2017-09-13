package it.eng.knowage.slimerjs.wrapper;

import static it.eng.knowage.slimerjs.wrapper.CommandLineArgument.wrapCommandLineArgumentName;
import it.eng.knowage.slimerjs.wrapper.beans.CustomHeaders;
import it.eng.knowage.slimerjs.wrapper.beans.OperatingSystem;
import it.eng.knowage.slimerjs.wrapper.beans.RenderOptions;
import it.eng.knowage.slimerjs.wrapper.beans.SlimerJSExecutionResponse;
import it.eng.knowage.slimerjs.wrapper.beans.SlimerJSOptions;
import it.eng.knowage.slimerjs.wrapper.beans.ViewportDimensions;
import it.eng.knowage.slimerjs.wrapper.enums.RenderFormat;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SlimerJS {
	private final static Logger logger = Logger.getLogger(SlimerJS.class);

	private static synchronized String getRenderId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Another way to call SlimerJS#render using the RenderOptions to specify all the common options
	 *
	 * @param url
	 *            to render
	 * @param sheets
	 *            number of sheets to loop over
	 * @param options
	 *            for rendering
	 * @return same as SlimerJS#render
	 * @throws IOException
	 *             if anything goes wrong executing the program
	 * @throws RenderException
	 *             if the render script fails for any reason
	 */
	public static List<InputStream> render(final URL url, final Integer sheets, final RenderOptions options) throws IOException, RenderException {
		return render(options.getOptions(), url, sheets, options.getDimensions(), options.getRenderFormat(), options.getCustomHeaders(),
				options.getJsRenderingWait(), options.getJsExitingWait());
	}

	/**
	 * Another way to call SlimerJS#render using the RenderOptions to specify all the common options
	 *
	 * @param url
	 *            to render
	 * @param options
	 *            for rendering
	 * @return same as SlimerJS#render
	 * @throws IOException
	 *             if anything goes wrong executing the program
	 * @throws RenderException
	 *             if the render script fails for any reason
	 */
	public static List<InputStream> render(final URL url, final RenderOptions options) throws IOException, RenderException {
		return render(options.getOptions(), url, 1, options.getDimensions(), options.getRenderFormat(), options.getCustomHeaders(),
				options.getJsRenderingWait(), options.getJsExitingWait());
	}

	/**
	 * Render the html in the input stream with the following properties using a script included with the wrapper
	 *
	 * @param options
	 *            any slimerjs options to pass to the script
	 * @param url
	 *            to render
	 * @param sheets
	 *            to loop over
	 * @param dimensions
	 *            dimensions of the viewport
	 * @param renderFormat
	 *            the format to render
	 * @param customHeaders
	 *            the headers to be sent
	 * @param jsRenderingWait
	 *            the amount of time to wait for JS before start to render, in milliseconds
	 * @param jsExitingWait
	 *            the amount of time to wait for SlimerJS before exit, in milliseconds
	 * @return the streams of the rendered outputs
	 * @throws IOException
	 *             if any file operations fail
	 * @throws RenderException
	 *             if the render script fails for any reason
	 */
	public static List<InputStream> render(final SlimerJSOptions options, final URL url, final Integer sheets, final ViewportDimensions dimensions,
			final RenderFormat renderFormat, final CustomHeaders customHeaders, final Long jsRenderingWait, final Long jsExitingWait) throws IOException,
			RenderException {
		if (url == null || sheets == null || renderFormat == null || dimensions == null || jsRenderingWait == null || jsExitingWait == null) {
			throw new NullPointerException("All parameters but headers are required");
		}

		if (jsRenderingWait < 0 || jsExitingWait < 0) {
			throw new IllegalArgumentException("Invalid jsRenderingWait or jsExitingWait values provided");
		}

		// The render script
		final InputStream renderScript = SlimerJS.class.getClassLoader().getResourceAsStream(
				SlimerJSConstants.SLIMER_BINARIES_RESOURCEPATH.concat("/".concat(SlimerJSConstants.DEFAULT_RENDER_SCRIPT)));

		// create the parent directories
		Files.createDirectories(SlimerJSConstants.TEMP_SOURCE_DIR);
		Files.createDirectories(SlimerJSConstants.TEMP_RENDER_DIR);

		final String renderId = getRenderId();

		// the output filename template
		Path renderPath = SlimerJSConstants.TEMP_RENDER_DIR.resolve(String.format(SlimerJSConstants.TARGET_PREFIX + "%s", renderId));

		final SlimerJSExecutionResponse slimerJSExecutionResponse = exec(renderScript, options, new CommandLineArgument(renderId), new CommandLineArgument(
				URLEncoder.encode(url.toString(), "UTF-8")), new CommandLineArgument(wrapCommandLineArgumentName(SlimerJSConstants.CUSTOMHEADERS_TEMPLATENAME),
				SlimerJSConstants.CUSTOMHEADERS_TEMPLATENAME, customHeaders), new CommandLineArgument(sheets.toString()),
				new CommandLineArgument(dimensions.getWidth()), new CommandLineArgument(dimensions.getHeight()), new CommandLineArgument(OperatingSystem.get()
						.name()), new CommandLineArgument(wrapCommandLineArgumentName(SlimerJSConstants.RENDERPATH_TEMPLATENAME),
						SlimerJSConstants.RENDERPATH_TEMPLATENAME, renderPath.toFile()), new CommandLineArgument(
						wrapCommandLineArgumentName(SlimerJSConstants.JS_RENDERING_WAIT_TEMPLATENAME), SlimerJSConstants.JS_RENDERING_WAIT_TEMPLATENAME,
						jsRenderingWait), new CommandLineArgument(wrapCommandLineArgumentName(SlimerJSConstants.JS_EXITING_WAIT_TEMPLATENAME),
						SlimerJSConstants.JS_EXITING_WAIT_TEMPLATENAME, jsExitingWait), new CommandLineArgument(SpagoBIUtilities.getHmacKey()));

		final int renderExitCode = slimerJSExecutionResponse.getExitCode();

		if (renderExitCode == 0) {
			List<InputStream> fileStreams = new ArrayList<>(sheets);
			for (int sheetNumber = 0; sheetNumber < sheets; sheetNumber++) {
				renderPath = SlimerJSConstants.TEMP_RENDER_DIR.resolve(String.format(SlimerJSConstants.TARGET_PREFIX + "%s_%s.%s", renderId, sheetNumber,
						renderFormat.name().toLowerCase()));
				fileStreams.add(new DeleteOnCloseFileInputStream(renderPath.toFile()));
			}
			return fileStreams;
		}

		final String error;

		switch (renderExitCode) {
		case 1:
			error = "Failed to set zoom on document body";
			break;
		case 2:
			error = "Failed to render PDF to output";
			break;
		case 3:
			error = "JS execution did not finish correctly";
			break;
		default:
			error = "Render script failed for an unknown reason.";
			break;
		}

		throw new RenderException(error);
	}

	/**
	 * Render the html in the input stream with the following properties using a script included with the wrapper
	 *
	 * @param options
	 *            any slimerjs options to pass to the script
	 * @param url
	 *            to render
	 * @param dimensions
	 *            dimensions of the viewport
	 * @param renderFormat
	 *            the format to render
	 * @param customHeaders
	 *            the headers to be sent
	 * @param jsRenderingWait
	 *            the amount of time to wait for JS before start to render, in milliseconds
	 * @param jsExitingWait
	 *            the amount of time to wait for SlimerJS before exit, in milliseconds
	 * @return the streams of the rendered outputs
	 * @throws IOException
	 *             if any file operations fail
	 * @throws RenderException
	 *             if the render script fails for any reason
	 */
	public static List<InputStream> render(final SlimerJSOptions options, final URL url, final ViewportDimensions dimensions, final RenderFormat renderFormat,
			final CustomHeaders customHeaders, final Long jsRenderingWait, final Long jsExitingWait) throws IOException, RenderException {
		return render(options, url, 1, dimensions, renderFormat, customHeaders, jsRenderingWait, jsExitingWait);
	}

	public static SlimerJSExecutionResponse exec(InputStream script, CommandLineArgument... arguments) throws IOException {
		return exec(script, null, arguments);
	}

	/**
	 * Execute a script with options and a list of arguments
	 *
	 * @param script
	 *            path of script to execute
	 * @param options
	 *            options to execute
	 * @param arguments
	 *            list of arguments
	 * @return the exit code of the script
	 * @throws IOException
	 *             if cmd execution fails
	 */
	public static SlimerJSExecutionResponse exec(InputStream script, SlimerJSOptions options, CommandLineArgument... arguments) throws IOException {
		if (!SlimerJSSetup.isInitialized()) {
			throw new IllegalStateException("Unable to find and execute SlimerJS binaries");
		}

		if (script == null) {
			throw new IllegalArgumentException("Script is a required argument");
		}

		// the path where the script will be copied to
		final String renderId = getRenderId();
		final Path scriptPath = SlimerJSConstants.TEMP_SCRIPT_DIR.resolve(SlimerJSConstants.SCRIPT_PREFIX + renderId + SlimerJSConstants.SCRIPT_EXTENSION);

		// create the parent directory
		Files.createDirectories(SlimerJSConstants.TEMP_SCRIPT_DIR);

		// copy the script to the path
		Files.copy(script, scriptPath);

		// start building the slimerjs binary call
		final CommandLine cmd = getCommandLine();

		final Map<String, Object> args = getArgs(cmd);

		// add options to the slimerjs call
		if (options != null) {
			options.apply(cmd, args);
		}

		// then script
		args.put("_script_path", scriptPath.toFile());
		cmd.addArgument("${_script_path}");

		// then any additional arguments
		if (arguments != null) {
			for (final CommandLineArgument arg : arguments) {
				if (arg != null) {
					arg.apply(cmd, args);
				}
			}
		}

		logger.info(String.format("Running command: %s", cmd.toString()));

		final LoggerOutputStream stdOutLogger = new LoggerOutputStream(logger, Level.ERROR);
		final LoggerOutputStream stdErrLogger = new LoggerOutputStream(logger, Level.ERROR);

		final DefaultExecutor de = new DefaultExecutor();
		de.setStreamHandler(new PumpStreamHandler(stdOutLogger, stdErrLogger));

		int code;
		try {
			code = de.execute(cmd);
		} catch (ExecuteException exe) {
			code = exe.getExitValue();
		}

		// remove the script after running it
		Files.deleteIfExists(scriptPath);

		logger.info("Execution Completed");

		return new SlimerJSExecutionResponse(code, stdOutLogger.getMessageContents(), stdErrLogger.getMessageContents());
	}

	private static CommandLine getCommandLine() {
		final CommandLine cmd;

		if (OperatingSystem.get().equals(OperatingSystem.OS.UNIX)) {
			cmd = new CommandLine("xvfb-run");
		} else {
			cmd = new CommandLine(SlimerJSSetup.getSlimerJsBinary());
		}

		return cmd;
	}

	private static Map<String, Object> getArgs(CommandLine cmd) {
		final Map<String, Object> args = new HashMap<>();
		cmd.setSubstitutionMap(args);

		if (OperatingSystem.get().equals(OperatingSystem.OS.UNIX)) {
			cmd.addArgument("-a");
			args.put("_slimerjs_binary", SlimerJSSetup.getSlimerJsBinary().getAbsolutePath());
			cmd.addArgument("${_slimerjs_binary}");
		}

		return args;
	}

	/**
	 * LoggerOutputStream private nested class override
	 */
	private static class LoggerOutputStream extends LogOutputStream {
		private final Logger logger;
		private final Level level;
		private final StringBuffer messageContents;

		LoggerOutputStream(Logger logger, Level level) {
			super();
			this.logger = logger;
			this.level = level;
			this.messageContents = new StringBuffer();
		}

		@Override
		protected void processLine(String s, int i) {
			logger.log(level, String.format("SlimerJS script logged: %s", s));
			messageContents.append(s).append(System.lineSeparator());
		}

		String getMessageContents() {
			return messageContents.toString();
		}
	}
}