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

package it.eng.qbe.model.structure;

import it.eng.spagobi.utilities.objects.Couple;

public interface IModelField extends IModelNode {

	@Override
	public String getUniqueName();

	public Couple getQueryName();

	public String getType();

	public void setType(String type);

	public int getLength();

	public void setLength(int length);

	public int getPrecision();

	public void setPrecision(int precision);

	public boolean isKey();

	public void setKey(boolean key);

	public IModelField clone(IModelEntity newParent);

	public Class getJavaClass();

	public void setJavaClass(Class javaClass);

	public boolean isEncrypted();
}
