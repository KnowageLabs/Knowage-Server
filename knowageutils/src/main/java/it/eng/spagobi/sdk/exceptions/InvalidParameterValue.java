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

package it.eng.spagobi.sdk.exceptions;

public class InvalidParameterValue  extends org.apache.axis.AxisFault  implements java.io.Serializable {
    private java.lang.String parameterFormat;

    private java.lang.String parameterName;

    private java.lang.String parameterType;

    private java.lang.String wrongParameterValue;

    public InvalidParameterValue() {
    }

    public InvalidParameterValue(
           java.lang.String parameterFormat,
           java.lang.String parameterName,
           java.lang.String parameterType,
           java.lang.String wrongParameterValue) {
        this.parameterFormat = parameterFormat;
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        this.wrongParameterValue = wrongParameterValue;
    }


    /**
     * Gets the parameterFormat value for this InvalidParameterValue.
     * 
     * @return parameterFormat
     */
    public java.lang.String getParameterFormat() {
        return parameterFormat;
    }


    /**
     * Sets the parameterFormat value for this InvalidParameterValue.
     * 
     * @param parameterFormat
     */
    public void setParameterFormat(java.lang.String parameterFormat) {
        this.parameterFormat = parameterFormat;
    }


    /**
     * Gets the parameterName value for this InvalidParameterValue.
     * 
     * @return parameterName
     */
    public java.lang.String getParameterName() {
        return parameterName;
    }


    /**
     * Sets the parameterName value for this InvalidParameterValue.
     * 
     * @param parameterName
     */
    public void setParameterName(java.lang.String parameterName) {
        this.parameterName = parameterName;
    }


    /**
     * Gets the parameterType value for this InvalidParameterValue.
     * 
     * @return parameterType
     */
    public java.lang.String getParameterType() {
        return parameterType;
    }


    /**
     * Sets the parameterType value for this InvalidParameterValue.
     * 
     * @param parameterType
     */
    public void setParameterType(java.lang.String parameterType) {
        this.parameterType = parameterType;
    }


    /**
     * Gets the wrongParameterValue value for this InvalidParameterValue.
     * 
     * @return wrongParameterValue
     */
    public java.lang.String getWrongParameterValue() {
        return wrongParameterValue;
    }


    /**
     * Sets the wrongParameterValue value for this InvalidParameterValue.
     * 
     * @param wrongParameterValue
     */
    public void setWrongParameterValue(java.lang.String wrongParameterValue) {
        this.wrongParameterValue = wrongParameterValue;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InvalidParameterValue)) return false;
        InvalidParameterValue other = (InvalidParameterValue) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.parameterFormat==null && other.getParameterFormat()==null) || 
             (this.parameterFormat!=null &&
              this.parameterFormat.equals(other.getParameterFormat()))) &&
            ((this.parameterName==null && other.getParameterName()==null) || 
             (this.parameterName!=null &&
              this.parameterName.equals(other.getParameterName()))) &&
            ((this.parameterType==null && other.getParameterType()==null) || 
             (this.parameterType!=null &&
              this.parameterType.equals(other.getParameterType()))) &&
            ((this.wrongParameterValue==null && other.getWrongParameterValue()==null) || 
             (this.wrongParameterValue!=null &&
              this.wrongParameterValue.equals(other.getWrongParameterValue())));
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
        if (getParameterFormat() != null) {
            _hashCode += getParameterFormat().hashCode();
        }
        if (getParameterName() != null) {
            _hashCode += getParameterName().hashCode();
        }
        if (getParameterType() != null) {
            _hashCode += getParameterType().hashCode();
        }
        if (getWrongParameterValue() != null) {
            _hashCode += getWrongParameterValue().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InvalidParameterValue.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://exceptions.sdk.spagobi.eng.it", "InvalidParameterValue"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterFormat");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameterFormat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameterName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameterType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameterType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("wrongParameterValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "wrongParameterValue"));
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


    /**
     * Writes the exception data to the faultDetails
     */
    public void writeDetails(javax.xml.namespace.QName qname, org.apache.axis.encoding.SerializationContext context) throws java.io.IOException {
        context.serialize(qname, null, this);
    }
}
