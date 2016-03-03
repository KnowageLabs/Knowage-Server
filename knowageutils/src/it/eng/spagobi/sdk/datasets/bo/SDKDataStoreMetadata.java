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

package it.eng.spagobi.sdk.datasets.bo;

public class SDKDataStoreMetadata  implements java.io.Serializable {
    private it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata[] fieldsMetadata;

    private java.util.HashMap properties;

    public SDKDataStoreMetadata() {
    }

    public SDKDataStoreMetadata(
           it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata[] fieldsMetadata,
           java.util.HashMap properties) {
           this.fieldsMetadata = fieldsMetadata;
           this.properties = properties;
    }


    /**
     * Gets the fieldsMetadata value for this SDKDataStoreMetadata.
     * 
     * @return fieldsMetadata
     */
    public it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata[] getFieldsMetadata() {
        return fieldsMetadata;
    }


    /**
     * Sets the fieldsMetadata value for this SDKDataStoreMetadata.
     * 
     * @param fieldsMetadata
     */
    public void setFieldsMetadata(it.eng.spagobi.sdk.datasets.bo.SDKDataStoreFieldMetadata[] fieldsMetadata) {
        this.fieldsMetadata = fieldsMetadata;
    }


    /**
     * Gets the properties value for this SDKDataStoreMetadata.
     * 
     * @return properties
     */
    public java.util.HashMap getProperties() {
        return properties;
    }


    /**
     * Sets the properties value for this SDKDataStoreMetadata.
     * 
     * @param properties
     */
    public void setProperties(java.util.HashMap properties) {
        this.properties = properties;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKDataStoreMetadata)) return false;
        SDKDataStoreMetadata other = (SDKDataStoreMetadata) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.fieldsMetadata==null && other.getFieldsMetadata()==null) || 
             (this.fieldsMetadata!=null &&
              java.util.Arrays.equals(this.fieldsMetadata, other.getFieldsMetadata()))) &&
            ((this.properties==null && other.getProperties()==null) || 
             (this.properties!=null &&
              this.properties.equals(other.getProperties())));
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
        if (getFieldsMetadata() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFieldsMetadata());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFieldsMetadata(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getProperties() != null) {
            _hashCode += getProperties().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKDataStoreMetadata.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreMetadata"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fieldsMetadata");
        elemField.setXmlName(new javax.xml.namespace.QName("", "fieldsMetadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataStoreFieldMetadata"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("properties");
        elemField.setXmlName(new javax.xml.namespace.QName("", "properties"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
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
