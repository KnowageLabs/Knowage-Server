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
package it.eng.spagobi.metadata.cwm;

import it.eng.spagobi.meta.model.ModelObject;
import it.eng.spagobi.meta.model.physical.PhysicalModel;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class ICWMMapper implements IMapper {

	@Override
	public ModelObject decodeModel(Object o) {
		if (o instanceof ICWM) {
			return decodeICWM((ICWM) o);
		} else {
			return null;
		}

	}

	public abstract PhysicalModel decodeICWM(ICWM cwm);

	@Override
	public Object encodeModel(ModelObject modelObject) {
		// TODO for the moment we are able to encode only a small portion of the entire model
		if (modelObject instanceof PhysicalModel) {
			return encodeICWM((PhysicalModel) modelObject);
		} else {
			return null;
		}
	}

	public abstract ICWM encodeICWM(PhysicalModel model);
}
