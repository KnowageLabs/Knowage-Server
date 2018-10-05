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
package it.eng.spagobi.tools.scheduler.wsEvents.dao.criterion;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import it.eng.spagobi.commons.dao.ICriterion;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;

public class SearchWsEventNotConsumed implements ICriterion<SbiWsEvent> {

	public SearchWsEventNotConsumed() {
	}

	@Override
	public Criteria evaluate(Session session) {
		Criteria c = session.createCriteria(SbiWsEvent.class);
		c.add(Restrictions.isNull("takeChargeDate"));
		return c;
	}

}
