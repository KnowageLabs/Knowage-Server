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

package it.eng.spagobi.tools.dataset.solr;

import it.eng.spagobi.tools.dataset.metasql.query.item.*;
import it.eng.spagobi.tools.dataset.metasql.query.visitor.AbstractFilterVisitor;
import it.eng.spagobi.utilities.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SolrFilterVisitor extends AbstractFilterVisitor {

    private static final Logger logger = Logger.getLogger(SolrFilterVisitor.class);

    private static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Override
    public void visit(BetweenFilter item) {
        append(item.getProjection());
        queryBuilder.append(":");
        queryBuilder.append("[");
        append(item.getBeginValue());
        queryBuilder.append(" TO ");
        append(item.getEndValue());
        queryBuilder.append("]");
    }

    @Override
    public void visit(InFilter item) {
        if(item.getProjections().size() == 1) {
            queryBuilder.append(item.getProjections().get(0));
            queryBuilder.append(":");
            queryBuilder.append("(");
            String isNot = item.getOperator().equals(SimpleFilterOperator.NOT_IN) ? "-" : "";
            String values = StringUtils.join(item.getOperands(), " ", isNot);
            queryBuilder.append(values);
            queryBuilder.append(")");
        } else {
            visit(transformToAndOrFilters(item));
        }
    }

    @Override
    public void visit(LikeFilter item) {
        if(item.getOperator().equals(SimpleFilterOperator.NOT_LIKE)) {
            queryBuilder.append("-");
        }
        queryBuilder.append(item.getProjection().getName());
        queryBuilder.append(":");
        String value = item.isPattern() ? item.getValue() : "*" + item.getValue() + "*";
        queryBuilder.append(value);
    }

    @Override
    public void visit(NullaryFilter item) {
        if(item.getOperator().equals(SimpleFilterOperator.IS_NULL)) {
            queryBuilder.append("-");
        }
        queryBuilder.append(item.getProjection().getName());
        queryBuilder.append(":[* TO *]");
    }

    @Override
    public void visit(UnaryFilter item) {
        append(item.getProjection());
        queryBuilder.append(" ");
        queryBuilder.append(item.getOperator());
        queryBuilder.append(" ");
        append(item.getOperand());
    }

    @Override
    public void visit(UnsatisfiedFilter item) {
        throw new UnsupportedOperationException("Visitor for " + UnsatisfiedFilter.class + "is not implemented yet");
    }

    public void apply(SolrQuery solrQuery, Filter filter) {
        visit(filter);
        solrQuery.addFilterQuery(queryBuilder.toString());
    }

    private void append(Projection projection) {
        queryBuilder.append(projection.getName());
    }

    protected void append(Object operand) {
        String parsedOperand;
        if (operand == null) {
            parsedOperand = "NULL";
        } else {
            if (Timestamp.class.isAssignableFrom(operand.getClass())) {
                parsedOperand = getFormattedTimestamp((Timestamp) operand);
            } else if (Date.class.isAssignableFrom(operand.getClass())) {
                parsedOperand = getFormattedDate((Date) operand);
            } else {
                parsedOperand = operand.toString();
            }
        }
        queryBuilder.append(ClientUtils.escapeQueryChars(parsedOperand));
    }

    @Override
    protected String getFormattedTimestamp(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SOLR_DATE_FORMAT);
        return dateFormat.format(timestamp);
    }

    @Override
    protected String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SOLR_DATE_FORMAT);
        return dateFormat.format(date);
    }

}
