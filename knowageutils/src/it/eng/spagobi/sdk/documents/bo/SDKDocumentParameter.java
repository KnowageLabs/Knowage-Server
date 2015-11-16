/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.documents.bo;

public class SDKDocumentParameter  implements java.io.Serializable {
    private it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints;

    private java.lang.Integer id;

    private java.lang.String label;

    private java.lang.String type;

    private java.lang.String urlName;

    private java.lang.Object[] values;

    public SDKDocumentParameter() {
    }

    public SDKDocumentParameter(
           it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints,
           java.lang.Integer id,
           java.lang.String label,
           java.lang.String type,
           java.lang.String urlName,
           java.lang.Object[] values) {
           this.constraints = constraints;
           this.id = id;
           this.label = label;
           this.type = type;
           this.urlName = urlName;
           this.values = values;
    }


    /**
     * Gets the constraints value for this SDKDocumentParameter.
     * 
     * @return constraints
     */
    public it.eng.spagobi.sdk.documents.bo.SDKConstraint[] getConstraints() {
        return constraints;
    }


    /**
     * Sets the constraints value for this SDKDocumentParameter.
     * 
     * @param constraints
     */
    public void setConstraints(it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints) {
        this.constraints = constraints;
    }


    /**
     * Gets the id value for this SDKDocumentParameter.
     * 
     * @return id
     */
    public java.lang.Integer getId() {
        return id;
    }


    /**
     * Sets the id value for this SDKDocumentParameter.
     * 
     * @param id
     */
    public void setId(java.lang.Integer id) {
        this.id = id;
    }


    /**
     * Gets the label value for this SDKDocumentParameter.
     * 
     * @return label
     */
    public java.lang.String getLabel() {
        return label;
    }


    /**
     * Sets the label value for this SDKDocumentParameter.
     * 
     * @param label
     */
    public void setLabel(java.lang.String label) {
        this.label = label;
    }


    /**
     * Gets the type value for this SDKDocumentParameter.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this SDKDocumentParameter.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the urlName value for this SDKDocumentParameter.
     * 
     * @return urlName
     */
    public java.lang.String getUrlName() {
        return urlName;
    }


    /**
     * Sets the urlName value for this SDKDocumentParameter.
     * 
     * @param urlName
     */
    public void setUrlName(java.lang.String urlName) {
        this.urlName = urlName;
    }


    /**
     * Gets the values value for this SDKDocumentParameter.
     * 
     * @return values
     */
    public java.lang.Object[] getValues() {
        return values;
    }


    /**
     * Sets the values value for this SDKDocumentParameter.
     * 
     * @param values
     */
    public void setValues(java.lang.Object[] values) {
        this.values = values;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKDocumentParameter)) return false;
        SDKDocumentParameter other = (SDKDocumentParameter) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.constraints==null && other.getConstraints()==null) || 
             (this.constraints!=null &&
              java.util.Arrays.equals(this.constraints, other.getConstraints()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.label==null && other.getLabel()==null) || 
             (this.label!=null &&
              this.label.equals(other.getLabel()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.urlName==null && other.getUrlName()==null) || 
             (this.urlName!=null &&
              this.urlName.equals(other.getUrlName()))) &&
            ((this.values==null && other.getValues()==null) || 
             (this.values!=null &&
              java.util.Arrays.equals(this.values, other.getValues())));
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
        if (getConstraints() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getConstraints());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getConstraints(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getLabel() != null) {
            _hashCode += getLabel().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getUrlName() != null) {
            _hashCode += getUrlName().hashCode();
        }
        if (getValues() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValues());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getValues(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKDocumentParameter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKDocumentParameter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("constraints");
        elemField.setXmlName(new javax.xml.namespace.QName("", "constraints"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.documents.sdk.spagobi.eng.it", "SDKConstraint"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("label");
        elemField.setXmlName(new javax.xml.namespace.QName("", "label"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("urlName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "urlName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("values");
        elemField.setXmlName(new javax.xml.namespace.QName("", "values"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
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
