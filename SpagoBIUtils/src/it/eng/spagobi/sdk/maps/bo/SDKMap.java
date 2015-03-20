/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.maps.bo;

public class SDKMap  implements java.io.Serializable {
    private java.lang.Integer binId;

    private java.lang.String descr;

    private java.lang.String format;

    private java.lang.Integer mapId;

    private java.lang.String name;

    private it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures;

    private java.lang.String url;

    public SDKMap() {
    }

    public SDKMap(
           java.lang.Integer binId,
           java.lang.String descr,
           java.lang.String format,
           java.lang.Integer mapId,
           java.lang.String name,
           it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures,
           java.lang.String url) {
           this.binId = binId;
           this.descr = descr;
           this.format = format;
           this.mapId = mapId;
           this.name = name;
           this.sdkFeatures = sdkFeatures;
           this.url = url;
    }


    /**
     * Gets the binId value for this SDKMap.
     * 
     * @return binId
     */
    public java.lang.Integer getBinId() {
        return binId;
    }


    /**
     * Sets the binId value for this SDKMap.
     * 
     * @param binId
     */
    public void setBinId(java.lang.Integer binId) {
        this.binId = binId;
    }


    /**
     * Gets the descr value for this SDKMap.
     * 
     * @return descr
     */
    public java.lang.String getDescr() {
        return descr;
    }


    /**
     * Sets the descr value for this SDKMap.
     * 
     * @param descr
     */
    public void setDescr(java.lang.String descr) {
        this.descr = descr;
    }


    /**
     * Gets the format value for this SDKMap.
     * 
     * @return format
     */
    public java.lang.String getFormat() {
        return format;
    }


    /**
     * Sets the format value for this SDKMap.
     * 
     * @param format
     */
    public void setFormat(java.lang.String format) {
        this.format = format;
    }


    /**
     * Gets the mapId value for this SDKMap.
     * 
     * @return mapId
     */
    public java.lang.Integer getMapId() {
        return mapId;
    }


    /**
     * Sets the mapId value for this SDKMap.
     * 
     * @param mapId
     */
    public void setMapId(java.lang.Integer mapId) {
        this.mapId = mapId;
    }


    /**
     * Gets the name value for this SDKMap.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKMap.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the sdkFeatures value for this SDKMap.
     * 
     * @return sdkFeatures
     */
    public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getSdkFeatures() {
        return sdkFeatures;
    }


    /**
     * Sets the sdkFeatures value for this SDKMap.
     * 
     * @param sdkFeatures
     */
    public void setSdkFeatures(it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures) {
        this.sdkFeatures = sdkFeatures;
    }


    /**
     * Gets the url value for this SDKMap.
     * 
     * @return url
     */
    public java.lang.String getUrl() {
        return url;
    }


    /**
     * Sets the url value for this SDKMap.
     * 
     * @param url
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKMap)) return false;
        SDKMap other = (SDKMap) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.binId==null && other.getBinId()==null) || 
             (this.binId!=null &&
              this.binId.equals(other.getBinId()))) &&
            ((this.descr==null && other.getDescr()==null) || 
             (this.descr!=null &&
              this.descr.equals(other.getDescr()))) &&
            ((this.format==null && other.getFormat()==null) || 
             (this.format!=null &&
              this.format.equals(other.getFormat()))) &&
            ((this.mapId==null && other.getMapId()==null) || 
             (this.mapId!=null &&
              this.mapId.equals(other.getMapId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.sdkFeatures==null && other.getSdkFeatures()==null) || 
             (this.sdkFeatures!=null &&
              java.util.Arrays.equals(this.sdkFeatures, other.getSdkFeatures()))) &&
            ((this.url==null && other.getUrl()==null) || 
             (this.url!=null &&
              this.url.equals(other.getUrl())));
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
        if (getBinId() != null) {
            _hashCode += getBinId().hashCode();
        }
        if (getDescr() != null) {
            _hashCode += getDescr().hashCode();
        }
        if (getFormat() != null) {
            _hashCode += getFormat().hashCode();
        }
        if (getMapId() != null) {
            _hashCode += getMapId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getSdkFeatures() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSdkFeatures());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSdkFeatures(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUrl() != null) {
            _hashCode += getUrl().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKMap.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.maps.sdk.spagobi.eng.it", "SDKMap"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("binId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "binId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descr");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descr"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("format");
        elemField.setXmlName(new javax.xml.namespace.QName("", "format"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mapId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mapId"));
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
        elemField.setFieldName("sdkFeatures");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sdkFeatures"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.maps.sdk.spagobi.eng.it", "SDKFeature"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url"));
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
