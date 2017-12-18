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
