package it.eng.spagobi.commons.dao;

import org.hibernate.Session;

public interface IExecuteOnTransaction<T> {
	public T execute(Session session) throws Exception;
}
