package it.eng.spagobi.commons.dao;

public interface EmittingEventDAO<T> {

	void setEventEmittingCommand(T command);

}