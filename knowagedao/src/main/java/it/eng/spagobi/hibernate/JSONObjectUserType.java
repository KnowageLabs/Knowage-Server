/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class JSONObjectUserType implements UserType {

	private static final int[] SQL_TYPES = { Types.LONGVARCHAR };

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return deepCopy(cached);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		if (value == null)
			return value;
		try {
			return new JSONObject(((JSONObject) value).toString());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return ((JSONObject) value).toString();
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == null)
			return (y != null);
		return (x.equals(y));
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return ((JSONObject) x).toString().hashCode();
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		Object ret = null;
		try {
			Optional<String> value = Optional.ofNullable(rs.getString(names[0]));

			if (value.isPresent()) {
				ret = new JSONObject(value.get());
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, SQL_TYPES[0]);
		} else {
			st.setString(index, ((JSONObject) value).toString());
		}
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return deepCopy(original);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class returnedClass() {
		return JSONObject.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}