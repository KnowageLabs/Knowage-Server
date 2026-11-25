package it.eng.spagobi.utilities.fonts;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BirtFonts {

	public static final int MAX_BYTES = 10 * 1024 * 1024;
	private final Path directory;
	private final Path fonts;

	public BirtFonts(Path resourceRoot) {
		directory = resourceRoot.toAbsolutePath().normalize().resolve("birt");
		fonts = directory.resolve("fonts");
	}

	public Path getConfigurationPath() {
		return directory.resolve("fontsConfig.xml");
	}

	public Map<String, Object> list() throws IOException {
		Map<String, Object> result = new LinkedHashMap<>();
		List<Map<String, Object>> items = new ArrayList<>();
		for (Path file : fontFiles()) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("name", file.getFileName().toString());
			item.put("path", file.toString());
			item.put("size", Files.size(file));
			items.add(item);
		}
		result.put("items", items);
		result.put("directory", fonts.toString());
		result.put("configurationPath", getConfigurationPath().toString());
		result.put("restartRequired", true);
		return result;
	}

	public synchronized void save(byte[] content, String name) throws IOException {
		validateName(name);
		if (content == null || content.length == 0 || content.length > MAX_BYTES) {
			throw new IllegalArgumentException("Fonts must be non-empty and no larger than 10 MB");
		}
		try {
			Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(content));
		} catch (FontFormatException | IOException e) {
			throw new IllegalArgumentException("Invalid TrueType or OpenType font", e);
		}
		synchronized (BirtFonts.class) {
			prepareDirectory();
			try (FileChannel channel = lockChannel(); FileLock lock = channel.lock()) {
				Path target = fonts.resolve(name);
				// Reject case-only duplicates consistently on Windows and Linux.
				for (Path file : fontFiles()) {
					if (file.getFileName().toString().equalsIgnoreCase(name)) {
						throw new FileAlreadyExistsException(name);
					}
				}
				Path uploaded = Files.createTempFile(directory, "uploaded-font-", ".tmp");
				try {
					Files.write(uploaded, content);
					Files.move(uploaded, target);
					try {
						writeConfiguration();
					} catch (IOException e) {
						Files.deleteIfExists(target);
						throw e;
					}
				} finally {
					Files.deleteIfExists(uploaded);
				}
			}
		}
	}

	public synchronized boolean delete(String name) throws IOException {
		validateName(name);
		synchronized (BirtFonts.class) {
			prepareDirectory();
			try (FileChannel channel = lockChannel(); FileLock lock = channel.lock()) {
				Path target = fonts.resolve(name);
				if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) return false;
				Path backup = Files.createTempFile(directory, "deleted-font-", ".tmp");
				try {
					Files.move(target, backup, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					Files.deleteIfExists(backup);
					throw e;
				}
				try {
					writeConfiguration();
				} catch (IOException e) {
					Files.move(backup, target, StandardCopyOption.REPLACE_EXISTING);
					throw e;
				}
				Files.deleteIfExists(backup);
				return true;
			}
		}
	}

	private FileChannel lockChannel() throws IOException {
		Path lock = directory.resolve(".fonts.lock");
		if (Files.isSymbolicLink(lock)) throw new IOException("Font lock must not be a symbolic link");
		return FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}

	private void prepareDirectory() throws IOException {
		checkDirectories();
		Files.createDirectories(fonts);
	}

	private void checkDirectories() throws IOException {
		if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(fonts)) {
			throw new IOException("BIRT font directories must not be symbolic links");
		}
	}

	private List<Path> fontFiles() throws IOException {
		checkDirectories();
		if (!Files.exists(fonts)) return Collections.emptyList();
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fonts)) {
			for (Path file : stream) {
				if (isFontName(file.getFileName().toString()) && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
					files.add(file);
				}
			}
		}
		Collections.sort(files);
		return files;
	}

	private void writeConfiguration() throws IOException {
		Path configuration = getConfigurationPath();
		if (Files.isSymbolicLink(configuration)) throw new IOException("Font configuration must not be a symbolic link");
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<font>\n    <font-paths>\n");
		for (Path file : fontFiles()) {
			xml.append("        <path path=\"").append(xmlAttribute(file.toString())).append("\" />\n");
		}
		xml.append("    </font-paths>\n</font>\n");
		Path temporary = Files.createTempFile(directory, "fontsConfig-", ".tmp");
		try {
			Files.write(temporary, xml.toString().getBytes(StandardCharsets.UTF_8));
			// An unsupported atomic move fails the operation instead of exposing partial XML to BIRT.
			Files.move(temporary, configuration, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static String xmlAttribute(String value) {
		return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\r", "&#13;").replace("\n", "&#10;").replace("\t", "&#9;");
	}

	private static boolean isFontName(String name) {
		if (name == null || !name.matches("[A-Za-z0-9][A-Za-z0-9._ -]{0,179}\\.(?i:ttf|otf)")) return false;
		String stem = name.substring(0, name.indexOf('.')).toUpperCase(Locale.ROOT);
		return !stem.matches("CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]");
	}

	private static void validateName(String name) {
		if (!isFontName(name)) throw new IllegalArgumentException("Use a simple TTF or OTF filename without paths or special characters");
	}
}
