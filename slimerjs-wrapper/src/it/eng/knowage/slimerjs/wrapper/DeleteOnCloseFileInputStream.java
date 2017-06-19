package it.eng.knowage.slimerjs.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This input stream deletes the file after closing. This is used to deliver the result of a render call so that files are not left on disk after usage.
 */
public class DeleteOnCloseFileInputStream extends FileInputStream {
	private final File file;

	public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (file != null) {
				Files.deleteIfExists(file.toPath());
			}
		}
	}
}
