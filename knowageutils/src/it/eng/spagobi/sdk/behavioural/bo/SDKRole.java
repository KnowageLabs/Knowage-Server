/**
 * SDKRole.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.behavioural.bo;

public class SDKRole  implements java.io.Serializable {
    private java.lang.String code;

    private java.lang.String descr;

    private java.lang.Integer extRoleId;

    private java.lang.String name;

    private java.lang.String organization;

    private java.lang.String roleTypeCd;

    private java.lang.Integer roleTypeId;

    public SDKRole() {
    }

    public SDKRole(
           java.lang.String code,
           java.lang.String descr,
           java.lang.Integer extRoleId,
           java.lang.String name,
           java.lang.String organization,
           java.lang.String roleTypeCd,
           java.lang.Integer roleTypeId) {
           this.code = code;
           this.descr = descr;
           this.extRoleId = extRoleId;
           this.name = name;
           this.organization = organization;
           this.roleTypeCd = roleTypeCd;
           this.roleTypeId = roleTypeId;
    }


    /**
     * Gets the code value for this SDKRole.
     * 
     * @return code
     */
    public java.lang.String getCode() {
        return code;
    }


    /**
     * Sets the code value for this SDKRole.
     * 
     * @param code
     */
    public void setCode(java.lang.String code) {
        this.code = code;
    }


    /**
     * Gets the descr value for this SDKRole.
     * 
     * @return descr
     */
    public java.lang.String getDescr() {
        return descr;
    }


    /**
     * Sets the descr value for this SDKRole.
     * 
     * @param descr
     */
    public void setDescr(java.lang.String descr) {
        this.descr = descr;
    }


    /**
     * Gets the extRoleId value for this SDKRole.
     * 
     * @return extRoleId
     */
    public java.lang.Integer getExtRoleId() {
        return extRoleId;
    }


    /**
     * Sets the extRoleId value for this SDKRole.
     * 
     * @param extRoleId
     */
    public void setExtRoleId(java.lang.Integer extRoleId) {
        this.extRoleId = extRoleId;
    }


    /**
     * Gets the name value for this SDKRole.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKRole.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the organization value for this SDKRole.
     * 
     * @return organization
     */
    public java.lang.String getOrganization() {
        return organization;
    }


    /**
     * Sets the organization value for this SDKRole.
     * 
     * @param organization
     */
    public void setOrganization(java.lang.String organization) {
        this.organization = organization;
    }


    /**
     * Gets the roleTypeCd value for this SDKRole.
     * 
     * @return roleTypeCd
     */
    public java.lang.String getRoleTypeCd() {
        return roleTypeCd;
    }


    /**
     * Sets the roleTypeCd value for this SDKRole.
     * 
     * @param roleTypeCd
     */
    public void setRoleTypeCd(java.lang.String roleTypeCd) {
        this.roleTypeCd = roleTypeCd;
    }


    /**
     * Gets the roleTypeId value for this SDKRole.
     * 
     * @return roleTypeId
     */
    public java.lang.Integer getRoleTypeId() {
        return roleTypeId;
    }


    /**
     * Sets the roleTypeId value for this SDKRole.
     * 
     * @param roleTypeId
     */
    public void setRoleTypeId(java.lang.Integer roleTypeId) {
        this.roleTypeId = roleTypeId;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKRole)) return false;
        SDKRole other = (SDKRole) obj;
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
            ((this.descr==null && other.getDescr()==null) || 
             (this.descr!=null &&
              this.descr.equals(other.getDescr()))) &&
            ((this.extRoleId==null && other.getExtRoleId()==null) || 
             (this.extRoleId!=null &&
              this.extRoleId.equals(other.getExtRoleId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.organization==null && other.getOrganization()==null) || 
             (this.organization!=null &&
              this.organization.equals(other.getOrganization()))) &&
            ((this.roleTypeCd==null && other.getRoleTypeCd()==null) || 
             (this.roleTypeCd!=null &&
              this.roleTypeCd.equals(other.getRoleTypeCd()))) &&
            ((this.roleTypeId==null && other.getRoleTypeId()==null) || 
             (this.roleTypeId!=null &&
              this.roleTypeId.equals(other.getRoleTypeId())));
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
        if (getDescr() != null) {
            _hashCode += getDescr().hashCode();
        }
        if (getExtRoleId() != null) {
            _hashCode += getExtRoleId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getOrganization() != null) {
            _hashCode += getOrganization().hashCode();
        }
        if (getRoleTypeCd() != null) {
            _hashCode += getRoleTypeCd().hashCode();
        }
        if (getRoleTypeId() != null) {
            _hashCode += getRoleTypeId().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKRole.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.behavioural.sdk.spagobi.eng.it", "SDKRole"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("code");
        elemField.setXmlName(new javax.xml.namespace.QName("", "code"));
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
        elemField.setFieldName("extRoleId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "extRoleId"));
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
        elemField.setFieldName("organization");
        elemField.setXmlName(new javax.xml.namespace.QName("", "organization"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("roleTypeCd");
        elemField.setXmlName(new javax.xml.namespace.QName("", "roleTypeCd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("roleTypeId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "roleTypeId"));
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
