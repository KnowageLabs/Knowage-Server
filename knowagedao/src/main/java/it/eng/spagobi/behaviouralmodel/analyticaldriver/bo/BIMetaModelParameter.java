package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

public class BIMetaModelParameter extends AbstractDriver {

	private Integer biMetaModelID;

	public Integer getBiMetaModelID() {
		return biMetaModelID;
	}

	public void setBiMetaModelID(Integer biMetaModelID) {
		this.biMetaModelID = biMetaModelID;
	}

	@Override
	public BIMetaModelParameter clone() {
		BIMetaModelParameter toReturn = new BIMetaModelParameter();
		toReturn.setLabel(super.getLabel());
		toReturn.setParameterUrlName(super.getParameterUrlName());
		toReturn.setParameterValues(super.getParameterValues());
		toReturn.setParameterValuesDescription(super.getParameterValuesDescription());
		toReturn.setParameter(super.getParameter());
		toReturn.setIterative(super.isIterative());
		toReturn.setTransientParmeters(super.isTransientParmeters());
		toReturn.setHasValidValues(super.hasValidValues());
		toReturn.setBiMetaModelID(biMetaModelID);
		toReturn.setId(super.getId());
		toReturn.setModifiable(super.getModifiable());
		toReturn.setMultivalue(super.isMultivalue());
		toReturn.setParID(super.getParID());
		toReturn.setPriority(super.getPriority());
		toReturn.setProg(super.getProg());
		toReturn.setRequired(super.isRequired());
		toReturn.setVisible(super.getVisible());
		toReturn.setColSpan(super.getColSpan());
		toReturn.setThickPerc(super.getThickPerc());

		return toReturn;
	}

}
