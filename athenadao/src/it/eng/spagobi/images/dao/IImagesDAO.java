package it.eng.spagobi.images.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.images.metadata.SbiImages;

import java.util.List;
import java.util.Map;

public interface IImagesDAO extends ISpagoBIDao {
	public enum OrderBy {
		name, timeIn
	};

	public enum Direction {
		asc, desc
	};

	public SbiImages loadImage(Integer id);

	public SbiImages loadImage(String name);

	public List<SbiImages> listImages(String name, String description, Map<OrderBy, Direction> sort);

	public long countImages(boolean restrictByUser);

	public void deleteImage(Integer id);

	public void deleteImage(String name);

	public Integer insertImage(String filename, byte[] data);

	public void updateImage(SbiImages sbiImage);

}
