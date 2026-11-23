package it.eng.spagobi.tools.dataset.resource.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import it.eng.spagobi.api.v2.export.Entry;
import it.eng.spagobi.api.v2.export.ExportMetadata;
import it.eng.spagobi.api.v2.export.ExportPathBuilder;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class UtilitiesTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final UserProfile profile = new UserProfile("reader", "tenant");

	@Before
	public void useTemporaryExportDirectory() {
		String resourcePath = temporaryFolder.getRoot().getAbsolutePath();
		new MockUp<SpagoBIUtilities>() {
			@Mock
			public String getResourcePath() {
				return resourcePath;
			}
		};
	}

	@Test
	public void listsActualExportFileNamesWithoutChangingDownloadedFiltering() throws Exception {
		UUID csv = createExport("DS_CHART", "Sales 2026 2025.csv", false);
		UUID excel = createExport("DS_CHART", "Sales 2026 2025.xlsx", true);

		Map<String, Entry> entries = listById();

		assertEquals("Sales 2026 2025.csv", entries.get(csv.toString()).getFilename());
		assertEquals("Sales 2026 2025.xlsx", entries.get(excel.toString()).getFilename());
		assertTrue(entries.get(excel.toString()).isAlreadyDownloaded());

		List<Entry> unread = Utilities.getInstance().getAllExportedFiles(profile, false);
		assertEquals(1, unread.size());
		assertEquals(csv.toString(), unread.get(0).getId());
	}

	@Test
	public void fallsBackToDatasetNameForLegacyMetadataWithoutAFileName() throws Exception {
		UUID missing = createExport("Legacy dataset", null, false);
		UUID blank = createExport("Another dataset", "  ", false);

		Map<String, Entry> entries = listById();

		assertEquals("Legacy dataset", entries.get(missing.toString()).getFilename());
		assertEquals("Another dataset", entries.get(blank.toString()).getFilename());
	}

	private Map<String, Entry> listById() throws Exception {
		return Utilities.getInstance().getAllExportedFiles(profile, true).stream()
				.collect(Collectors.toMap(Entry::getId, entry -> entry));
	}

	private UUID createExport(String datasetName, String fileName, boolean downloaded) throws Exception {
		String resourcePath = temporaryFolder.getRoot().getAbsolutePath();
		ExportPathBuilder paths = ExportPathBuilder.getInstance();
		UUID id = UUID.randomUUID();
		Files.createDirectories(paths.getPerJobExportPath(resourcePath, profile, id));
		Files.createFile(paths.getPerJobIdDataFile(resourcePath, profile, id));
		ExportMetadata metadata = new ExportMetadata();
		metadata.setId(id);
		metadata.setDataSetName(datasetName);
		metadata.setFileName(fileName);
		metadata.setStartDate(new Date(0));
		ExportMetadata.writeToJsonFile(metadata, paths.getPerJobIdMetadataFile(resourcePath, profile, id));
		if (downloaded) {
			Files.createFile(paths.getPerJobIdDownloadedPlaceholderFile(resourcePath, profile, id));
		}
		return id;
	}
}
