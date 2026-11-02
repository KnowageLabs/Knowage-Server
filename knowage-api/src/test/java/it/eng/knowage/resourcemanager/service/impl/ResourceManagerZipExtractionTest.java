package it.eng.knowage.resourcemanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.knowage.boot.error.KnowageRuntimeException;

class ResourceManagerZipExtractionTest {

	@TempDir
	Path tempDirectory;

	private final ResourceManagerAPIImpl resourceManagerAPI = new ResourceManagerAPIImpl();

	@Test
	void extractsNestedFiles() throws IOException {
		Path destination = Files.createDirectory(tempDirectory.resolve("extracted"));
		Path archive = createArchive("nested/", "nested/file.txt");

		extract(archive, destination);

		assertEquals("test", new String(Files.readAllBytes(destination.resolve("nested/file.txt")), StandardCharsets.UTF_8));
	}

	@ParameterizedTest
	@ValueSource(strings = { "../extracted-sibling/new.txt", "../extracted-sibling/nested/" })
	void rejectsTraversalBeforeCreatingParentDirectories(String entryName) throws IOException {
		Path destination = Files.createDirectory(tempDirectory.resolve("extracted"));

		assertRejected(createArchive(entryName), destination);

		assertFalse(Files.exists(tempDirectory.resolve("extracted-sibling")));
	}

	@Test
	void rejectsTraversalWithoutOverwritingExistingFile() throws IOException {
		Path destination = Files.createDirectory(tempDirectory.resolve("extracted"));
		Path outside = tempDirectory.resolve("outside.txt");
		Files.write(outside, "original".getBytes(StandardCharsets.UTF_8));

		assertRejected(createArchive("../outside.txt"), destination);

		assertEquals("original", new String(Files.readAllBytes(outside), StandardCharsets.UTF_8));
	}

	@Test
	void rejectsAbsoluteEntryPaths() throws IOException {
		Path destination = Files.createDirectory(tempDirectory.resolve("extracted"));
		Path outside = tempDirectory.resolve("outside.txt").toAbsolutePath();

		assertRejected(createArchive(outside.toString()), destination);

		assertFalse(Files.exists(outside));
	}

	@Test
	void rejectsTraversalThroughExistingSymbolicLink() throws IOException {
		Path destination = Files.createDirectory(tempDirectory.resolve("extracted"));
		Path outside = Files.createDirectory(tempDirectory.resolve("outside"));
		try {
			Files.createSymbolicLink(destination.resolve("nested"), outside);
		} catch (IOException | UnsupportedOperationException | SecurityException e) {
			assumeTrue(false, "Symbolic links are not available: " + e.getMessage());
		}

		assertRejected(createArchive("nested/file.txt"), destination);

		assertFalse(Files.exists(outside.resolve("file.txt")));
	}

	private Path createArchive(String... entryNames) throws IOException {
		Path archive = tempDirectory.resolve("archive.zip");
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
			for (String entryName : entryNames) {
				zip.putNextEntry(new ZipEntry(entryName));
				if (!entryName.endsWith("/")) {
					zip.write("test".getBytes(StandardCharsets.UTF_8));
				}
				zip.closeEntry();
			}
		}
		return archive;
	}

	private void assertRejected(Path archive, Path destination) {
		KnowageRuntimeException exception = assertThrows(KnowageRuntimeException.class, () -> extract(archive, destination));
		assertTrue(exception.getCause() instanceof IOException);
		assertEquals("ZIP entry is outside the extraction directory", exception.getCause().getMessage());
	}

	private void extract(Path archive, Path destination) {
		ReflectionTestUtils.invokeMethod(resourceManagerAPI, "extractFolder", archive.toString(), destination.toString());
	}
}
