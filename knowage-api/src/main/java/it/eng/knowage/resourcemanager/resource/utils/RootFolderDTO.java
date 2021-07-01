package it.eng.knowage.resourcemanager.resource.utils;

public class RootFolderDTO {

	private FolderDTO root;

	public RootFolderDTO() {
		super();
	}

	public RootFolderDTO(FolderDTO subfolder) {
		super();
		this.root = subfolder;

	}

	public FolderDTO getRoot() {
		return root;
	}

	public void setRoot(FolderDTO root) {
		this.root = root;
	}

}
