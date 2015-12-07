package it.eng.spagobi.utilities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class HelperForTest {

	private HelperForTest() {
	}

	public static String readFile(String fileName, Class<?> clazz) throws IOException {
		InputStream in = clazz.getResourceAsStream(fileName);
		String res= IOUtils.toString(in, "UTF-8");
		in.close();
		return res;
	}

	public static boolean all(boolean[] done) {
		for (int i = 1; i < done.length; i++) {
			if (done[i] != done[i - 1]) {
				return false;
			}
		}
		return true;
	}

	public static byte[] getFileContent(String fileName, Class<?> clazz) throws IOException {
		InputStream in = clazz.getResourceAsStream(fileName);
		byte[] res= IOUtils.toByteArray(in);
		in.close();
		return res;
	}

}
