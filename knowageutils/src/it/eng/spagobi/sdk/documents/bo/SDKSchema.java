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

package it.eng.spagobi.sdk.documents.bo;


public class SDKSchema  implements java.io.Serializable {

    private java.lang.String schemaName;

    private java.lang.String schemaDescription;
    
    private java.lang.String schemaDataSourceLbl;
    
    private it.eng.spagobi.sdk.importexport.bo.SDKFile schemaFile; 

    public SDKSchema() {
    }

    public SDKSchema(
    	   java.lang.String schemaName,
           java.lang.String schemaDescription,
           java.lang.String schemaDataSourceLbl,
           it.eng.spagobi.sdk.importexport.bo.SDKFile schemaFile) {
           this.schemaName = schemaName;
           this.schemaDescription = schemaDescription;
           this.schemaDataSourceLbl = schemaDataSourceLbl;
           this.schemaFile = schemaFile;
    }


    /**
	 * @return the schemaName
	 */
	public java.lang.String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaName(java.lang.String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the schemaDescription
	 */
	public java.lang.String getSchemaDescription() {
		return schemaDescription;
	}

	/**
	 * @param schemaDescription the schemaDescription to set
	 */
	public void setSchemaDescription(java.lang.String schemaDescription) {
		this.schemaDescription = schemaDescription;
	}

	/**
	 * @return the schemaDataSourceLbl
	 */
	public java.lang.String getSchemaDataSourceLbl() {
		return schemaDataSourceLbl;
	}

	/**
	 * @param schemaDataSourceLbl the schemaDataSourceLbl to set
	 */
	public void setSchemaDataSourceLbl(java.lang.String schemaDataSourceLbl) {
		this.schemaDataSourceLbl = schemaDataSourceLbl;
	}

	/**
	 * @return the schemaFile
	 */
	public it.eng.spagobi.sdk.importexport.bo.SDKFile getSchemaFile() {
		return schemaFile;
	}

	/**
	 * @param schemaFile the schemaFile to set
	 */
	public void setSchemaFile(it.eng.spagobi.sdk.importexport.bo.SDKFile schemaFile) {
		this.schemaFile = schemaFile;
	}


	private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKSchema)) return false;
        SDKSchema other = (SDKSchema) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.schemaName==null && other.getSchemaName()==null) || 
             (this.schemaName!=null &&
              this.schemaName.equals(other.getSchemaName()))) &&
            ((this.schemaDescription==null && other.getSchemaDescription()==null) || 
             (this.schemaDescription!=null &&
              this.schemaDescription.equals(other.getSchemaDescription()))) &&
            ((this.schemaDataSourceLbl==null && other.getSchemaDataSourceLbl()==null) || 
             (this.schemaDataSourceLbl!=null &&
              this.schemaDataSourceLbl.equals(other.getSchemaDataSourceLbl())))&&
            ((this.schemaFile==null && other.getSchemaFile()==null) || 
             (this.schemaFile!=null &&
              this.schemaFile.equals(other.getSchemaFile())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getSchemaName() != null) {
            _hashCode += getSchemaName().hashCode();
        }
        if (getSchemaDescription() != null) {
            _hashCode += getSchemaDescription().hashCode();
        }
        if (getSchemaDataSourceLbl() != null) {
            _hashCode += getSchemaDataSourceLbl().hashCode();
        }
        if (getSchemaFile() != null) {
            _hashCode += getSchemaFile().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKSchema.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKSchema"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schemaName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schemaName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schemaDescription");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schemaDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schemaDataSourceLbl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schemaDataSourceLbl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("schemaFile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "schemaFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.importexport.sdk.spagobi.eng.it", "SDKFile"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
