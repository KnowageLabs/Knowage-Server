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
package it.eng.spagobi.commons.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.knowage.commons.zip.SonarZipCommons;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Class di utilita' per zip ed unzip di file
 *
 * @author luigi82
 *
 */
public class ZipUtils {

	private static final Logger LOGGER = LogManager.getLogger(ZipUtils.class);

	/**
	 * Crea l'archivio zip a partire dalla inFolder
	 *
	 * @param inFolder
	 * @param zipFile
	 * @throws IOException
	 */
	public static void zip(File inFolder, File zipFile) throws IOException {

		zip(inFolder, zipFile, inFolder.getName());

	}

	/**
	 * Crea l'archivio zip 'zipFile' a partire dai file 'fileNames'
	 *
	 * @param zipFile
	 * @param fileNames
	 * @throws IOException
	 */
	public static void zip(File zipFile, String... fileNames) throws IOException {
		ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zipFile));
		for (String filename : fileNames) {
			zip(new File(filename), "", z);
		}
		z.close();
	}

	/**
	 * Crea un archivio zip a partire dalla infolder, sostituisce nello zip il nome della infolder con quello di rootZipFolderName
	 *
	 * @param inFolder
	 * @param zipFile
	 * @param rootZipFolderName
	 * @throws IOException
	 */
	public static void zip(File inFolder, File zipFile, String rootZipFolderName) throws IOException {
		String[] dirlist = inFolder.list();

		if (dirlist != null) {
			ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zipFile));

			for (int i = 0; i < dirlist.length; i++) {
				Path path = Paths.get(inFolder.getPath(), dirlist[i]);
				zip(path.toFile(), rootZipFolderName + "/", z);
			}

			z.close();
		}
	}

	private static void zip(File input, String destDir, ZipOutputStream z) throws IOException {

		if (!input.isDirectory()) {

			z.putNextEntry(new ZipEntry((destDir + input.getName())));

			try (FileInputStream inStream = new FileInputStream(input)) {

				byte[] a = new byte[(int) input.length()];

				int did = inStream.read(a);

				if (did != input.length())
					throw new IOException("Impossibile leggere tutto il file " + input.getPath() + " letti solo " + did
							+ " di " + input.length());

				z.write(a, 0, a.length);

				z.closeEntry();

			}

			input = null;

		} else { // recurse

			Path newDestDir = Paths.get(destDir + input.getName());
			Path newInpurPath = Paths.get(input.getPath());

			z.putNextEntry(new ZipEntry(newDestDir.toString()));
			z.closeEntry();

			String[] dirlist = (input.list());

			input = null;

			for (int i = 0; i < dirlist.length; i++) {
				Path input2 = newInpurPath.resolve(dirlist[i]);
				zip(input2.toFile(), newDestDir.toString(), z);

			}
		}
	}

	public static byte[] zipBytes(String filename, byte[] input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		ZipEntry entry = new ZipEntry(filename);
		entry.setSize(input.length);
		zos.putNextEntry(entry);
		zos.write(input);
		zos.closeEntry();
		zos.close();
		return baos.toByteArray();
	}

	public static byte[] zipInputStreams(List<InputStream> inputStreams, String prefix, String sufix)
			throws IOException {
		byte[] buffer = new byte[1024];
		try (ByteArrayOutputStream byteArrayToreturn = new ByteArrayOutputStream();
				ZipOutputStream out = new ZipOutputStream(byteArrayToreturn)) {
			int ind = 1;
			for (InputStream is : inputStreams) {
				ZipEntry z = new ZipEntry((prefix + "" + ind + ".").concat(sufix));
				ind++;
				out.putNextEntry(z);
				int len;
				while ((len = is.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				out.closeEntry();
				is.close();

			}
			return byteArrayToreturn.toByteArray();
		}
	}

	public static void unzip(File zipFile, File outFolder, boolean prependZipFileName) throws IOException {
		unzip(zipFile.getName(), new FileInputStream(zipFile), outFolder, prependZipFileName);

	}

	/**
	 * Estrae il contenuto del file zip in outFolder
	 *
	 * @param zipFile
	 * @param outFolder
	 * @throws IOException
	 */
	public static void unzip(InputStream zipFile, OutputStream f) throws IOException {

		ZipInputStream in = new ZipInputStream(zipFile);
		while (in.getNextEntry() != null) {
			int count;
			byte[] data = new byte[1000];

			// write the files to the disk
			BufferedOutputStream out = new BufferedOutputStream(f, 1000);

			while ((count = in.read(data, 0, 1000)) != -1) {
				out.write(data, 0, count);
			}

			out.flush();
			out.close();

		}
	}

	public static void unzip(File zipFile, OutputStream f) throws IOException {
		try (ZipFile zf = new ZipFile(zipFile)) {

			while (zf.entries().hasMoreElements()) {
				ZipEntry entry = zf.entries().nextElement();
				int count;
				byte[] data = new byte[2048];
				InputStream in = zf.getInputStream(entry);
				// write the files to the disk
				BufferedOutputStream out = new BufferedOutputStream(f, 2048);

				while ((count = in.read(data, 0, 2048)) != -1) {
					out.write(data, 0, count);
				}

				out.flush();
				out.close();

			}
		}
	}

	private static void unzip(String zipFileName, InputStream zipFile, File outFolder, boolean prependZipFileName)
			throws IOException {
		ZipEntry entry;

		try (ZipInputStream in = new ZipInputStream(zipFile)) {

			while ((entry = in.getNextEntry()) != null) {

				if (entry.isDirectory()) {

					Path path = Paths.get(outFolder.getPath(), entry.getName());
					Files.createDirectories(path);

				} else {

					int count;
					byte[] data = new byte[1000];

					Path path = Paths.get(outFolder.getPath(),
							(prependZipFileName ? zipFileName + "_" : "") + entry.getName());

					if (Files.exists(path))
						throw new ZipException("File already exists: " + path);

					// write the files to the disk
					try (OutputStream os = Files.newOutputStream(path);
							BufferedOutputStream out = new BufferedOutputStream(os, 1000)) {

						while ((count = in.read(data, 0, 1000)) != -1) {
							out.write(data, 0, count);
						}

						out.flush();
					}
				}
			}
		}
	}

	/**
	 * Estrae il contenuto del file zip in outFolder
	 *
	 * @param zipFile
	 * @param outFolder
	 * @throws IOException
	 */
	public static void unzip(InputStream zipFile, File outFolder) throws IOException {
		unzip("", zipFile, outFolder, false);
	}

	/* NOT USED
	 * public static void unzipFile(String filePath) {

		ZipEntry zEntry = null;
		try (FileInputStream fis = new FileInputStream(filePath);
				ZipInputStream zipIs = new ZipInputStream(new BufferedInputStream(fis))) {
			while ((zEntry = zipIs.getNextEntry()) != null) {
				SonarZipCommons sonarZipCommons = new SonarZipCommons();
				
				if(sonarZipCommons.doThresholdCheck(filePath)) {
					unzipEntry(filePath, zEntry, zipIs);
				} else {
					LOGGER.error("Error while unzip file. Invalid archive file");
					throw new SpagoBIRuntimeException("Error while unzip file. Invalid archive file");
				}
			}
		} catch (IOException e) {
			LOGGER.warn("Non-fatal error unzipping {}", filePath, e);
		}
	}
	*/

	private static void unzipEntry(String filePath, ZipEntry zEntry, ZipInputStream zipIs) {
		byte[] tmp = new byte[4 * 1024];
		String opFilePath = "C:/" + zEntry.getName();
		try (FileOutputStream fos = new FileOutputStream(opFilePath)) {
			int size = 0;
			while ((size = zipIs.read(tmp)) != -1) {
				fos.write(tmp, 0, size);
			}
			fos.flush();
		} catch (Exception e) {
			LOGGER.warn("Non-fatal error unzipping {}, entry {}", filePath, zEntry, e);
		}
	}


}
