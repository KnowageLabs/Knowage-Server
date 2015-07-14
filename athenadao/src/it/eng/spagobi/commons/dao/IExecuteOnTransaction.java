package it.eng.spagobi.commons.dao;

import org.hibernate.Session;
import org.json.JSONException;

public interface IExecuteOnTransaction<T> {
	public T execute(Session session) throws JSONException;
}
