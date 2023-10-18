/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.utilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.PathTraversalChecker;

/**
 * This class has been created to provide SpagoBI Access Utils, in order to customize operations with clients.
 *
 * @author zoppello
 */
public class SpagoBIAccessUtils {

	private static final Logger LOGGER = Logger.getLogger(SpagoBIAccessUtils.class);

	/**
	 * Unzip.
	 *
	 * @param repositoryZip the repository_zip
	 * @param newDirectory  the new directory
	 *
	 * @throws ZipException the zip exception
	 * @throws IOException  Signals that an I/O exception has occurred.
	 */
	public void unzip(File repositoryZip, File newDirectory) throws ZipException, IOException {
		try (ZipFile zipFile = new ZipFile(repositoryZip)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry entry = null;
			String name = null;
			String path = null;
			File file = null;
			while (entries.hasMoreElements()) {
				entry = entries.nextElement();
				name = entry.getName();
				path = newDirectory.getPath() + File.separator + name;
				file = new File(path);
				PathTraversalChecker.checkDescendentOfDirectory(file, newDirectory);

				// if file already exists, deletes it
				if (file.exists() && file.isFile())
					deleteDirectory(file);

				if (!entry.isDirectory()) {
					file = file.getParentFile();
					file.mkdirs();

					String fileName = newDirectory.getPath() + File.separator + entry.getName();

					try (FileOutputStream fileout = new FileOutputStream(fileName);
							BufferedOutputStream bufout = new BufferedOutputStream(fileout);
							InputStream in = zipFile.getInputStream(entry)) {
						copyInputStream(in, bufout);
						bufout.flush();
					}
				} else {
					file.mkdirs();
				}
			}
		}
	}

	private void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[8 * 1024];
		int len = 0;
		while ((len = in.read(b)) != -1) {
			out.write(b, 0, len);
		}
	}

	/**
	 * Delete directory.
	 *
	 * @param pathdest the pathdest
	 *
	 * @return true, if successful
	 */
	public boolean deleteDirectory(String pathdest) {
		File directory = new File(pathdest);
		return deleteDirectory(directory);
	}

	/**
	 * Delete directory.
	 *
	 * @param directory the directory
	 *
	 * @return true, if successful
	 */
	public boolean deleteDirectory(File directory) {
		try {
			if (directory.isDirectory()) {
				File[] files = directory.listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.isFile()) {
						boolean deletion = file.delete();
						// if (!deletion)return false;
					} else
						deleteDirectory(file.getAbsolutePath());
				}
			}
			boolean deletion = directory.delete();
			// if (!deletion) return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Delete file.
	 *
	 * @param fileName the file name
	 * @param path     the path
	 *
	 * @return true, if successful
	 */
	public boolean deleteFile(String fileName, String path) {
		try {
			File toDelete = new File(path + File.separatorChar + fileName);
			if (toDelete.exists())
				toDelete.delete();
		} catch (Exception exc) {
			return false;
		}
		return true;
	}

	/**
	 * Given an <code>InputStream</code> as input, gets the correspondent bytes array.
	 *
	 * @param is The input straeam
	 *
	 * @return An array of bytes obtained from the input stream.
	 */
	public byte[] getByteArrayFromInputStream(InputStream is) {

		try {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos);

			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					bos.write(b);
				else
					bos.write(b, 0, c);
			}
			bos.flush();
			byte[] ret = baos.toByteArray();
			bos.close();
			return ret;
		} catch (IOException ioe) {
			LOGGER.error("Exception: " + ioe);
			ioe.printStackTrace();
			return null;
		}

	}

	/**
	 * Given an <code>InputStream</code> as input flushs the content into an OutputStream and then close the input and output stream.
	 *
	 * @param is           The input stream
	 * @param os           The output stream
	 * @param closeStreams the close streams
	 */
	public void flushFromInputStreamToOutputStream(InputStream is, OutputStream os, boolean closeStreams) {
		try {
			int c = 0;
			byte[] b = new byte[1024];
			while ((c = is.read(b)) != -1) {
				if (c == 1024)
					os.write(b);
				else
					os.write(b, 0, c);
			}
			os.flush();
		} catch (IOException ioe) {
			LOGGER.error("Exception: " + ioe);
			ioe.printStackTrace();
		} finally {
			if (closeStreams) {
				try {
					if (os != null)
						os.close();
					if (is != null)
						is.close();
				} catch (IOException e) {
					LOGGER.error("Error closing streams: " + e);
					e.printStackTrace();
				}

			}
		}
	}

}
