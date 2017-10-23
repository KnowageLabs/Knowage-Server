package it.eng.knowage.slimerjs.wrapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SlimerJSConstants {
	static final String DEFAULT_RENDER_SCRIPT = "defaultrender.js";

	static final String ZIP_EXTENSION = ".zip";

	static final String SLIMER_BINARIES_RESOURCEPATH = "it/eng/knowage/slimerjs/wrapper", SLIMER_BINARIES_PACKAGENAME = "slimerjs-0.9.6-%s",
			SLIMER_BINARIES_BIN = "slimerjs%s", SLIMER_BINARIES_WINDOWS = "win32", SLIMER_BINARIES_MAC = "macosx", SLIMER_BINARIES_UNIX = "linux-x86_64";

	static final String XULRUNNER_BINARIES_BIN = "xulrunner", XULRUNNER_DIR = "xulrunner";

	private static final String JVM_UUID = UUID.randomUUID().toString();

	static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir", "/tmp")).resolve("java-slimerjs");

	static final Path TEMP_SCRIPT_DIR = TEMP_DIR.resolve("scripts-" + JVM_UUID), TEMP_SOURCE_DIR = TEMP_DIR.resolve("source-" + JVM_UUID);

	public static final Path TEMP_RENDER_DIR = TEMP_DIR.resolve("output-" + JVM_UUID);

	static final String HEADER_PREFIX = "header-", FOOTER_PREFIX = "footer-", TARGET_PREFIX = "target-", SCRIPT_PREFIX = "script-", SCRIPT_EXTENSION = ".js",
			SOURCE_PREFIX = "source-";

	static final String HEADERFUNCTION_FILE = "headerFunctionFile", FOOTERFUNCTION_FILE = "footerFunctionFile";

	static final String SOURCEPATH_TEMPLATENAME = "sourcePath", RENDERPATH_TEMPLATENAME = "renderPath-";

	static final String CUSTOMHEADERS_TEMPLATENAME = "customeHeaders";

	static final String JS_RENDERING_WAIT_TEMPLATENAME = "jsRenderingWait", JS_EXITING_WAIT_TEMPLATENAME = "jsExitingWait";

	static final String SHUTDOWN_HOOK_THREAD_NAME = "SlimerJSSetupShutDownHook";
}
