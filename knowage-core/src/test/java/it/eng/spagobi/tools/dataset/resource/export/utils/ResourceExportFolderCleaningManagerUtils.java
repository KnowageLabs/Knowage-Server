package it.eng.spagobi.tools.dataset.resource.export.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class ResourceExportFolderCleaningManagerUtils {

	public static final long DEFAULT_FILE_SIZE = 10485760L;

	public boolean createSingleFile(String path, Long length) {
		boolean wellCreated = false;
		RandomAccessFile rafile;
		try {
			rafile = new RandomAccessFile(path, "rw");
			rafile.setLength(length);
			wellCreated = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wellCreated;
	}

	public void createFilesUntilFolderSize(String folderPath, Long folderSize, Long fileSize) {
		if (!new File(folderPath).exists()) {
			new File(folderPath).mkdirs();
		} else {
			for (File file : new File(folderPath).listFiles()) {
				file.delete();
			}
			new File(folderPath).delete();
			new File(folderPath).mkdirs();
		}

		Map<String, Long> filesMap = new HashMap<String, Long>();

		Long currentFolderSize = 0L;
		int i = 0;
		do {

			filesMap.put(folderPath + "file_" + i++, fileSize);

			currentFolderSize += fileSize;

		} while (currentFolderSize <= folderSize);

		for (String filePath : filesMap.keySet()) {
			createSingleFile(filePath, filesMap.get(filePath));
		}

	}

	public void createFilesUntilFolderSize(String folderPath, Long folderSize) {
		createFilesUntilFolderSize(folderPath, folderSize, DEFAULT_FILE_SIZE);
	}

	public long folderSize(File folder) {
		long length = 0;
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				length += file.length();
			} else {
				length += folderSize(file);
			}
		}
		return length;
	}
}
