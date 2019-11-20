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

package it.eng.spagobi.tools.dataset.metasql.query.visitor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.CompoundFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NotInFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;

public abstract class AbstractFilterVisitor implements IFilterVisitor {

    private static final Logger logger = Logger.getLogger(AbstractFilterVisitor.class);

    protected StringBuilder queryBuilder = new StringBuilder();

    @Override
    public void visit(AndFilter item) {
        visit((CompoundFilter) item);
    }

    @Override
    public void visit(OrFilter item) {
        visit((CompoundFilter) item);
    }

    public void visit(SimpleFilter item) {
        if (item instanceof BetweenFilter) {
            visit((BetweenFilter) item);
        } else if (item instanceof InFilter) {
            visit((InFilter) item);
        }
         else if (item instanceof NotInFilter) {
                visit((NotInFilter) item);
        } else if (item instanceof LikeFilter) {
            visit((LikeFilter) item);
        } else if (item instanceof NullaryFilter) {
            visit((NullaryFilter) item);
        } else if (item instanceof UnaryFilter) {
            visit((UnaryFilter) item);
        } else if (item instanceof UnsatisfiedFilter) {
            visit((UnsatisfiedFilter) item);
        } else {
            throw new IllegalArgumentException("No visit(" + item.getClass().getCanonicalName() + ") method available");
        }
    }

    @Override
    public void visit(Filter filter) {
        if (filter instanceof CompoundFilter) {
            visit((CompoundFilter) filter);
        } else if (filter instanceof SimpleFilter) {
            visit((SimpleFilter) filter);
        } else {
            throw new IllegalArgumentException("No visit(" + filter.getClass().getCanonicalName() + ") method available");
        }
    }

    protected void visit(CompoundFilter item) {
        String spacedOp = " " + item.getCompositionOperator().toString() + " ";
        List<Filter> filters = item.getFilters();

        boolean isCompoundFilter = filters.get(0) instanceof CompoundFilter;
        queryBuilder.append(isCompoundFilter ? "(" : "");
        visit(filters.get(0));
        queryBuilder.append(isCompoundFilter ? ")" : "");

        for (int i = 1; i < filters.size(); i++) {
            queryBuilder.append(spacedOp);
            isCompoundFilter = filters.get(i) instanceof CompoundFilter;
            queryBuilder.append(isCompoundFilter ? "(" : "");
            visit(filters.get(i));
            queryBuilder.append(isCompoundFilter ? ")" : "");
        }
    }

    protected Filter transformToAndOrFilters(InFilter item) {
        List<Projection> projections = item.getProjections();
        List<Object> operands = item.getOperands();

        int columnCount = projections.size();
        int tupleCount = operands.size() / columnCount;

        AndFilter[] andFilters = new AndFilter[tupleCount];
        for (int tupleIndex = 0; tupleIndex < tupleCount; tupleIndex++) {
            UnaryFilter[] equalFilters = new UnaryFilter[columnCount];
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                equalFilters[columnIndex] = new UnaryFilter(projections.get(columnIndex), SimpleFilterOperator.EQUALS_TO,
                        operands.get(columnIndex + tupleIndex * columnCount));
            }

            andFilters[tupleIndex] = new AndFilter(equalFilters);
        }
        return new OrFilter(andFilters);
    }

    protected Filter transformToAndOrFilters(NotInFilter item) {
        List<Projection> projections = item.getProjections();
        List<Object> operands = item.getOperands();

        int columnCount = projections.size();
        int tupleCount = operands.size() / columnCount;

        AndFilter[] andFilters = new AndFilter[tupleCount];
        for (int tupleIndex = 0; tupleIndex < tupleCount; tupleIndex++) {
            UnaryFilter[] equalFilters = new UnaryFilter[columnCount];
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                equalFilters[columnIndex] = new UnaryFilter(projections.get(columnIndex), SimpleFilterOperator.EQUALS_TO,
                        operands.get(columnIndex + tupleIndex * columnCount));
            }

            andFilters[tupleIndex] = new AndFilter(equalFilters);
        }
        return new OrFilter(andFilters);
    }
    protected abstract String getFormattedTimestamp(Timestamp timestamp);

    protected abstract String getFormattedDate(Date date);
}
