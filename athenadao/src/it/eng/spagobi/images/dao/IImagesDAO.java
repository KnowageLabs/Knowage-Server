package it.eng.spagobi.images.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.images.metadata.SbiImages;

import java.util.List;

public interface IImagesDAO extends ISpagoBIDao {

	public SbiImages loadImage(Integer id);

	public SbiImages loadImage(String name);

	public List<SbiImages> listImages(String name, String description);

	public long countImages(boolean restrictByUser);

	public void deleteImage(Integer id);

	public void deleteImage(String name);

	public Integer insertImage(String filename, byte[] data);

	public void updateImage(SbiImages sbiImage);

}
