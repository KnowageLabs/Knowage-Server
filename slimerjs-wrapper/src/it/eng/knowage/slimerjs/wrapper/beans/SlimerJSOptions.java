package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.knowage.slimerjs.wrapper.CommandLineArgument;
import it.eng.spagobi.utilities.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;

/**
 * SlimerJS options passed to the binary before any scripts or arguments
 * <p>
 * See <a href="https://docs.slimerjs.org/0.9/configuration.html">here</a>
 */
public class SlimerJSOptions {
	public static SlimerJSOptions DEFAULT = new SlimerJSOptions(false, false, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null);

	public enum YesNo {
		yes, no
	}

	public enum SSLProtocol {
		sslv3, sslv2, tlsv1, any
	}

	private final boolean help;
	private final boolean version;
	private final String debug;
	private final File cookiesFile;
	private final Boolean diskCache;
	private final String diskCachePath;
	private final Boolean ignoreSslErrors;
	private final Boolean loadImages;
	private final File localStoragePath;
	private final Long localStorageQuota;
	private final Boolean localToRemoteUrlAccess;
	private final Long maxDiskCacheSize;
	private final String outputEncoding;

	private final Short remoteDebuggerPort;
	private final YesNo remoteDebuggerAutorun;

	private final String proxy;
	private final String proxyType;
	private final String proxyAuth;

	private final String scriptEncoding;

	private final SSLProtocol sslProtocol;
	private final String sslCertificatesPath;
	private final Boolean webSecurity;

	private final Boolean webdriver;
	private final String webdriverSeleniumGridHub;

	// Private constructor
	private SlimerJSOptions(boolean help, boolean version, String debug, File cookiesFile, Boolean diskCache, String diskCachePath, Boolean ignoreSslErrors,
			Boolean loadImages, File localStoragePath, Long localStorageQuota, Boolean localToRemoteUrlAccess, Long maxDiskCacheSize, String outputEncoding,
			Short remoteDebuggerPort, YesNo remoteDebuggerAutorun, String proxy, String proxyType, String proxyAuth, String scriptEncoding,
			SSLProtocol sslProtocol, String sslCertificatesPath, Boolean webSecurity, Boolean webdriver, String webdriverSeleniumGridHub) {
		this.help = help;
		this.version = version;
		this.debug = debug;
		this.cookiesFile = cookiesFile;
		this.diskCache = diskCache;
		this.diskCachePath = diskCachePath;
		this.ignoreSslErrors = ignoreSslErrors;
		this.loadImages = loadImages;
		this.localStoragePath = localStoragePath;
		this.localStorageQuota = localStorageQuota;
		this.localToRemoteUrlAccess = localToRemoteUrlAccess;
		this.maxDiskCacheSize = maxDiskCacheSize;
		this.outputEncoding = outputEncoding;
		this.remoteDebuggerPort = remoteDebuggerPort;
		this.remoteDebuggerAutorun = remoteDebuggerAutorun;
		this.proxy = proxy;
		this.proxyType = proxyType;
		this.proxyAuth = proxyAuth;
		this.scriptEncoding = scriptEncoding;
		this.sslProtocol = sslProtocol;
		this.sslCertificatesPath = sslCertificatesPath;
		this.webSecurity = webSecurity;
		this.webdriver = webdriver;
		this.webdriverSeleniumGridHub = webdriverSeleniumGridHub;
	}

	private SlimerJSOptions(boolean help, boolean version, File cookiesFile, Boolean diskCache, String diskCachePath, Boolean ignoreSslErrors,
			Boolean loadImages, File localStoragePath, Long localStorageQuota, Boolean localToRemoteUrlAccess, Long maxDiskCacheSize, String outputEncoding,
			Short remoteDebuggerPort, YesNo remoteDebuggerAutorun, String proxy, String proxyType, String proxyAuth, String scriptEncoding,
			SSLProtocol sslProtocol, String sslCertificatesPath, Boolean webSecurity, Boolean webdriver, String webdriverSeleniumGridHub) {
		this(false, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public boolean isHelp() {
		return help;
	}

	public boolean isVersion() {
		return version;
	}

	public String getDebug() {
		return debug;
	}

	public File getCookiesFile() {
		return cookiesFile;
	}

	public Boolean getDiskCache() {
		return diskCache;
	}

	public String getDiskCachePath() {
		return diskCachePath;
	}

	public Boolean getIgnoreSslErrors() {
		return ignoreSslErrors;
	}

	public Boolean getLoadImages() {
		return loadImages;
	}

	public Long getLocalStorageQuota() {
		return localStorageQuota;
	}

	public Boolean getLocalToRemoteUrlAccess() {
		return localToRemoteUrlAccess;
	}

	public Long getMaxDiskCacheSize() {
		return maxDiskCacheSize;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public Short getRemoteDebuggerPort() {
		return remoteDebuggerPort;
	}

	public YesNo getRemoteDebuggerAutorun() {
		return remoteDebuggerAutorun;
	}

	public String getProxy() {
		return proxy;
	}

	public String getProxyType() {
		return proxyType;
	}

	public String getProxyAuth() {
		return proxyAuth;
	}

	public String getScriptEncoding() {
		return scriptEncoding;
	}

	public SSLProtocol getSslProtocol() {
		return sslProtocol;
	}

	public String getSslCertificatesPath() {
		return sslCertificatesPath;
	}

	public Boolean getWebSecurity() {
		return webSecurity;
	}

	public Boolean getWebdriver() {
		return webdriver;
	}

	public String getWebdriverSeleniumGridHub() {
		return webdriverSeleniumGridHub;
	}

	public File getLocalStoragePath() {
		return localStoragePath;
	}

	public SlimerJSOptions withHelp(boolean help) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withVersion(boolean version) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withDebug(String debug) {
		return new SlimerJSOptions(help, version, debug, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath,
				localStorageQuota, localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType,
				proxyAuth, scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withCookiesFile(File cookiesFile) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withDiskCache(Boolean diskCache) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withDiskCachePath(String diskCachePath) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withIgnoreSslErrors(Boolean ignoreSslErrors) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withLoadImages(Boolean loadImages) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withLocalStoragePath(File localStoragePath) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withLocalStorageQuota(Long localStorageQuota) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withLocalToRemoteUrlAccess(Boolean localToRemoteUrlAccess) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withMaxDiskCacheSize(Long maxDiskCacheSize) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withOutputEncoding(String outputEncoding) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withRemoteDebuggerPort(Short remoteDebuggerPort) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withRemoteDebuggerAutorun(YesNo remoteDebuggerAutorun) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withProxy(String proxy) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withProxyType(String proxyType) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withProxyAuth(String proxyAuth) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withScriptEncoding(String scriptEncoding) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withSslProtocol(SSLProtocol sslProtocol) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withSslCertificatesPath(String sslCertificatesPath) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withWebSecurity(Boolean webSecurity) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withWebdriver(Boolean webdriver) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	public SlimerJSOptions withWebdriverSeleniumGridHub(String webdriverSeleniumGridHub) {
		return new SlimerJSOptions(help, version, cookiesFile, diskCache, diskCachePath, ignoreSslErrors, loadImages, localStoragePath, localStorageQuota,
				localToRemoteUrlAccess, maxDiskCacheSize, outputEncoding, remoteDebuggerPort, remoteDebuggerAutorun, proxy, proxyType, proxyAuth,
				scriptEncoding, sslProtocol, sslCertificatesPath, webSecurity, webdriver, webdriverSeleniumGridHub);
	}

	/**
	 * For a command line execution, add the appropriate arguments
	 *
	 * @param cmd
	 *            command line execution to modify
	 * @param args
	 *            argument map
	 */
	public void apply(final CommandLine cmd, final Map<String, Object> args) {
		final List<CommandLineArgument> arguments = new LinkedList<>();

		if (isHelp()) {
			arguments.add(new CommandLineArgument("--help"));
		}

		if (isVersion()) {
			arguments.add(new CommandLineArgument("--version"));
		}

		if (getDebug() != null) {
			arguments.add(new CommandLineArgument("--debug=${debug}", "debug", getDebug()));
		}

		if (getCookiesFile() != null) {
			arguments.add(new CommandLineArgument("--cookies-file=${cookies-file}", "cookies-file", getCookiesFile()));
		}

		if (getDiskCache() != null) {
			arguments.add(new CommandLineArgument("--disk-cache=${disk-cache}", "disk-cache", getDiskCache()));
		}

		if (getDiskCachePath() != null) {
			arguments.add(new CommandLineArgument("--disk-cache-path=${disk-cache-path}", "disk-cache-path", getDiskCachePath()));
		}

		if (getIgnoreSslErrors() != null) {
			arguments.add(new CommandLineArgument("--ignore-ssl-errors=${ignore-ssl-errors}", "ignore-ssl-errors", getIgnoreSslErrors()));
		}

		if (getLoadImages() != null) {
			arguments.add(new CommandLineArgument("--load-images=${load-images}", "load-images", getLoadImages()));
		}

		if (getLocalStoragePath() != null) {
			arguments.add(new CommandLineArgument("--local-storage-path=${local-storage-path}", "local-storage-path", getLocalStoragePath()));
		}

		if (getLocalStorageQuota() != null) {
			arguments.add(new CommandLineArgument("--local-storage-quota=${local-storage-quota}", "local-storage-quota", getLocalStorageQuota()));
		}

		if (getLocalToRemoteUrlAccess() != null) {
			arguments.add(new CommandLineArgument("--local-to-remote-url-access=${local-remote-url-access}", "local-remote-url-access",
					getLocalToRemoteUrlAccess()));
		}

		if (getMaxDiskCacheSize() != null) {
			arguments.add(new CommandLineArgument("--max-disk-cache-size=${max-disk-cache-size}", "max-disk-cache-size", getMaxDiskCacheSize()));
		}
		if (getOutputEncoding() != null) {
			arguments.add(new CommandLineArgument("--output-encoding=${output-encoding}", "output-encoding", getOutputEncoding()));
		}
		if (getRemoteDebuggerPort() != null) {
			arguments.add(new CommandLineArgument("--remote-debugger-port=${remote-debugger-port}", "remote-debugger-port", getRemoteDebuggerPort()));
		}
		if (getRemoteDebuggerAutorun() != null) {
			arguments.add(new CommandLineArgument("--remote-debugger-autorun=${remote-debugger-autorun}", "remote-debugger-autorun", getRemoteDebuggerAutorun()
					.name()));
		}

		if (getProxy() != null) {
			arguments.add(new CommandLineArgument("--proxy=${proxy}", "proxy", getProxy()));
		}
		if (getProxyType() != null) {
			arguments.add(new CommandLineArgument("--proxy-type=${proxy-type}", "proxy-type", getProxyType()));
		}

		if (getProxyAuth() != null) {
			arguments.add(new CommandLineArgument("--proxy-auth=${proxy-auth}", "proxy-auth", getProxyAuth()));
		}

		if (getScriptEncoding() != null) {
			arguments.add(new CommandLineArgument("--script-encoding=${script-encoding}", "script-encoding", getScriptEncoding()));
		}
		if (getSslProtocol() != null) {
			arguments.add(new CommandLineArgument("--ssl-protocol=${ssl-protocol}", "ssl-protocol", getSslProtocol().name()));
		}

		if (getSslCertificatesPath() != null) {
			arguments.add(new CommandLineArgument("--ssl-certificates-path=${ssl-cert-path}", "ssl-cert-path", getSslCertificatesPath()));
		}
		if (getWebSecurity() != null) {
			arguments.add(new CommandLineArgument("--web-security=${web-security}", "web-security", getWebSecurity()));
		}

		if (getWebdriver() != null) {
			arguments.add(new CommandLineArgument("--webdriver"));
			if (getWebdriverSeleniumGridHub() != null) {
				arguments.add(new CommandLineArgument("--webdriver-selenium-grid-hub=${webdriver-selenium-grid-hub}", "webdriver-selenium-grid-hub",
						getWebdriverSeleniumGridHub()));
			}
		}

		for (final CommandLineArgument cla : arguments) {
			cla.apply(cmd, args);
		}
	}

	@Override
	public String toString() {
		final List<String> args = new LinkedList<>();

		if (isHelp()) {
			args.add("--help");
		}
		if (isVersion()) {
			args.add("--version");
		}

		if (getDebug() != null) {
			args.add("--debug=" + getDebug());
		}

		if (getCookiesFile() != null) {
			args.add("--cookies-file=" + getCookiesFile());
		}

		if (getDiskCache()) {
			args.add("--disk-cache=" + getDiskCache());
		}

		if (getIgnoreSslErrors()) {
			args.add("--ignore-ssl-errors=" + getIgnoreSslErrors());
		}

		if (getLoadImages()) {
			args.add("--load-images=" + getLoadImages());
		}

		if (getLocalStoragePath() != null) {
			args.add("--local-storage-path=" + getLocalStoragePath());
		}

		if (getLocalToRemoteUrlAccess() != null) {
			args.add("--local-to-remote-url-access=" + getLocalToRemoteUrlAccess());
		}
		if (getMaxDiskCacheSize() != null) {
			args.add("--max-disk-cache-size=" + getMaxDiskCacheSize());
		}
		if (getOutputEncoding() != null) {
			args.add("--output-encoding=" + getOutputEncoding());
		}
		if (getRemoteDebuggerPort() != null) {
			args.add("--remote-debugger-port=" + getRemoteDebuggerPort());
		}
		if (getRemoteDebuggerAutorun() != null) {
			args.add("--remote-debugger-autorun=" + getRemoteDebuggerAutorun().name());
		}

		if (getProxy() != null) {
			args.add("--proxy=" + getProxy());
		}
		if (getProxyType() != null) {
			args.add("--proxy-type=" + getProxyType());
		}

		if (getProxyAuth() != null) {
			args.add("--proxy-auth=" + getProxyAuth());
		}

		if (getScriptEncoding() != null) {
			args.add("--script-encoding=" + getScriptEncoding());
		}
		if (getSslProtocol() != null) {
			args.add("--ssl-protocol=" + getSslProtocol().name());
		}

		if (getSslCertificatesPath() != null) {
			args.add("--ssl-certificates-path=" + getSslCertificatesPath());
		}
		if (getWebSecurity() != null) {
			args.add("--web-security=" + getWebSecurity());
		}

		if (getWebdriver() != null) {
			args.add("--webdriver");
			if (getWebdriverSeleniumGridHub() != null) {
				args.add("--webdriver-selenium-grid-hub=" + getWebdriverSeleniumGridHub());
			}
		}

		return !args.isEmpty() ? StringUtils.join(args, " ") : "";
	}

}
