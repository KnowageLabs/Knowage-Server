/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
 */
package it.eng.spagobi.tools.dataset.validation;

import it.eng.spagobi.tools.dataset.common.datastore.IField;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ErrorField {
	
	private int columnIndex;
	private IField field;
	private String errorDescription;
	
	
	public ErrorField(int columnIndex, IField field){
		this.columnIndex = columnIndex;
		this.field = field;
	}
	
	public ErrorField(int columnIndex, IField field, String errorDescription){
		this.columnIndex = columnIndex;
		this.field = field;
		this.errorDescription = errorDescription;
	}
	
	
	
	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @param errorDescription the errorDescription to set
	 */
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}


	/**
	 * @return the columnIndex
	 */
	public int getColumnIndex() {
		return columnIndex;
	}
	/**
	 * @param columnIndex the columnIndex to set
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	/**
	 * @return the field
	 */
	public IField getField() {
		return field;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(IField field) {
		this.field = field;
	}
	
	public String toString() {
		return "ColumnIndex = "+columnIndex+" FieldValue = "+field.getValue();
	}
	
	

}
