/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.engines.bo;

public class SDKEngine  implements java.io.Serializable {
    private java.lang.String className;

    private java.lang.String description;

    private java.lang.String documentType;

    private java.lang.String driverClassName;

    private java.lang.String driverName;

    private java.lang.Integer encrypt;

    private java.lang.String engineType;

    private java.lang.Integer id;

    private java.lang.String label;

    private java.lang.String mainUrl;

    private java.lang.String name;

    private java.lang.String secondUrl;

    private java.lang.String url;

    private java.lang.Boolean useDataSet;

    private java.lang.Boolean useDataSource;

    public SDKEngine() {
    }

    public SDKEngine(
           java.lang.String className,
           java.lang.String description,
           java.lang.String documentType,
           java.lang.String driverClassName,
           java.lang.String driverName,
           java.lang.Integer encrypt,
           java.lang.String engineType,
           java.lang.Integer id,
           java.lang.String label,
           java.lang.String mainUrl,
           java.lang.String name,
           java.lang.String secondUrl,
           java.lang.String url,
           java.lang.Boolean useDataSet,
           java.lang.Boolean useDataSource) {
           this.className = className;
           this.description = description;
           this.documentType = documentType;
           this.driverClassName = driverClassName;
           this.driverName = driverName;
           this.encrypt = encrypt;
           this.engineType = engineType;
           this.id = id;
           this.label = label;
           this.mainUrl = mainUrl;
           this.name = name;
           this.secondUrl = secondUrl;
           this.url = url;
           this.useDataSet = useDataSet;
           this.useDataSource = useDataSource;
    }


    /**
     * Gets the className value for this SDKEngine.
     * 
     * @return className
     */
    public java.lang.String getClassName() {
        return className;
    }


    /**
     * Sets the className value for this SDKEngine.
     * 
     * @param className
     */
    public void setClassName(java.lang.String className) {
        this.className = className;
    }


    /**
     * Gets the description value for this SDKEngine.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this SDKEngine.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the documentType value for this SDKEngine.
     * 
     * @return documentType
     */
    public java.lang.String getDocumentType() {
        return documentType;
    }


    /**
     * Sets the documentType value for this SDKEngine.
     * 
     * @param documentType
     */
    public void setDocumentType(java.lang.String documentType) {
        this.documentType = documentType;
    }


    /**
     * Gets the driverClassName value for this SDKEngine.
     * 
     * @return driverClassName
     */
    public java.lang.String getDriverClassName() {
        return driverClassName;
    }


    /**
     * Sets the driverClassName value for this SDKEngine.
     * 
     * @param driverClassName
     */
    public void setDriverClassName(java.lang.String driverClassName) {
        this.driverClassName = driverClassName;
    }


    /**
     * Gets the driverName value for this SDKEngine.
     * 
     * @return driverName
     */
    public java.lang.String getDriverName() {
        return driverName;
    }


    /**
     * Sets the driverName value for this SDKEngine.
     * 
     * @param driverName
     */
    public void setDriverName(java.lang.String driverName) {
        this.driverName = driverName;
    }


    /**
     * Gets the encrypt value for this SDKEngine.
     * 
     * @return encrypt
     */
    public java.lang.Integer getEncrypt() {
        return encrypt;
    }


    /**
     * Sets the encrypt value for this SDKEngine.
     * 
     * @param encrypt
     */
    public void setEncrypt(java.lang.Integer encrypt) {
        this.encrypt = encrypt;
    }


    /**
     * Gets the engineType value for this SDKEngine.
     * 
     * @return engineType
     */
    public java.lang.String getEngineType() {
        return engineType;
    }


    /**
     * Sets the engineType value for this SDKEngine.
     * 
     * @param engineType
     */
    public void setEngineType(java.lang.String engineType) {
        this.engineType = engineType;
    }


    /**
     * Gets the id value for this SDKEngine.
     * 
     * @return id
     */
    public java.lang.Integer getId() {
        return id;
    }


    /**
     * Sets the id value for this SDKEngine.
     * 
     * @param id
     */
    public void setId(java.lang.Integer id) {
        this.id = id;
    }


    /**
     * Gets the label value for this SDKEngine.
     * 
     * @return label
     */
    public java.lang.String getLabel() {
        return label;
    }


    /**
     * Sets the label value for this SDKEngine.
     * 
     * @param label
     */
    public void setLabel(java.lang.String label) {
        this.label = label;
    }


    /**
     * Gets the mainUrl value for this SDKEngine.
     * 
     * @return mainUrl
     */
    public java.lang.String getMainUrl() {
        return mainUrl;
    }


    /**
     * Sets the mainUrl value for this SDKEngine.
     * 
     * @param mainUrl
     */
    public void setMainUrl(java.lang.String mainUrl) {
        this.mainUrl = mainUrl;
    }


    /**
     * Gets the name value for this SDKEngine.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKEngine.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the secondUrl value for this SDKEngine.
     * 
     * @return secondUrl
     */
    public java.lang.String getSecondUrl() {
        return secondUrl;
    }


    /**
     * Sets the secondUrl value for this SDKEngine.
     * 
     * @param secondUrl
     */
    public void setSecondUrl(java.lang.String secondUrl) {
        this.secondUrl = secondUrl;
    }


    /**
     * Gets the url value for this SDKEngine.
     * 
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this SDKEngine.
     * 
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }


    /**
     * Gets the useDataSet value for this SDKEngine.
     * 
     * @return useDataSet
     */
    public java.lang.Boolean getUseDataSet() {
        return useDataSet;
    }


    /**
     * Sets the useDataSet value for this SDKEngine.
     * 
     * @param useDataSet
     */
    public void setUseDataSet(java.lang.Boolean useDataSet) {
        this.useDataSet = useDataSet;
    }


    /**
     * Gets the useDataSource value for this SDKEngine.
     * 
     * @return useDataSource
     */
    public java.lang.Boolean getUseDataSource() {
        return useDataSource;
    }


    /**
     * Sets the useDataSource value for this SDKEngine.
     * 
     * @param useDataSource
     */
    public void setUseDataSource(java.lang.Boolean useDataSource) {
        this.useDataSource = useDataSource;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKEngine)) return false;
        SDKEngine other = (SDKEngine) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.className==null && other.getClassName()==null) || 
             (this.className!=null &&
              this.className.equals(other.getClassName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.documentType==null && other.getDocumentType()==null) || 
             (this.documentType!=null &&
              this.documentType.equals(other.getDocumentType()))) &&
            ((this.driverClassName==null && other.getDriverClassName()==null) || 
             (this.driverClassName!=null &&
              this.driverClassName.equals(other.getDriverClassName()))) &&
            ((this.driverName==null && other.getDriverName()==null) || 
             (this.driverName!=null &&
              this.driverName.equals(other.getDriverName()))) &&
            ((this.encrypt==null && other.getEncrypt()==null) || 
             (this.encrypt!=null &&
              this.encrypt.equals(other.getEncrypt()))) &&
            ((this.engineType==null && other.getEngineType()==null) || 
             (this.engineType!=null &&
              this.engineType.equals(other.getEngineType()))) &&
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.label==null && other.getLabel()==null) || 
             (this.label!=null &&
              this.label.equals(other.getLabel()))) &&
            ((this.mainUrl==null && other.getMainUrl()==null) || 
             (this.mainUrl!=null &&
              this.mainUrl.equals(other.getMainUrl()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.secondUrl==null && other.getSecondUrl()==null) || 
             (this.secondUrl!=null &&
              this.secondUrl.equals(other.getSecondUrl()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl()))) &&
            ((this.useDataSet==null && other.getUseDataSet()==null) || 
             (this.useDataSet!=null &&
              this.useDataSet.equals(other.getUseDataSet()))) &&
            ((this.useDataSource==null && other.getUseDataSource()==null) || 
             (this.useDataSource!=null &&
              this.useDataSource.equals(other.getUseDataSource())));
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
        if (getClassName() != null) {
            _hashCode += getClassName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getDocumentType() != null) {
            _hashCode += getDocumentType().hashCode();
        }
        if (getDriverClassName() != null) {
            _hashCode += getDriverClassName().hashCode();
        }
        if (getDriverName() != null) {
            _hashCode += getDriverName().hashCode();
        }
        if (getEncrypt() != null) {
            _hashCode += getEncrypt().hashCode();
        }
        if (getEngineType() != null) {
            _hashCode += getEngineType().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getLabel() != null) {
            _hashCode += getLabel().hashCode();
        }
        if (getMainUrl() != null) {
            _hashCode += getMainUrl().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getSecondUrl() != null) {
            _hashCode += getSecondUrl().hashCode();
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        if (getUseDataSet() != null) {
            _hashCode += getUseDataSet().hashCode();
        }
        if (getUseDataSource() != null) {
            _hashCode += getUseDataSource().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKEngine.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.engines.sdk.spagobi.eng.it", "SDKEngine"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("className");
        elemField.setXmlName(new javax.xml.namespace.QName("", "className"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "documentType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("driverClassName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "driverClassName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("driverName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "driverName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("encrypt");
        elemField.setXmlName(new javax.xml.namespace.QName("", "encrypt"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("engineType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "engineType"));
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
        elemField.setFieldName("label");
        elemField.setXmlName(new javax.xml.namespace.QName("", "label"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mainUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mainUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secondUrl");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secondUrl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("useDataSet");
        elemField.setXmlName(new javax.xml.namespace.QName("", "useDataSet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("useDataSource");
        elemField.setXmlName(new javax.xml.namespace.QName("", "useDataSource"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
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
