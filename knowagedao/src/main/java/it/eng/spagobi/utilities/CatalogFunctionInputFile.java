package it.eng.spagobi.utilities;


public class CatalogFunctionInputFile {

	private String fileName;
	private String alias;
	private byte[] content;

	public CatalogFunctionInputFile(String fileName, String alias, byte[] content) {
		super();
		this.fileName = fileName;
		this.alias = alias;
		this.content = content;

	}

	public CatalogFunctionInputFile() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
