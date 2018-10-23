/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.strategy;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import org.apache.log4j.Logger;

class InlineViewEvaluationStrategy extends AbstractJdbcEvaluationStrategy {

    private static final Logger logger = Logger.getLogger(InlineViewEvaluationStrategy.class);

    public InlineViewEvaluationStrategy(IDataSet dataSet) {
        super(dataSet);
    }

    @Override
    protected String getTableName()  throws DataBaseException {
        QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
        String subQueryAlias = DataBaseFactory.getDataBase(getDataSource()).getSubQueryAlias();
        return "(\n" + querableBehaviour.getStatement().replace(";", "") + "\n) " + subQueryAlias;
    }

    @Override
    protected IDataSource getDataSource() {
        return  dataSet.getDataSource();
    }
}
