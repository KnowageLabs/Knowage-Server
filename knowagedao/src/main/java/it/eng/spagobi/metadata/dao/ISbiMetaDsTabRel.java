package it.eng.spagobi.metadata.dao;

import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.metadata.metadata.SbiMetaDsTabRel;

public interface ISbiMetaDsTabRel extends ISpagoBIDao {

	public SbiMetaDsTabRel loadRelationById(Integer relationId) throws EMFUserError;

	public SbiMetaDsTabRel loadDsIdandTableId(Integer dsId, Integer tableId) throws EMFUserError;

	public List<SbiMetaDsTabRel> loadByDatasetId(Integer datasetId) throws EMFUserError;

	public List<SbiMetaDsTabRel> loadAllRelations() throws EMFUserError;

	public void modifyRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError;

	public void insertRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError;

	public void deleteRelation(SbiMetaDsTabRel SbiMetaDsTabRel) throws EMFUserError;

}
