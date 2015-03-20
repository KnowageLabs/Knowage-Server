/**
 * SDKDomain.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.domains.bo;

public class SDKDomain  implements java.io.Serializable {
    private java.lang.String domainCd;

    private java.lang.String domainNm;

    private java.lang.String valueCd;

    private java.lang.String valueDs;

    private java.lang.Integer valueId;

    private java.lang.String valueNm;

    public SDKDomain() {
    }

    public SDKDomain(
           java.lang.String domainCd,
           java.lang.String domainNm,
           java.lang.String valueCd,
           java.lang.String valueDs,
           java.lang.Integer valueId,
           java.lang.String valueNm) {
           this.domainCd = domainCd;
           this.domainNm = domainNm;
           this.valueCd = valueCd;
           this.valueDs = valueDs;
           this.valueId = valueId;
           this.valueNm = valueNm;
    }


    /**
     * Gets the domainCd value for this SDKDomain.
     * 
     * @return domainCd
     */
    public java.lang.String getDomainCd() {
        return domainCd;
    }


    /**
     * Sets the domainCd value for this SDKDomain.
     * 
     * @param domainCd
     */
    public void setDomainCd(java.lang.String domainCd) {
        this.domainCd = domainCd;
    }


    /**
     * Gets the domainNm value for this SDKDomain.
     * 
     * @return domainNm
     */
    public java.lang.String getDomainNm() {
        return domainNm;
    }


    /**
     * Sets the domainNm value for this SDKDomain.
     * 
     * @param domainNm
     */
    public void setDomainNm(java.lang.String domainNm) {
        this.domainNm = domainNm;
    }


    /**
     * Gets the valueCd value for this SDKDomain.
     * 
     * @return valueCd
     */
    public java.lang.String getValueCd() {
        return valueCd;
    }


    /**
     * Sets the valueCd value for this SDKDomain.
     * 
     * @param valueCd
     */
    public void setValueCd(java.lang.String valueCd) {
        this.valueCd = valueCd;
    }


    /**
     * Gets the valueDs value for this SDKDomain.
     * 
     * @return valueDs
     */
    public java.lang.String getValueDs() {
        return valueDs;
    }


    /**
     * Sets the valueDs value for this SDKDomain.
     * 
     * @param valueDs
     */
    public void setValueDs(java.lang.String valueDs) {
        this.valueDs = valueDs;
    }


    /**
     * Gets the valueId value for this SDKDomain.
     * 
     * @return valueId
     */
    public java.lang.Integer getValueId() {
        return valueId;
    }


    /**
     * Sets the valueId value for this SDKDomain.
     * 
     * @param valueId
     */
    public void setValueId(java.lang.Integer valueId) {
        this.valueId = valueId;
    }


    /**
     * Gets the valueNm value for this SDKDomain.
     * 
     * @return valueNm
     */
    public java.lang.String getValueNm() {
        return valueNm;
    }


    /**
     * Sets the valueNm value for this SDKDomain.
     * 
     * @param valueNm
     */
    public void setValueNm(java.lang.String valueNm) {
        this.valueNm = valueNm;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKDomain)) return false;
        SDKDomain other = (SDKDomain) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.domainCd==null && other.getDomainCd()==null) || 
             (this.domainCd!=null &&
              this.domainCd.equals(other.getDomainCd()))) &&
            ((this.domainNm==null && other.getDomainNm()==null) || 
             (this.domainNm!=null &&
              this.domainNm.equals(other.getDomainNm()))) &&
            ((this.valueCd==null && other.getValueCd()==null) || 
             (this.valueCd!=null &&
              this.valueCd.equals(other.getValueCd()))) &&
            ((this.valueDs==null && other.getValueDs()==null) || 
             (this.valueDs!=null &&
              this.valueDs.equals(other.getValueDs()))) &&
            ((this.valueId==null && other.getValueId()==null) || 
             (this.valueId!=null &&
              this.valueId.equals(other.getValueId()))) &&
            ((this.valueNm==null && other.getValueNm()==null) || 
             (this.valueNm!=null &&
              this.valueNm.equals(other.getValueNm())));
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
        if (getDomainCd() != null) {
            _hashCode += getDomainCd().hashCode();
        }
        if (getDomainNm() != null) {
            _hashCode += getDomainNm().hashCode();
        }
        if (getValueCd() != null) {
            _hashCode += getValueCd().hashCode();
        }
        if (getValueDs() != null) {
            _hashCode += getValueDs().hashCode();
        }
        if (getValueId() != null) {
            _hashCode += getValueId().hashCode();
        }
        if (getValueNm() != null) {
            _hashCode += getValueNm().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKDomain.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.domains.sdk.spagobi.eng.it", "SDKDomain"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("domainCd");
        elemField.setXmlName(new javax.xml.namespace.QName("", "domainCd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("domainNm");
        elemField.setXmlName(new javax.xml.namespace.QName("", "domainNm"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueCd");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valueCd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueDs");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valueDs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valueId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueNm");
        elemField.setXmlName(new javax.xml.namespace.QName("", "valueNm"));
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
