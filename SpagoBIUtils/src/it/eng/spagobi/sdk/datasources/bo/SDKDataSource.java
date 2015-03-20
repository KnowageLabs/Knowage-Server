/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.datasources.bo;

public class SDKDataSource  implements java.io.Serializable {
    private java.lang.String attrSchema;

    private java.lang.String descr;

    private java.lang.Integer dialectId;

    private java.lang.String driver;

    private java.lang.Integer id;

    private java.lang.String jndi;

    private java.lang.String label;

    private java.lang.Integer multiSchema;

    private java.lang.String name;

    private java.lang.String pwd;

    private java.lang.String urlConnection;

    public SDKDataSource() {
    }

    public SDKDataSource(
           java.lang.String attrSchema,
           java.lang.String descr,
           java.lang.Integer dialectId,
           java.lang.String driver,
           java.lang.Integer id,
           java.lang.String jndi,
           java.lang.String label,
           java.lang.Integer multiSchema,
           java.lang.String name,
           java.lang.String pwd,
           java.lang.String urlConnection) {
           this.attrSchema = attrSchema;
           this.descr = descr;
           this.dialectId = dialectId;
           this.driver = driver;
           this.id = id;
           this.jndi = jndi;
           this.label = label;
           this.multiSchema = multiSchema;
           this.name = name;
           this.pwd = pwd;
           this.urlConnection = urlConnection;
    }


    /**
     * Gets the attrSchema value for this SDKDataSource.
     * 
     * @return attrSchema
     */
    public java.lang.String getAttrSchema() {
        return attrSchema;
    }


    /**
     * Sets the attrSchema value for this SDKDataSource.
     * 
     * @param attrSchema
     */
    public void setAttrSchema(java.lang.String attrSchema) {
        this.attrSchema = attrSchema;
    }


    /**
     * Gets the descr value for this SDKDataSource.
     * 
     * @return descr
     */
    public java.lang.String getDescr() {
        return descr;
    }


    /**
     * Sets the descr value for this SDKDataSource.
     * 
     * @param descr
     */
    public void setDescr(java.lang.String descr) {
        this.descr = descr;
    }


    /**
     * Gets the dialectId value for this SDKDataSource.
     * 
     * @return dialectId
     */
    public java.lang.Integer getDialectId() {
        return dialectId;
    }


    /**
     * Sets the dialectId value for this SDKDataSource.
     * 
     * @param dialectId
     */
    public void setDialectId(java.lang.Integer dialectId) {
        this.dialectId = dialectId;
    }


    /**
     * Gets the driver value for this SDKDataSource.
     * 
     * @return driver
     */
    public java.lang.String getDriver() {
        return driver;
    }


    /**
     * Sets the driver value for this SDKDataSource.
     * 
     * @param driver
     */
    public void setDriver(java.lang.String driver) {
        this.driver = driver;
    }


    /**
     * Gets the id value for this SDKDataSource.
     * 
     * @return id
     */
    public java.lang.Integer getId() {
        return id;
    }


    /**
     * Sets the id value for this SDKDataSource.
     * 
     * @param id
     */
    public void setId(java.lang.Integer id) {
        this.id = id;
    }


    /**
     * Gets the jndi value for this SDKDataSource.
     * 
     * @return jndi
     */
    public java.lang.String getJndi() {
        return jndi;
    }


    /**
     * Sets the jndi value for this SDKDataSource.
     * 
     * @param jndi
     */
    public void setJndi(java.lang.String jndi) {
        this.jndi = jndi;
    }


    /**
     * Gets the label value for this SDKDataSource.
     * 
     * @return label
     */
    public java.lang.String getLabel() {
        return label;
    }


    /**
     * Sets the label value for this SDKDataSource.
     * 
     * @param label
     */
    public void setLabel(java.lang.String label) {
        this.label = label;
    }


    /**
     * Gets the multiSchema value for this SDKDataSource.
     * 
     * @return multiSchema
     */
    public java.lang.Integer getMultiSchema() {
        return multiSchema;
    }


    /**
     * Sets the multiSchema value for this SDKDataSource.
     * 
     * @param multiSchema
     */
    public void setMultiSchema(java.lang.Integer multiSchema) {
        this.multiSchema = multiSchema;
    }


    /**
     * Gets the name value for this SDKDataSource.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKDataSource.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the pwd value for this SDKDataSource.
     * 
     * @return pwd
     */
    public java.lang.String getPwd() {
        return pwd;
    }


    /**
     * Sets the pwd value for this SDKDataSource.
     * 
     * @param pwd
     */
    public void setPwd(java.lang.String pwd) {
        this.pwd = pwd;
    }


    /**
     * Gets the urlConnection value for this SDKDataSource.
     * 
     * @return urlConnection
     */
    public java.lang.String getUrlConnection() {
        return urlConnection;
    }


    /**
     * Sets the urlConnection value for this SDKDataSource.
     * 
     * @param urlConnection
     */
    public void setUrlConnection(java.lang.String urlConnection) {
        this.urlConnection = urlConnection;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKDataSource)) return false;
        SDKDataSource other = (SDKDataSource) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attrSchema==null && other.getAttrSchema()==null) || 
             (this.attrSchema!=null &&
              this.attrSchema.equals(other.getAttrSchema()))) &&
            ((this.descr==null && other.getDescr()==null) || 
             (this.descr!=null &&
              this.descr.equals(other.getDescr()))) &&
            ((this.dialectId==null && other.getDialectId()==null) || 
             (this.dialectId!=null &&
              this.dialectId.equals(other.getDialectId()))) &&
            ((this.driver==null && other.getDriver()==null) || 
             (this.driver!=null &&
              this.driver.equals(other.getDriver()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.jndi==null && other.getJndi()==null) || 
             (this.jndi!=null &&
              this.jndi.equals(other.getJndi()))) &&
            ((this.label==null && other.getLabel()==null) || 
             (this.label!=null &&
              this.label.equals(other.getLabel()))) &&
            ((this.multiSchema==null && other.getMultiSchema()==null) || 
             (this.multiSchema!=null &&
              this.multiSchema.equals(other.getMultiSchema()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.pwd==null && other.getPwd()==null) || 
             (this.pwd!=null &&
              this.pwd.equals(other.getPwd()))) &&
            ((this.urlConnection==null && other.getUrlConnection()==null) || 
             (this.urlConnection!=null &&
              this.urlConnection.equals(other.getUrlConnection())));
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
        if (getAttrSchema() != null) {
            _hashCode += getAttrSchema().hashCode();
        }
        if (getDescr() != null) {
            _hashCode += getDescr().hashCode();
        }
        if (getDialectId() != null) {
            _hashCode += getDialectId().hashCode();
        }
        if (getDriver() != null) {
            _hashCode += getDriver().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getJndi() != null) {
            _hashCode += getJndi().hashCode();
        }
        if (getLabel() != null) {
            _hashCode += getLabel().hashCode();
        }
        if (getMultiSchema() != null) {
            _hashCode += getMultiSchema().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getPwd() != null) {
            _hashCode += getPwd().hashCode();
        }
        if (getUrlConnection() != null) {
            _hashCode += getUrlConnection().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKDataSource.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.datasources.sdk.spagobi.eng.it", "SDKDataSource"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attrSchema");
        elemField.setXmlName(new javax.xml.namespace.QName("", "attrSchema"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descr");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descr"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dialectId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dialectId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("driver");
        elemField.setXmlName(new javax.xml.namespace.QName("", "driver"));
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
        elemField.setFieldName("jndi");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jndi"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("label");
        elemField.setXmlName(new javax.xml.namespace.QName("", "label"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("multiSchema");
        elemField.setXmlName(new javax.xml.namespace.QName("", "multiSchema"));
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
        elemField.setFieldName("pwd");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pwd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("urlConnection");
        elemField.setXmlName(new javax.xml.namespace.QName("", "urlConnection"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
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
