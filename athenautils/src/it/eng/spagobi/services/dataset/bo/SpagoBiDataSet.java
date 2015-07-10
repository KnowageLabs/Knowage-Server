/**
 * SpagoBiDataSet.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.services.dataset.bo;

public class SpagoBiDataSet  implements java.io.Serializable {
    private boolean _public;

    private boolean active;

    private java.lang.Integer categoryId;

    private java.lang.String configuration;

    private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource;

    private it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading;

    private java.lang.String description;

    private int dsId;

    private java.lang.String dsMetadata;

    private java.lang.String flatTableName;

    private java.lang.String label;

    private java.lang.String name;

    private boolean numRows;

    private java.lang.String organization;

    private java.lang.String parameters;

    private java.lang.String persistTableName;

    private boolean persisted;
    
    private boolean scheduled;

    private java.lang.String pivotColumnName;

    private java.lang.String pivotColumnValue;

    private java.lang.String pivotRowName;

    private java.lang.Integer scopeId;
    

	private java.lang.String scopeCd;
    
    private java.lang.String owner;

    private java.lang.Integer transformerId;

    private java.lang.String type;

    private int versionNum;
    
	public SpagoBiDataSet() {
    }

    public SpagoBiDataSet(
           boolean _public,
           boolean active,
           java.lang.Integer categoryId,
           java.lang.String configuration,
           it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource,
           it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading,
           java.lang.String description,
           int dsId,
           java.lang.String dsMetadata,
           java.lang.String flatTableName,
           java.lang.String label,
           java.lang.String name,
           boolean numRows,
           java.lang.String organization,
           java.lang.String parameters,
           java.lang.String persistTableName,
           boolean persisted,
           boolean scheduled,
           java.lang.String pivotColumnName,
           java.lang.String pivotColumnValue,
           java.lang.String pivotRowName,
           java.lang.Integer scopeId,
           java.lang.String scopeCd,
           java.lang.String owner,
           java.lang.Integer transformerId,
           java.lang.String type,
           int versionNum) {
           this._public = _public;
           this.active = active;
           this.categoryId = categoryId;
           this.configuration = configuration;
           this.dataSource = dataSource;
           this.dataSourceForReading = dataSourceForReading;
           this.description = description;
           this.dsId = dsId;
           this.dsMetadata = dsMetadata;
           this.flatTableName = flatTableName;
           this.label = label;
           this.name = name;
           this.numRows = numRows;
           this.organization = organization;
           this.parameters = parameters;
           this.persistTableName = persistTableName;
           this.persisted = persisted;
           this.scheduled = scheduled;
           this.pivotColumnName = pivotColumnName;
           this.pivotColumnValue = pivotColumnValue;
           this.pivotRowName = pivotRowName;
           this.scopeId = scopeId;
           this.transformerId = transformerId;
           this.type = type;
           this.versionNum = versionNum;
           this.scopeCd= scopeCd;
           this.owner= owner;
    }

    public java.lang.String getScopeCd() {
		return scopeCd;
	}

	public void setScopeCd(java.lang.String scopeCd) {
		this.scopeCd = scopeCd;
	}

	public java.lang.String getOwner() {
		return owner;
	}

	public void setOwner(java.lang.String owner) {
		this.owner = owner;
	}

    /**
     * Gets the _public value for this SpagoBiDataSet.
     * 
     * @return _public
     */
    public boolean is_public() {
        return _public;
    }


    /**
     * Sets the _public value for this SpagoBiDataSet.
     * 
     * @param _public
     */
    public void set_public(boolean _public) {
        this._public = _public;
    }


    /**
     * Gets the active value for this SpagoBiDataSet.
     * 
     * @return active
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets the active value for this SpagoBiDataSet.
     * 
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * Gets the categoryId value for this SpagoBiDataSet.
     * 
     * @return categoryId
     */
    public java.lang.Integer getCategoryId() {
        return categoryId;
    }


    /**
     * Sets the categoryId value for this SpagoBiDataSet.
     * 
     * @param categoryId
     */
    public void setCategoryId(java.lang.Integer categoryId) {
        this.categoryId = categoryId;
    }


    /**
     * Gets the configuration value for this SpagoBiDataSet.
     * 
     * @return configuration
     */
    public java.lang.String getConfiguration() {
        return configuration;
    }


    /**
     * Sets the configuration value for this SpagoBiDataSet.
     * 
     * @param configuration
     */
    public void setConfiguration(java.lang.String configuration) {
        this.configuration = configuration;
    }


    /**
     * Gets the dataSource value for this SpagoBiDataSet.
     * 
     * @return dataSource
     */
    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSource() {
        return dataSource;
    }


    /**
     * Sets the dataSource value for this SpagoBiDataSet.
     * 
     * @param dataSource
     */
    public void setDataSource(it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Gets the dataSourceForReading value for this SpagoBiDataSet.
     * 
     * @return dataSourceForReading
     */
    public it.eng.spagobi.services.datasource.bo.SpagoBiDataSource getDataSourceForReading() {
        return dataSourceForReading;
    }


    /**
     * Sets the dataSourceForReading value for this SpagoBiDataSet.
     * 
     * @param dataSourceForReading
     */
    public void setDataSourceForReading(it.eng.spagobi.services.datasource.bo.SpagoBiDataSource dataSourceForReading) {
        this.dataSourceForReading = dataSourceForReading;
    }


    /**
     * Gets the description value for this SpagoBiDataSet.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this SpagoBiDataSet.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the dsId value for this SpagoBiDataSet.
     * 
     * @return dsId
     */
    public int getDsId() {
        return dsId;
    }


    /**
     * Sets the dsId value for this SpagoBiDataSet.
     * 
     * @param dsId
     */
    public void setDsId(int dsId) {
        this.dsId = dsId;
    }


    /**
     * Gets the dsMetadata value for this SpagoBiDataSet.
     * 
     * @return dsMetadata
     */
    public java.lang.String getDsMetadata() {
        return dsMetadata;
    }


    /**
     * Sets the dsMetadata value for this SpagoBiDataSet.
     * 
     * @param dsMetadata
     */
    public void setDsMetadata(java.lang.String dsMetadata) {
        this.dsMetadata = dsMetadata;
    }


    /**
     * Gets the flatTableName value for this SpagoBiDataSet.
     * 
     * @return flatTableName
     */
    public java.lang.String getFlatTableName() {
        return flatTableName;
    }


    /**
     * Sets the flatTableName value for this SpagoBiDataSet.
     * 
     * @param flatTableName
     */
    public void setFlatTableName(java.lang.String flatTableName) {
        this.flatTableName = flatTableName;
    }


    /**
     * Gets the label value for this SpagoBiDataSet.
     * 
     * @return label
     */
    public java.lang.String getLabel() {
        return label;
    }


    /**
     * Sets the label value for this SpagoBiDataSet.
     * 
     * @param label
     */
    public void setLabel(java.lang.String label) {
        this.label = label;
    }


    /**
     * Gets the name value for this SpagoBiDataSet.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SpagoBiDataSet.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the numRows value for this SpagoBiDataSet.
     * 
     * @return numRows
     */
    public boolean isNumRows() {
        return numRows;
    }


    /**
     * Sets the numRows value for this SpagoBiDataSet.
     * 
     * @param numRows
     */
    public void setNumRows(boolean numRows) {
        this.numRows = numRows;
    }


    /**
     * Gets the organization value for this SpagoBiDataSet.
     * 
     * @return organization
     */
    public java.lang.String getOrganization() {
        return organization;
    }


    /**
     * Sets the organization value for this SpagoBiDataSet.
     * 
     * @param organization
     */
    public void setOrganization(java.lang.String organization) {
        this.organization = organization;
    }


    /**
     * Gets the parameters value for this SpagoBiDataSet.
     * 
     * @return parameters
     */
    public java.lang.String getParameters() {
        return parameters;
    }


    /**
     * Sets the parameters value for this SpagoBiDataSet.
     * 
     * @param parameters
     */
    public void setParameters(java.lang.String parameters) {
        this.parameters = parameters;
    }


    /**
     * Gets the persistTableName value for this SpagoBiDataSet.
     * 
     * @return persistTableName
     */
    public java.lang.String getPersistTableName() {
        return persistTableName;
    }


    /**
     * Sets the persistTableName value for this SpagoBiDataSet.
     * 
     * @param persistTableName
     */
    public void setPersistTableName(java.lang.String persistTableName) {
        this.persistTableName = persistTableName;
    }


    /**
     * Gets the persisted value for this SpagoBiDataSet.
     * 
     * @return persisted
     */
    public boolean isPersisted() {
        return persisted;
    }


    /**
     * Sets the persisted value for this SpagoBiDataSet.
     * 
     * @param persisted
     */
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
    
    /**
	 * @return the scheduled
	 */
	public boolean isScheduled() {
		return scheduled;
	}

	/**
	 * @param scheduled
	 *            the scheduled to set
	 */
	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}


    /**
     * Gets the pivotColumnName value for this SpagoBiDataSet.
     * 
     * @return pivotColumnName
     */
    public java.lang.String getPivotColumnName() {
        return pivotColumnName;
    }


    /**
     * Sets the pivotColumnName value for this SpagoBiDataSet.
     * 
     * @param pivotColumnName
     */
    public void setPivotColumnName(java.lang.String pivotColumnName) {
        this.pivotColumnName = pivotColumnName;
    }


    /**
     * Gets the pivotColumnValue value for this SpagoBiDataSet.
     * 
     * @return pivotColumnValue
     */
    public java.lang.String getPivotColumnValue() {
        return pivotColumnValue;
    }


    /**
     * Sets the pivotColumnValue value for this SpagoBiDataSet.
     * 
     * @param pivotColumnValue
     */
    public void setPivotColumnValue(java.lang.String pivotColumnValue) {
        this.pivotColumnValue = pivotColumnValue;
    }


    /**
     * Gets the pivotRowName value for this SpagoBiDataSet.
     * 
     * @return pivotRowName
     */
    public java.lang.String getPivotRowName() {
        return pivotRowName;
    }


    /**
     * Sets the pivotRowName value for this SpagoBiDataSet.
     * 
     * @param pivotRowName
     */
    public void setPivotRowName(java.lang.String pivotRowName) {
        this.pivotRowName = pivotRowName;
    }


    /**
     * Gets the scopeId value for this SpagoBiDataSet.
     * 
     * @return scopeId
     */
    public java.lang.Integer getScopeId() {
        return scopeId;
    }


    /**
     * Sets the scopeId value for this SpagoBiDataSet.
     * 
     * @param scopeId
     */
    public void setScopeId(java.lang.Integer scopeId) {
        this.scopeId = scopeId;
    }


    /**
     * Gets the transformerId value for this SpagoBiDataSet.
     * 
     * @return transformerId
     */
    public java.lang.Integer getTransformerId() {
        return transformerId;
    }


    /**
     * Sets the transformerId value for this SpagoBiDataSet.
     * 
     * @param transformerId
     */
    public void setTransformerId(java.lang.Integer transformerId) {
        this.transformerId = transformerId;
    }


    /**
     * Gets the type value for this SpagoBiDataSet.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this SpagoBiDataSet.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the versionNum value for this SpagoBiDataSet.
     * 
     * @return versionNum
     */
    public int getVersionNum() {
        return versionNum;
    }


    /**
     * Sets the versionNum value for this SpagoBiDataSet.
     * 
     * @param versionNum
     */
    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }
	
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SpagoBiDataSet)) return false;
        SpagoBiDataSet other = (SpagoBiDataSet) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this._public == other.is_public() &&
            this.active == other.isActive() &&
            ((this.categoryId==null && other.getCategoryId()==null) || 
             (this.categoryId!=null &&
              this.categoryId.equals(other.getCategoryId()))) &&
            ((this.configuration==null && other.getConfiguration()==null) || 
             (this.configuration!=null &&
              this.configuration.equals(other.getConfiguration()))) &&
            ((this.dataSource==null && other.getDataSource()==null) || 
             (this.dataSource!=null &&
              this.dataSource.equals(other.getDataSource()))) &&
            ((this.dataSourceForReading==null && other.getDataSourceForReading()==null) || 
             (this.dataSourceForReading!=null &&
              this.dataSourceForReading.equals(other.getDataSourceForReading()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            this.dsId == other.getDsId() &&
            ((this.dsMetadata==null && other.getDsMetadata()==null) || 
             (this.dsMetadata!=null &&
              this.dsMetadata.equals(other.getDsMetadata()))) &&
            ((this.flatTableName==null && other.getFlatTableName()==null) || 
             (this.flatTableName!=null &&
              this.flatTableName.equals(other.getFlatTableName()))) &&
            ((this.label==null && other.getLabel()==null) || 
             (this.label!=null &&
              this.label.equals(other.getLabel()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            this.numRows == other.isNumRows() &&
            ((this.organization==null && other.getOrganization()==null) || 
             (this.organization!=null &&
              this.organization.equals(other.getOrganization()))) &&
            ((this.parameters==null && other.getParameters()==null) || 
             (this.parameters!=null &&
              this.parameters.equals(other.getParameters()))) &&
            ((this.persistTableName==null && other.getPersistTableName()==null) || 
             (this.persistTableName!=null &&
              this.persistTableName.equals(other.getPersistTableName()))) &&
            this.persisted == other.isPersisted() && this.scheduled == other.isScheduled() &&
            ((this.pivotColumnName==null && other.getPivotColumnName()==null) || 
             (this.pivotColumnName!=null &&
              this.pivotColumnName.equals(other.getPivotColumnName()))) &&
            ((this.pivotColumnValue==null && other.getPivotColumnValue()==null) || 
             (this.pivotColumnValue!=null &&
              this.pivotColumnValue.equals(other.getPivotColumnValue()))) &&
            ((this.pivotRowName==null && other.getPivotRowName()==null) || 
             (this.pivotRowName!=null &&
              this.pivotRowName.equals(other.getPivotRowName()))) &&
            ((this.scopeId==null && other.getScopeId()==null) || 
             (this.scopeId!=null &&
              this.scopeId.equals(other.getScopeId()))) &&
            ((this.transformerId==null && other.getTransformerId()==null) || 
             (this.transformerId!=null &&
              this.transformerId.equals(other.getTransformerId()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            this.versionNum == other.getVersionNum();
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
        _hashCode += (is_public() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isActive() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getCategoryId() != null) {
            _hashCode += getCategoryId().hashCode();
        }
        if (getConfiguration() != null) {
            _hashCode += getConfiguration().hashCode();
        }
        if (getDataSource() != null) {
            _hashCode += getDataSource().hashCode();
        }
        if (getDataSourceForReading() != null) {
            _hashCode += getDataSourceForReading().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        _hashCode += getDsId();
        if (getDsMetadata() != null) {
            _hashCode += getDsMetadata().hashCode();
        }
        if (getFlatTableName() != null) {
            _hashCode += getFlatTableName().hashCode();
        }
        if (getLabel() != null) {
            _hashCode += getLabel().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        _hashCode += (isNumRows() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getOrganization() != null) {
            _hashCode += getOrganization().hashCode();
        }
        if (getParameters() != null) {
            _hashCode += getParameters().hashCode();
        }
        if (getPersistTableName() != null) {
            _hashCode += getPersistTableName().hashCode();
        }
        _hashCode += (isPersisted() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        _hashCode += (isScheduled() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getPivotColumnName() != null) {
            _hashCode += getPivotColumnName().hashCode();
        }
        if (getPivotColumnValue() != null) {
            _hashCode += getPivotColumnValue().hashCode();
        }
        if (getPivotRowName() != null) {
            _hashCode += getPivotRowName().hashCode();
        }
        if (getScopeId() != null) {
            _hashCode += getScopeId().hashCode();
        }
        if (getTransformerId() != null) {
            _hashCode += getTransformerId().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        _hashCode += getVersionNum();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SpagoBiDataSet.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.dataset.services.spagobi.eng.it", "SpagoBiDataSet"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_public");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_public"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("active");
        elemField.setXmlName(new javax.xml.namespace.QName("", "active"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "categoryId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("configuration");
        elemField.setXmlName(new javax.xml.namespace.QName("", "configuration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataSource");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dataSource"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasource.services.spagobi.eng.it", "SpagoBiDataSource"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataSourceForReading");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dataSourceForReading"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasource.services.spagobi.eng.it", "SpagoBiDataSource"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dsId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dsId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dsMetadata");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dsMetadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("flatTableName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "flatTableName"));
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
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numRows");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numRows"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("organization");
        elemField.setXmlName(new javax.xml.namespace.QName("", "organization"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameters");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("persistTableName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "persistTableName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("persisted");
        elemField.setXmlName(new javax.xml.namespace.QName("", "persisted"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField.setFieldName("scheduled");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scheduled"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pivotColumnName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pivotColumnName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pivotColumnValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pivotColumnValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pivotRowName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "pivotRowName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scopeId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scopeId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transformerId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "transformerId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionNum");
        elemField.setXmlName(new javax.xml.namespace.QName("", "versionNum"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
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
