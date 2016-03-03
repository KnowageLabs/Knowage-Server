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

import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.images.dao.criterion.GetImagesByName;
import it.eng.spagobi.images.dao.criterion.SearchImages;
import it.eng.spagobi.images.dao.util.ImageUtil;
import it.eng.spagobi.images.metadata.SbiImages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class ImagesDAOImpl extends AbstractHibernateDAO implements IImagesDAO {

	static protected Logger logger = Logger.getLogger(ImagesDAOImpl.class);

	@Override
	public SbiImages loadImage(Integer id) {
		return load(SbiImages.class, id);
	}

	@Override
	public SbiImages loadImage(String name) {
		List<SbiImages> l = list(new GetImagesByName(name));
		if (l != null && !l.isEmpty()) {
			return l.get(0);
		}
		return null;
	}

	@Override
	public List<SbiImages> listImages(String name, String description, Map<OrderBy, Direction> sort) {
		return list(new SearchImages(name, description, sort));
	}

	@Override
	public void deleteImage(Integer id) {
		delete(SbiImages.class, id);
	}

	@Override
	public void deleteImage(String name) {
		SbiImages image = loadImage(name);
		if (image != null && image.getImageId() != null) {
			delete(SbiImages.class, image.getImageId());
		}
	}

	@Override
	public Integer insertImage(final String filename, final byte[] data) {
		return executeOnTransaction(new IExecuteOnTransaction<Integer>() {
			@Override
			public Integer execute(Session session) {
				Blob b = session.getLobHelper().createBlob(data);
				ByteArrayOutputStream contentIcoBytes = new ByteArrayOutputStream();
				Blob contentIco = null;
				try {
					ImageUtil.resize(filename, new ByteArrayInputStream(data), contentIcoBytes, 80, 80);
					contentIco = session.getLobHelper().createBlob(contentIcoBytes.toByteArray());
					contentIcoBytes.close();
				} catch (IOException e) {
					throw new SpagoBIDOAException("Error while resizing image for preview field", e);
				}
				SbiImages sbiImage = new SbiImages();
				sbiImage.setName(filename);
				sbiImage.setContent(b);
				sbiImage.setContentIco(contentIco);
				updateSbiCommonInfo4Insert(sbiImage);
				return (Integer) session.save(sbiImage);
			}

		});
	}

	@Override
	public void updateImage(SbiImages sbiImage) {
		update(sbiImage);
	}

	@Override
	public long countImages(final boolean restrictByUser) {
		return executeOnTransaction(new IExecuteOnTransaction<Long>() {
			@Override
			public Long execute(Session session) {
				String user = getUserProfile().getUserUniqueIdentifier().toString();
				Criteria c = session.createCriteria(SbiImages.class).setProjection(Projections.rowCount());
				if (restrictByUser)
					c.add(Restrictions.eq("commonInfo.userIn", user));
				Object result = c.uniqueResult();
				return result == null ? 0 : (Long) result;
			}
		}).longValue();
	}

	public static void main(String[] args) {
		logger.debug(OrderBy.valueOf("CommonInfo").valueOf("timeIn"));
	}
}
