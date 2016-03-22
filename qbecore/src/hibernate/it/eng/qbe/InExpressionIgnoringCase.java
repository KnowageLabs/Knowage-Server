package it.eng.qbe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.TypedValue;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;
import org.hibernate.util.StringHelper;

/**
 * Code taken from see org.hibernate.criterion.InExpression
 * 
 */
public class InExpressionIgnoringCase implements Criterion {

	private final String propertyName;
	private final Object[] values;

	public InExpressionIgnoringCase(String propertyName, Object[] values) {
		this.propertyName = propertyName;
		this.values = values;
	}

	public InExpressionIgnoringCase(String propertyName, Collection values) {
		this.propertyName = propertyName;
		this.values = values.toArray();
	}

	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		String[] columns = criteriaQuery.findColumns(propertyName, criteria);
		final String[] wrappedLowerColumns = wrapLower(columns);
		if (criteriaQuery.getFactory().getDialect().supportsRowValueConstructorSyntaxInInList() || columns.length <= 1) {

			String singleValueParam = StringHelper.repeat("lower(?), ", columns.length - 1) + "?";
			if (columns.length > 1) {
				singleValueParam = '(' + singleValueParam + ')';
			}
			String params = values.length > 0 ? StringHelper.repeat(singleValueParam + ", ", values.length - 1) + singleValueParam : "";
			String cols = StringHelper.join(", ", wrappedLowerColumns);
			if (columns.length > 1) {
				cols = '(' + cols + ')';
			}
			return cols + " in (" + params + ')';
		} else {
			String cols = " ( " + StringHelper.join(" = lower(?) and ", wrappedLowerColumns) + "= lower(?) ) ";
			cols = values.length > 0 ? StringHelper.repeat(cols + "or ", values.length - 1) + cols : "";
			cols = " ( " + cols + " ) ";
			return cols;
		}
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		List<TypedValue> list = new ArrayList<>();
		Type type = criteriaQuery.getTypeUsingProjection(criteria, propertyName);
		if (type.isComponentType()) {
			CompositeType actype = (CompositeType) type;
			Type[] types = actype.getSubtypes();
			for (int j = 0; j < values.length; j++) {
				for (int i = 0; i < types.length; i++) {
					Object subval = values[j] == null ? null : actype.getPropertyValues(values[j], EntityMode.POJO)[i];
					list.add(new TypedValue(types[i], subval, EntityMode.POJO));
				}
			}
		} else {
			for (int j = 0; j < values.length; j++) {
				list.add(new TypedValue(type, values[j], EntityMode.POJO));
			}
		}
		return list.toArray(new TypedValue[list.size()]);
	}

	@Override
	public String toString() {
		return propertyName + " in (" + StringHelper.toString(values) + ')';
	}

	private String[] wrapLower(final String[] columns) {
		final String[] wrappedColumns = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			wrappedColumns[i] = "lower(" + columns[i] + ")";
		}
		return wrappedColumns;
	}

}
