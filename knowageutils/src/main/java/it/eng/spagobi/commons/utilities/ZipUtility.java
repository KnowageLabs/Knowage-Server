package it.eng.spagobi.commons.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipUtility {

	private List<String> fileList;
	private String sourcePath;

	static private Logger logger = Logger.getLogger(ZipUtility.class);

	public ZipUtility(String sourcePath) {
		fileList = new ArrayList<String>();
		this.sourcePath = sourcePath;
	}

	public static File generateZipFile(String sourcePath, String zipFileName) {
		ZipUtility zipUtility = new ZipUtility(sourcePath);
		zipUtility.generateFileList(new File(sourcePath));
		File zipFile = new File(sourcePath, zipFileName);
		if (zipFile.exists()) {
			return zipFile;
		} else {
			zipUtility.zipIt(sourcePath, zipFile.getAbsolutePath());
		}
		return zipFile;
	}

	public void zipIt(String sourceFolder, String zipFile) {
		byte[] buffer = new byte[1024];
		String source = new File(sourceFolder).getName();
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			logger.info("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList) {
				logger.info("File Added : " + file);
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(sourceFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();
			logger.info("Folder successfully compressed");

		} catch (IOException ex) {
			logger.error("Error zipping file [" + zipFile + "]", ex);

		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				logger.error("Error closing ZipOutputStream file", e);
			}
		}
	}

	public void generateFileList(File node) {
		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));
		}
		// traverse directory recursively
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(this.sourcePath.length() + 1, file.length());
	}
}
