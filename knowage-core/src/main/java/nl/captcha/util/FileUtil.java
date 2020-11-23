package nl.captcha.util;

import java.io.*;

public class FileUtil {

    /**
     * Get a file resource and return it as an InputStream. Intended primarily
     * to read in binary files which are contained in a jar.
     * 
     * @param filename
     * @return An @{link InputStream} to the file
     */
    public static final InputStream readResource(String filename) {
        InputStream jarIs = FileUtil.class.getResourceAsStream(filename);
        if (jarIs == null) {
            throw new RuntimeException(new FileNotFoundException("File '"
                    + filename + "' not found."));
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[16384];
        int nRead;

        try {
            while ((nRead = jarIs.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            jarIs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

}
