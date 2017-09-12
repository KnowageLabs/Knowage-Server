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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Class di utilita' per zip ed unzip di file
 *
 * @author luigi82
 *
 */
public class ZipUtils {

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
				zip(new File(inFolder.getPath() + "/" + dirlist[i]), rootZipFolderName + "/", z);
			}

			z.close();
		}
	}

	private static void zip(File input, String destDir, ZipOutputStream z) throws IOException {

		if (!input.isDirectory()) {

			z.putNextEntry(new ZipEntry((destDir + input.getName())));

			FileInputStream inStream = new FileInputStream(input);

			byte[] a = new byte[(int) input.length()];

			int did = inStream.read(a);

			if (did != input.length())
				throw new IOException("Impossibile leggere tutto il file " + input.getPath() + " letti solo " + did + " di " + input.length());

			z.write(a, 0, a.length);

			z.closeEntry();

			inStream.close();

			input = null;

		} else { // recurse

			String newDestDir = destDir + input.getName() + "/";
			String newInpurPath = input.getPath() + "/";

			z.putNextEntry(new ZipEntry(newDestDir));
			z.closeEntry();

			String[] dirlist = (input.list());

			input = null;

			for (int i = 0; i < dirlist.length; i++) {
				zip(new File(newInpurPath + dirlist[i]), newDestDir, z);

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
	/*
	 * public static void unzip(File zipFile, File outFolder) throws IOException { unzip(new FileInputStream(zipFile), outFolder); // new
	 * BufferedInputStream(zipFile) }
	 */

	public static void unzip(InputStream zipFile, OutputStream f) throws IOException {

		ZipInputStream in = new ZipInputStream(zipFile);
		ZipEntry entry;
		while ((entry = in.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[1000];

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
		ZipFile zf = new ZipFile(zipFile);

		while (zf.entries().hasMoreElements()) {
			ZipEntry entry = zf.entries().nextElement();
			int count;
			byte data[] = new byte[2048];
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

	private static void unzip(String zipFileName, InputStream zipFile, File outFolder, boolean prependZipFileName) throws IOException {
		BufferedOutputStream out = null;
		ZipInputStream in = new ZipInputStream(zipFile);
		ZipEntry entry;

		try {

			while ((entry = in.getNextEntry()) != null) {

				if (entry.isDirectory()) {

					File d = new File(outFolder.getPath() + "/" + entry.getName());
					d.mkdirs();

				} else {

					int count;
					byte data[] = new byte[1000];

					String outFileName = outFolder.getPath() + "/" + (prependZipFileName ? zipFileName + "_" : "") + entry.getName();
					if (new File(outFileName).exists())
						throw new ZipException("file already exists: " + outFileName);

					// write the files to the disk
					out = new BufferedOutputStream(new FileOutputStream(outFileName), 1000);

					while ((count = in.read(data, 0, 1000)) != -1) {
						out.write(data, 0, count);
					}

					out.flush();
					out.close();
				}
			}
		} finally {

			in.close();
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

	public static void unzipFile(String filePath) {

		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(filePath);
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while ((zEntry = zipIs.getNextEntry()) != null) {
				try {
					byte[] tmp = new byte[4 * 1024];
					FileOutputStream fos = null;
					String opFilePath = "C:/" + zEntry.getName();
					//System.out.println("Extracting file to " + opFilePath);
					fos = new FileOutputStream(opFilePath);
					int size = 0;
					while ((size = zipIs.read(tmp)) != -1) {
						fos.write(tmp, 0, size);
					}
					fos.flush();
					fos.close();
				} catch (Exception ex) {

				}
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String zipname = "c:\\test.zip";
		zip(new File(zipname), "C:\\ENEL\\TEST\\src\\Test.java",
				"c:\\excel\\3039_3035303332363330393633_454e4750473034_32372f30342f323031322031393a31333a3032.xls");
	}

}
