/**
 * SpagoBIUserProfile.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.security.bo;

public class SpagoBIUserProfile  implements java.io.Serializable {
    private java.util.HashMap attributes;

    private java.lang.String[] functions;

    private java.lang.Boolean isSuperadmin;

    private java.lang.String organization;

    private java.lang.String[] roles;

    private java.lang.String uniqueIdentifier;

    private java.lang.String userId;

    private java.lang.String userName;

    public SpagoBIUserProfile() {
    }

    public SpagoBIUserProfile(
           java.util.HashMap attributes,
           java.lang.String[] functions,
           java.lang.Boolean isSuperadmin,
           java.lang.String organization,
           java.lang.String[] roles,
           java.lang.String uniqueIdentifier,
           java.lang.String userId,
           java.lang.String userName) {
           this.attributes = attributes;
           this.functions = functions;
           this.isSuperadmin = isSuperadmin;
           this.organization = organization;
           this.roles = roles;
           this.uniqueIdentifier = uniqueIdentifier;
           this.userId = userId;
           this.userName = userName;
    }


    /**
     * Gets the attributes value for this SpagoBIUserProfile.
     * 
     * @return attributes
     */
    public java.util.HashMap getAttributes() {
        return attributes;
    }


    /**
     * Sets the attributes value for this SpagoBIUserProfile.
     * 
     * @param attributes
     */
    public void setAttributes(java.util.HashMap attributes) {
        this.attributes = attributes;
    }


    /**
     * Gets the functions value for this SpagoBIUserProfile.
     * 
     * @return functions
     */
    public java.lang.String[] getFunctions() {
        return functions;
    }


    /**
     * Sets the functions value for this SpagoBIUserProfile.
     * 
     * @param functions
     */
    public void setFunctions(java.lang.String[] functions) {
        this.functions = functions;
    }


    /**
     * Gets the isSuperadmin value for this SpagoBIUserProfile.
     * 
     * @return isSuperadmin
     */
    public java.lang.Boolean getIsSuperadmin() {
        return isSuperadmin;
    }


    /**
     * Sets the isSuperadmin value for this SpagoBIUserProfile.
     * 
     * @param isSuperadmin
     */
    public void setIsSuperadmin(java.lang.Boolean isSuperadmin) {
        this.isSuperadmin = isSuperadmin;
    }


    /**
     * Gets the organization value for this SpagoBIUserProfile.
     * 
     * @return organization
     */
    public java.lang.String getOrganization() {
        return organization;
    }


    /**
     * Sets the organization value for this SpagoBIUserProfile.
     * 
     * @param organization
     */
    public void setOrganization(java.lang.String organization) {
        this.organization = organization;
    }


    /**
     * Gets the roles value for this SpagoBIUserProfile.
     * 
     * @return roles
     */
    public java.lang.String[] getRoles() {
        return roles;
    }


    /**
     * Sets the roles value for this SpagoBIUserProfile.
     * 
     * @param roles
     */
    public void setRoles(java.lang.String[] roles) {
        this.roles = roles;
    }


    /**
     * Gets the uniqueIdentifier value for this SpagoBIUserProfile.
     * 
     * @return uniqueIdentifier
     */
    public java.lang.String getUniqueIdentifier() {
        return uniqueIdentifier;
    }


    /**
     * Sets the uniqueIdentifier value for this SpagoBIUserProfile.
     * 
     * @param uniqueIdentifier
     */
    public void setUniqueIdentifier(java.lang.String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    /**
     * Gets the userId value for this SpagoBIUserProfile.
     * 
     * @return userId
     */
    public java.lang.String getUserId() {
        return userId;
    }


    /**
     * Sets the userId value for this SpagoBIUserProfile.
     * 
     * @param userId
     */
    public void setUserId(java.lang.String userId) {
        this.userId = userId;
    }


    /**
     * Gets the userName value for this SpagoBIUserProfile.
     * 
     * @return userName
     */
    public java.lang.String getUserName() {
        return userName;
    }


    /**
     * Sets the userName value for this SpagoBIUserProfile.
     * 
     * @param userName
     */
    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SpagoBIUserProfile)) return false;
        SpagoBIUserProfile other = (SpagoBIUserProfile) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.attributes==null && other.getAttributes()==null) || 
             (this.attributes!=null &&
              this.attributes.equals(other.getAttributes()))) &&
            ((this.functions==null && other.getFunctions()==null) || 
             (this.functions!=null &&
              java.util.Arrays.equals(this.functions, other.getFunctions()))) &&
            ((this.isSuperadmin==null && other.getIsSuperadmin()==null) || 
             (this.isSuperadmin!=null &&
              this.isSuperadmin.equals(other.getIsSuperadmin()))) &&
            ((this.organization==null && other.getOrganization()==null) || 
             (this.organization!=null &&
              this.organization.equals(other.getOrganization()))) &&
            ((this.roles==null && other.getRoles()==null) || 
             (this.roles!=null &&
              java.util.Arrays.equals(this.roles, other.getRoles()))) &&
            ((this.uniqueIdentifier==null && other.getUniqueIdentifier()==null) || 
             (this.uniqueIdentifier!=null &&
              this.uniqueIdentifier.equals(other.getUniqueIdentifier()))) &&
            ((this.userId==null && other.getUserId()==null) || 
             (this.userId!=null &&
              this.userId.equals(other.getUserId()))) &&
            ((this.userName==null && other.getUserName()==null) || 
             (this.userName!=null &&
              this.userName.equals(other.getUserName())));
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
        if (getAttributes() != null) {
            _hashCode += getAttributes().hashCode();
        }
        if (getFunctions() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFunctions());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFunctions(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getIsSuperadmin() != null) {
            _hashCode += getIsSuperadmin().hashCode();
        }
        if (getOrganization() != null) {
            _hashCode += getOrganization().hashCode();
        }
        if (getRoles() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRoles());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRoles(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getUniqueIdentifier() != null) {
            _hashCode += getUniqueIdentifier().hashCode();
        }
        if (getUserId() != null) {
            _hashCode += getUserId().hashCode();
        }
        if (getUserName() != null) {
            _hashCode += getUserName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SpagoBIUserProfile.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.security.services.spagobi.eng.it", "SpagoBIUserProfile"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attributes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "attributes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("functions");
        elemField.setXmlName(new javax.xml.namespace.QName("", "functions"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("isSuperadmin");
        elemField.setXmlName(new javax.xml.namespace.QName("", "isSuperadmin"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("organization");
        elemField.setXmlName(new javax.xml.namespace.QName("", "organization"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("roles");
        elemField.setXmlName(new javax.xml.namespace.QName("", "roles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uniqueIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("", "uniqueIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userName"));
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
