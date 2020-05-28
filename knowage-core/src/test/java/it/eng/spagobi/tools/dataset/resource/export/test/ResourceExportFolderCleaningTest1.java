package it.eng.spagobi.tools.dataset.resource.export.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import it.eng.spagobi.tools.dataset.resource.export.ResourceExportFolderCleaningManager;
import it.eng.spagobi.tools.dataset.resource.export.utils.ResourceExportFolderCleaningManagerUtils;

public class ResourceExportFolderCleaningTest1 extends ResourceExportFolderCleaningTestMain {

	private static final String FOLDER_PATH = "C:\\temp\\TestMain1\\";
	private ResourceExportFolderCleaningManager resourceExportFolderCleaningManager = new ResourceExportFolderCleaningManager();

	@Before
	public void prepare() throws Exception {
		ResourceExportFolderCleaningManagerUtils cleaningManagerUtils = new ResourceExportFolderCleaningManagerUtils();
		cleaningManagerUtils.createFilesUntilFolderSize(FOLDER_PATH, MAX_FOLDER_SIZE);

		resourceExportFolderCleaningManager.executeCleaning(FOLDER_PATH, MAX_FOLDER_SIZE, CLEANING_PERCENTAGE);

	}

	@Test
	public void test() throws Exception {
		Long actualFolderSize = resourceExportFolderCleaningManager.folderSize(new File(FOLDER_PATH));

		assertEquals("Expected FolderSize", true, actualFolderSize < MAX_FOLDER_SIZE * (1 - CLEANING_PERCENTAGE));
	}

}
