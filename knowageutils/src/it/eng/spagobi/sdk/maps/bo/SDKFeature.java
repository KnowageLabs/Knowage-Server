/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.maps.bo;

public class SDKFeature  implements java.io.Serializable {
    private java.lang.String descr;

    private java.lang.Integer featureId;

    private java.lang.String name;

    private java.lang.String svgGroup;

    private java.lang.String type;

    private java.lang.Boolean visibleFlag;

    public SDKFeature() {
    }

    public SDKFeature(
           java.lang.String descr,
           java.lang.Integer featureId,
           java.lang.String name,
           java.lang.String svgGroup,
           java.lang.String type,
           java.lang.Boolean visibleFlag) {
           this.descr = descr;
           this.featureId = featureId;
           this.name = name;
           this.svgGroup = svgGroup;
           this.type = type;
           this.visibleFlag = visibleFlag;
    }


    /**
     * Gets the descr value for this SDKFeature.
     * 
     * @return descr
     */
    public java.lang.String getDescr() {
        return descr;
    }


    /**
     * Sets the descr value for this SDKFeature.
     * 
     * @param descr
     */
    public void setDescr(java.lang.String descr) {
        this.descr = descr;
    }


    /**
     * Gets the featureId value for this SDKFeature.
     * 
     * @return featureId
     */
    public java.lang.Integer getFeatureId() {
        return featureId;
    }


    /**
     * Sets the featureId value for this SDKFeature.
     * 
     * @param featureId
     */
    public void setFeatureId(java.lang.Integer featureId) {
        this.featureId = featureId;
    }


    /**
     * Gets the name value for this SDKFeature.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKFeature.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the svgGroup value for this SDKFeature.
     * 
     * @return svgGroup
     */
    public java.lang.String getSvgGroup() {
        return svgGroup;
    }


    /**
     * Sets the svgGroup value for this SDKFeature.
     * 
     * @param svgGroup
     */
    public void setSvgGroup(java.lang.String svgGroup) {
        this.svgGroup = svgGroup;
    }


    /**
     * Gets the type value for this SDKFeature.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this SDKFeature.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the visibleFlag value for this SDKFeature.
     * 
     * @return visibleFlag
     */
    public java.lang.Boolean getVisibleFlag() {
        return visibleFlag;
    }


    /**
     * Sets the visibleFlag value for this SDKFeature.
     * 
     * @param visibleFlag
     */
    public void setVisibleFlag(java.lang.Boolean visibleFlag) {
        this.visibleFlag = visibleFlag;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKFeature)) return false;
        SDKFeature other = (SDKFeature) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.descr==null && other.getDescr()==null) || 
             (this.descr!=null &&
              this.descr.equals(other.getDescr()))) &&
            ((this.featureId==null && other.getFeatureId()==null) || 
             (this.featureId!=null &&
              this.featureId.equals(other.getFeatureId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.svgGroup==null && other.getSvgGroup()==null) || 
             (this.svgGroup!=null &&
              this.svgGroup.equals(other.getSvgGroup()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.visibleFlag==null && other.getVisibleFlag()==null) || 
             (this.visibleFlag!=null &&
              this.visibleFlag.equals(other.getVisibleFlag())));
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
        if (getDescr() != null) {
            _hashCode += getDescr().hashCode();
        }
        if (getFeatureId() != null) {
            _hashCode += getFeatureId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getSvgGroup() != null) {
            _hashCode += getSvgGroup().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getVisibleFlag() != null) {
            _hashCode += getVisibleFlag().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKFeature.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.maps.sdk.spagobi.eng.it", "SDKFeature"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descr");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descr"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("featureId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "featureId"));
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
        elemField.setFieldName("svgGroup");
        elemField.setXmlName(new javax.xml.namespace.QName("", "svgGroup"));
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
        elemField.setFieldName("visibleFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("", "visibleFlag"));
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
