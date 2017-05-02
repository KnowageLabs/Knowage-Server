package it.eng.spagobi.hdfs;

import java.io.IOException;

import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class HdfsUtilities {

	private static final Logger logger = Logger.getLogger(HdfsUtilities.class);

	public static String getHdfsSperator() {
		return Path.SEPARATOR;
	}

	public boolean copyBetweenHdfs(Hdfs hdfsSrc, String src, Hdfs hdfsDst, String dst, boolean deleteSrc) {
		Path pathSrc = new Path(src);
		Path pathDst = new Path(dst);
		try {
			FileUtil.copy(hdfsSrc.getFs(), pathSrc, hdfsDst.getFs(), pathDst, deleteSrc, hdfsSrc.getConfiguration());
		} catch (IOException e) {
			logger.error("Impossible to move the file from HDFS path\"" + src + "\" to path: \"" + dst + "\"" + e);
			return false;
		}
		return true;
	}
}
