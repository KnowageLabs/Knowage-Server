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
package it.eng.spagobi.tools.crossnavigation.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;

import java.util.List;

public interface ICrossNavigationDAO extends ISpagoBIDao {

	public List<SimpleNavigation> listNavigation();

	public NavigationDetail loadNavigation(Integer id);

	public void update(NavigationDetail nd);

	public void insert(NavigationDetail nd);

	public void delete(Integer id);

}
