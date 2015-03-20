/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.documents.bo;

public class SDKFunctionality  implements java.io.Serializable {
    private java.lang.String code;

    private it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments;

    private it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities;

    private java.lang.String description;

    private java.lang.Integer id;

    private java.lang.String name;

    private java.lang.Integer parentId;

    private java.lang.String path;

    private java.lang.Integer prog;

    public SDKFunctionality() {
    }

    public SDKFunctionality(
           java.lang.String code,
           it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments,
           it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities,
           java.lang.String description,
           java.lang.Integer id,
           java.lang.String name,
           java.lang.Integer parentId,
           java.lang.String path,
           java.lang.Integer prog) {
           this.code = code;
           this.containedDocuments = containedDocuments;
           this.containedFunctionalities = containedFunctionalities;
           this.description = description;
           this.id = id;
           this.name = name;
           this.parentId = parentId;
           this.path = path;
           this.prog = prog;
    }


    /**
     * Gets the code value for this SDKFunctionality.
     * 
     * @return code
     */
    public java.lang.String getCode() {
        return code;
    }


    /**
     * Sets the code value for this SDKFunctionality.
     * 
     * @param code
     */
    public void setCode(java.lang.String code) {
        this.code = code;
    }


    /**
     * Gets the containedDocuments value for this SDKFunctionality.
     * 
     * @return containedDocuments
     */
    public it.eng.spagobi.sdk.documents.bo.SDKDocument[] getContainedDocuments() {
        return containedDocuments;
    }


    /**
     * Sets the containedDocuments value for this SDKFunctionality.
     * 
     * @param containedDocuments
     */
    public void setContainedDocuments(it.eng.spagobi.sdk.documents.bo.SDKDocument[] containedDocuments) {
        this.containedDocuments = containedDocuments;
    }


    /**
     * Gets the containedFunctionalities value for this SDKFunctionality.
     * 
     * @return containedFunctionalities
     */
    public it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] getContainedFunctionalities() {
        return containedFunctionalities;
    }


    /**
     * Sets the containedFunctionalities value for this SDKFunctionality.
     * 
     * @param containedFunctionalities
     */
    public void setContainedFunctionalities(it.eng.spagobi.sdk.documents.bo.SDKFunctionality[] containedFunctionalities) {
        this.containedFunctionalities = containedFunctionalities;
    }


    /**
     * Gets the description value for this SDKFunctionality.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this SDKFunctionality.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the id value for this SDKFunctionality.
     * 
     * @return id
     */
    public java.lang.Integer getId() {
        return id;
    }


    /**
     * Sets the id value for this SDKFunctionality.
     * 
     * @param id
     */
    public void setId(java.lang.Integer id) {
        this.id = id;
    }


    /**
     * Gets the name value for this SDKFunctionality.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKFunctionality.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the parentId value for this SDKFunctionality.
     * 
     * @return parentId
     */
    public java.lang.Integer getParentId() {
        return parentId;
    }


    /**
     * Sets the parentId value for this SDKFunctionality.
     * 
     * @param parentId
     */
    public void setParentId(java.lang.Integer parentId) {
        this.parentId = parentId;
    }


    /**
     * Gets the path value for this SDKFunctionality.
     * 
     * @return path
     */
    public java.lang.String getPath() {
        return path;
    }


    /**
     * Sets the path value for this SDKFunctionality.
     * 
     * @param path
     */
    public void setPath(java.lang.String path) {
        this.path = path;
    }


    /**
     * Gets the prog value for this SDKFunctionality.
     * 
     * @return prog
     */
    public java.lang.Integer getProg() {
        return prog;
    }


    /**
     * Sets the prog value for this SDKFunctionality.
     * 
     * @param prog
     */
    public void setProg(java.lang.Integer prog) {
        this.prog = prog;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKFunctionality)) return false;
        SDKFunctionality other = (SDKFunctionality) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.code==null && other.getCode()==null) || 
             (this.code!=null &&
              this.code.equals(other.getCode()))) &&
            ((this.containedDocuments==null && other.getContainedDocuments()==null) || 
             (this.containedDocuments!=null &&
              java.util.Arrays.equals(this.containedDocuments, other.getContainedDocuments()))) &&
            ((this.containedFunctionalities==null && other.getContainedFunctionalities()==null) || 
             (this.containedFunctionalities!=null &&
              java.util.Arrays.equals(this.containedFunctionalities, other.getContainedFunctionalities()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.parentId==null && other.getParentId()==null) || 
             (this.parentId!=null &&
              this.parentId.equals(other.getParentId()))) &&
            ((this.path==null && other.getPath()==null) || 
             (this.path!=null &&
              this.path.equals(other.getPath()))) &&
            ((this.prog==null && other.getProg()==null) || 
             (this.prog!=null &&
              this.prog.equals(other.getProg())));
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
        if (getCode() != null) {
            _hashCode += getCode().hashCode();
        }
        if (getContainedDocuments() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getContainedDocuments());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getContainedDocuments(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getContainedFunctionalities() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getContainedFunctionalities());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getContainedFunctionalities(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getParentId() != null) {
            _hashCode += getParentId().hashCode();
        }
        if (getPath() != null) {
            _hashCode += getPath().hashCode();
        }
        if (getProg() != null) {
            _hashCode += getProg().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKFunctionality.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKFunctionality"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("code");
        elemField.setXmlName(new javax.xml.namespace.QName("", "code"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containedDocuments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "containedDocuments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocument"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("containedFunctionalities");
        elemField.setXmlName(new javax.xml.namespace.QName("", "containedFunctionalities"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKFunctionality"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parentId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parentId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("path");
        elemField.setXmlName(new javax.xml.namespace.QName("", "path"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prog");
        elemField.setXmlName(new javax.xml.namespace.QName("", "prog"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
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
