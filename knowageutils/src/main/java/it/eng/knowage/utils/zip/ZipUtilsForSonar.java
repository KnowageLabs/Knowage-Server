package it.eng.knowage.utils.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipUtilsForSonar {
	
	private static final Logger logger = Logger.getLogger(ZipUtilsForSonar.class);
	
	int thresholdEntries = 10000;
	int thresholdSize = 1000000000;
	double thresholdRatio = 10;
	int totalSizeArchive = 0;
	int totalEntryArchive = 0;
	
	public boolean doThresholdCheck(String inFile) {
		try {
			File f = new File(inFile);
			ZipFile zipFile = new ZipFile(f);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			ZipEntry ze = entries.nextElement();
			InputStream in = new BufferedInputStream(zipFile.getInputStream(ze));
			//OutputStream out = new BufferedOutputStream(new FileOutputStream("./output_onlyfortesting.txt"));
	
			totalEntryArchive ++;
	
			int nBytes = -1;
			byte[] buffer = new byte[2048];
			int totalSizeEntry = 0;
	
			while((nBytes = in.read(buffer)) > 0) {
				//out.write(buffer, 0, nBytes);
			    totalSizeEntry += nBytes;
			    totalSizeArchive += nBytes;
	
			    double compressionRatio = (double) totalSizeEntry / ze.getCompressedSize();
			    if(compressionRatio > thresholdRatio) {
			    	// ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack
			    	return false;
			    }
			}
			
			if(totalSizeArchive > thresholdSize) {
			      // the uncompressed data size is too much for the application resource capacity
			      return false;
			}
	
			if(totalEntryArchive > thresholdEntries) {
			      // too much entries in this archive, can lead to inodes exhaustion of the system
				return false;
			}
			//out.close();
			return true;
			
		} catch (Exception e) {
			logger.error("Error while unzip file. Invalid archive file");
			throw new SpagoBIServiceException("Error while unzip file. Invalid archive file", e);
		}
	}
}
