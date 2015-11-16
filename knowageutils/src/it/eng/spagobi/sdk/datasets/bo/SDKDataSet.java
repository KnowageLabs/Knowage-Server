/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.datasets.bo;

public class SDKDataSet  implements java.io.Serializable {
    private java.lang.String description;

   // private java.lang.String fileName;

    private java.lang.Integer id;
    
    private java.lang.Integer versionNum;
    
    private java.lang.Boolean active;

    private java.lang.Boolean _public;
    
 //   private java.lang.String javaClassName;

 //   private java.lang.Integer jdbcDataSourceId;

 //   private java.lang.String jdbcQuery;
    
 //   private java.lang.String jdbcQueryScript;
    
//    private java.lang.String jdbcQueryScriptLanguage;

    private java.lang.String label;

    private java.lang.String name;

    private java.lang.Boolean numberingRows;

    private it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters;

    private java.lang.String pivotColumnName;

    private java.lang.String pivotColumnValue;

    private java.lang.String pivotRowName;

  //  private java.lang.String scriptLanguage;

  //  private java.lang.String scriptText;

    private java.lang.String type;
    
    private java.lang.String configuration;

    private java.lang.String transformer;

    private java.lang.String category;

    private java.lang.String organization;

    
  //  private java.lang.String jsonQuery;

  //  private java.lang.String datamarts;

  //  private java.lang.String webServiceAddress;

  //  private java.lang.String webServiceOperation;
    
  //  private java.lang.String customData;
    

    public SDKDataSet() {
    }

    public SDKDataSet(
           java.lang.String description,
           java.lang.String fileName,
           java.lang.Integer id,
           java.lang.Integer versionNum,
           java.lang.Boolean active,
           java.lang.Boolean _public,           
           java.lang.String javaClassName,
           java.lang.Integer jdbcDataSourceId,
           java.lang.String jdbcQuery,
           java.lang.String jdbcQueryScript,
           java.lang.String jdbcQueryScriptLanguage,
           java.lang.String label,
           java.lang.String name,
           java.lang.Boolean numberingRows,
           it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters,
           java.lang.String pivotColumnName,
           java.lang.String pivotColumnValue,
           java.lang.String pivotRowName,
           java.lang.String scriptLanguage,
           java.lang.String scriptText,
           java.lang.String type,
           java.lang.String configuration,
           java.lang.String userIn,
           java.lang.String userUp,
           java.lang.String userDe,
           java.lang.String sbiVersionIn,
           java.lang.String sbiVersionUp,
           java.lang.String sbiVersionDe,
           java.lang.String metaVersion,
           java.lang.String organization,
           java.lang.String transformer,
           java.lang.String category,
           java.lang.String jsonQuery,
           java.lang.String datamarts,
           java.lang.String webServiceAddress,
           java.lang.String webServiceOperation,
           java.lang.String customData) {
           this.description = description;
          // this.fileName = fileName;
           this.id = id;
           this.versionNum = versionNum;
           this.active = active;
           this._public = _public;           
          // this.javaClassName = javaClassName;
          // this.jdbcDataSourceId = jdbcDataSourceId;
           // this.jdbcQuery = jdbcQuery;
           //  this.jdbcQueryScript = jdbcQueryScript;
           //this.jdbcQueryScriptLanguage = jdbcQueryScriptLanguage;
           this.label = label;
           this.name = name;
           this.numberingRows = numberingRows;
           this.parameters = parameters;
           this.pivotColumnName = pivotColumnName;
           this.pivotColumnValue = pivotColumnValue;
           this.pivotRowName = pivotRowName;
           //this.scriptLanguage = scriptLanguage;
           //this.scriptText = scriptText;
           this.type = type;
           this.configuration = configuration;
           this.transformer = transformer;
           this.category = category;
           this.organization = organization;
           
           //this.jsonQuery = jsonQuery;
           //this.datamarts = datamarts;
           //this.webServiceAddress = webServiceAddress;
           //this.webServiceOperation = webServiceOperation;
           //this.customData = customData;
    }


    /**
     * Gets the description value for this SDKDataSet.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this SDKDataSet.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the fileName value for this SDKDataSet.
     * 
     * @return fileName
    
    public java.lang.String getFileName() {
        return fileName;
    }
    */

    /**
     * Sets the fileName value for this SDKDataSet.
     * 
     * @param fileName
     *
    public void setFileName(java.lang.String fileName) {
        this.fileName = fileName;
    }
    */

    /**
     * Gets the id value for this SDKDataSet.
     * 
     * @return id
     */
    public java.lang.Integer getId() {
        return id;
    }


    /**
	 * @return the versionNum
	 */
	public java.lang.Integer getVersionNum() {
		return versionNum;
	}

	/**
	 * @param versionNum the versionNum to set
	 */
	public void setVersionNum(java.lang.Integer versionNum) {
		this.versionNum = versionNum;
	}

	/**
	 * @return the active
	 */
	public java.lang.Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(java.lang.Boolean active) {
		this.active = active;
	}

	
	
	public java.lang.Boolean get_public() {
		return _public;
	}

	public void set_public(java.lang.Boolean _public) {
		this._public = _public;
	}

	/**
     * Sets the id value for this SDKDataSet.
     * 
     * @param id
     */
    public void setId(java.lang.Integer id) {
        this.id = id;
    }


    /**
     * Gets the javaClassName value for this SDKDataSet.
     * 
     * @return javaClassName
     *
    public java.lang.String getJavaClassName() {
        return javaClassName;
    }
    */

    /**
     * Sets the javaClassName value for this SDKDataSet.
     * 
     * @param javaClassName
     *
    public void setJavaClassName(java.lang.String javaClassName) {
        this.javaClassName = javaClassName;
    }
*/

    /**
     * Gets the jdbcDataSourceId value for this SDKDataSet.
     * 
     * @return jdbcDataSourceId
     *
    public java.lang.Integer getJdbcDataSourceId() {
        return jdbcDataSourceId;
    }
*/

    /**
     * Sets the jdbcDataSourceId value for this SDKDataSet.
     * 
     * @param jdbcDataSourceId
     *
    public void setJdbcDataSourceId(java.lang.Integer jdbcDataSourceId) {
        this.jdbcDataSourceId = jdbcDataSourceId;
    }
*/

    /**
     * Gets the jdbcQuery value for this SDKDataSet.
     * 
     * @return jdbcQuery
     *
    public java.lang.String getJdbcQuery() {
        return jdbcQuery;
    }
*/

    /**
     * Sets the jdbcQuery value for this SDKDataSet.
     * 
     * @param jdbcQuery
     *
    public void setJdbcQuery(java.lang.String jdbcQuery) {
        this.jdbcQuery = jdbcQuery;
    }
    *
    public java.lang.String getJdbcQueryScript() {
		return jdbcQueryScript;
	}
    *
	public void setJdbcQueryScript(java.lang.String jdbcQueryScript) {
		this.jdbcQueryScript = jdbcQueryScript;
	}

	public java.lang.String getJdbcQueryScriptLanguage() {
		return jdbcQueryScriptLanguage;
	}

	public void setJdbcQueryScriptLanguage(java.lang.String jdbcQueryScriptLanguage) {
		this.jdbcQueryScriptLanguage = jdbcQueryScriptLanguage;
	}
	*/
	/**
     * Gets the label value for this SDKDataSet.
     * 
     * @return label
     */
    public java.lang.String getLabel() {
        return label;
    }


    /**
     * Sets the label value for this SDKDataSet.
     * 
     * @param label
     */
    public void setLabel(java.lang.String label) {
        this.label = label;
    }


    /**
     * Gets the name value for this SDKDataSet.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SDKDataSet.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the numberingRows value for this SDKDataSet.
     * 
     * @return numberingRows
     */
    public java.lang.Boolean getNumberingRows() {
        return numberingRows;
    }


    /**
     * Sets the numberingRows value for this SDKDataSet.
     * 
     * @param numberingRows
     */
    public void setNumberingRows(java.lang.Boolean numberingRows) {
        this.numberingRows = numberingRows;
    }


    /**
     * Gets the parameters value for this SDKDataSet.
     * 
     * @return parameters
     */
    public it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] getParameters() {
        return parameters;
    }


    /**
     * Sets the parameters value for this SDKDataSet.
     * 
     * @param parameters
     */
    public void setParameters(it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter[] parameters) {
        this.parameters = parameters;
    }


    /**
     * Gets the pivotColumnName value for this SDKDataSet.
     * 
     * @return pivotColumnName
     */
    public java.lang.String getPivotColumnName() {
        return pivotColumnName;
    }


    /**
     * Sets the pivotColumnName value for this SDKDataSet.
     * 
     * @param pivotColumnName
     */
    public void setPivotColumnName(java.lang.String pivotColumnName) {
        this.pivotColumnName = pivotColumnName;
    }


    /**
     * Gets the pivotColumnValue value for this SDKDataSet.
     * 
     * @return pivotColumnValue
     */
    public java.lang.String getPivotColumnValue() {
        return pivotColumnValue;
    }


    /**
     * Sets the pivotColumnValue value for this SDKDataSet.
     * 
     * @param pivotColumnValue
     */
    public void setPivotColumnValue(java.lang.String pivotColumnValue) {
        this.pivotColumnValue = pivotColumnValue;
    }


    /**
     * Gets the pivotRowName value for this SDKDataSet.
     * 
     * @return pivotRowName
     */
    public java.lang.String getPivotRowName() {
        return pivotRowName;
    }


    /**
     * Sets the pivotRowName value for this SDKDataSet.
     * 
     * @param pivotRowName
     */
    public void setPivotRowName(java.lang.String pivotRowName) {
        this.pivotRowName = pivotRowName;
    }


    /**
     * Gets the scriptLanguage value for this SDKDataSet.
     * 
     * @return scriptLanguage
     *
    public java.lang.String getScriptLanguage() {
        return scriptLanguage;
    }
    */

    /**
     * Sets the scriptLanguage value for this SDKDataSet.
     * 
     * @param scriptLanguage
     *
    public void setScriptLanguage(java.lang.String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }


    /**
     * Gets the scriptText value for this SDKDataSet.
     * 
     * @return scriptText
     *
    public java.lang.String getScriptText() {
        return scriptText;
    }


    /**
     * Sets the scriptText value for this SDKDataSet.
     * 
     * @param scriptText
     *
    public void setScriptText(java.lang.String scriptText) {
        this.scriptText = scriptText;
    }
    */

    /**
     * Gets the type value for this SDKDataSet.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this SDKDataSet.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }

    /**
     * Gets the transformer value for this SDKDataSet.
     * 
     * @return transformer
     */
    public java.lang.String getTransformer() {
        return transformer;
    }


    /**
     * Sets the transformer value for this SDKDataSet.
     * 
     * @param transformer
     */
    public void setTransformer(java.lang.String transformer) {
        this.transformer = transformer;
    }


    /**
     * Gets the category value for this SDKDataSet.
     * 
     * @return category
     */
    public java.lang.String getCategory() {
        return category;
    }


    /**
     * Sets the category value for this SDKDataSet.
     * 
     * @param category
     */
    public void setCategory(java.lang.String category) {
        this.category = category;
    }


    /**
     * Gets the jsonQuery value for this SDKDataSet.
     * 
     * @return jsonQuery
     *
    public java.lang.String getJsonQuery() {
        return jsonQuery;
    }


    /**
     * Sets the jsonQuery value for this SDKDataSet.
     * 
     * @param jsonQuery
     *
    public void setJsonQuery(java.lang.String jsonQuery) {
        this.jsonQuery = jsonQuery;
    }


    /**
     * Gets the datamarts value for this SDKDataSet.
     * 
     * @return datamarts
     *
    public java.lang.String getDatamarts() {
        return datamarts;
    }


    /**
     * Sets the datamarts value for this SDKDataSet.
     * 
     * @param datamarts
     *
    public void setDatamarts(java.lang.String datamarts) {
        this.datamarts = datamarts;
    }


    /**
     * Gets the webServiceAddress value for this SDKDataSet.
     * 
     * @return webServiceAddress
     *
    public java.lang.String getWebServiceAddress() {
        return webServiceAddress;
    }


    /**
     * Sets the webServiceAddress value for this SDKDataSet.
     * 
     * @param webServiceAddress
     *
    public void setWebServiceAddress(java.lang.String webServiceAddress) {
        this.webServiceAddress = webServiceAddress;
    }


    /**
     * Gets the webServiceOperation value for this SDKDataSet.
     * 
     * @return webServiceOperation
     *
    public java.lang.String getWebServiceOperation() {
        return webServiceOperation;
    }


    /**
     * Sets the webServiceOperation value for this SDKDataSet.
     * 
     * @param webServiceOperation
     *
    public void setWebServiceOperation(java.lang.String webServiceOperation) {
        this.webServiceOperation = webServiceOperation;
    }

    
    public java.lang.String getCustomData() {
		return customData;
	}

	public void setCustomData(java.lang.String customData) {
		this.customData = customData;
	}
	*/
	/**
	 * @return the configuration
	 */
	public java.lang.String getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(java.lang.String configuration) {
		this.configuration = configuration;
	}




	private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SDKDataSet)) return false;
        SDKDataSet other = (SDKDataSet) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
     /*       ((this.fileName==null && other.getFileName()==null) || 
             (this.fileName!=null &&
              this.fileName.equals(other.getFileName()))) && */
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.versionNum==null && other.getVersionNum()==null) || 
             (this.versionNum!=null &&
              this.versionNum.equals(other.getVersionNum()))) &&
            ((this.active==null && other.getActive()==null) || 
             (this.active!=null &&
              this.active.equals(other.getActive()))) &&
            ((this._public==null && other.get_public()==null) || 
             (this._public!=null &&
              this._public.equals(other.get_public()))) &&
              /*  ((this.javaClassName==null && other.getJavaClassName()==null) || 
             (this.javaClassName!=null &&
              this.javaClassName.equals(other.getJavaClassName()))) &&
            ((this.jdbcDataSourceId==null && other.getJdbcDataSourceId()==null) || 
             (this.jdbcDataSourceId!=null &&
              this.jdbcDataSourceId.equals(other.getJdbcDataSourceId()))) &&
            ((this.jdbcQuery==null && other.getJdbcQuery()==null) || 
             (this.jdbcQuery!=null &&
              this.jdbcQuery.equals(other.getJdbcQuery()))) &&
            ((this.jdbcQueryScript==null && other.getJdbcQueryScript()==null) || 
             (this.jdbcQueryScript!=null &&
              this.jdbcQueryScript.equals(other.getJdbcQueryScript()))) &&
            ((this.jdbcQueryScriptLanguage==null && other.getJdbcQueryScriptLanguage()==null) || 
             (this.jdbcQueryScriptLanguage!=null &&
              this.jdbcQueryScriptLanguage.equals(other.getJdbcQueryScriptLanguage()))) && */
            ((this.label==null && other.getLabel()==null) || 
             (this.label!=null &&
              this.label.equals(other.getLabel()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.numberingRows==null && other.getNumberingRows()==null) || 
             (this.numberingRows!=null &&
              this.numberingRows.equals(other.getNumberingRows()))) &&
            ((this.parameters==null && other.getParameters()==null) || 
             (this.parameters!=null &&
              java.util.Arrays.equals(this.parameters, other.getParameters()))) &&
            ((this.pivotColumnName==null && other.getPivotColumnName()==null) || 
             (this.pivotColumnName!=null &&
              this.pivotColumnName.equals(other.getPivotColumnName()))) &&
            ((this.pivotColumnValue==null && other.getPivotColumnValue()==null) || 
             (this.pivotColumnValue!=null &&
              this.pivotColumnValue.equals(other.getPivotColumnValue()))) &&
            ((this.pivotRowName==null && other.getPivotRowName()==null) || 
             (this.pivotRowName!=null &&
              this.pivotRowName.equals(other.getPivotRowName()))) &&
         /*   ((this.scriptLanguage==null && other.getScriptLanguage()==null) || 
             (this.scriptLanguage!=null &&
              this.scriptLanguage.equals(other.getScriptLanguage()))) &&
            ((this.scriptText==null && other.getScriptText()==null) || 
             (this.scriptText!=null &&
              this.scriptText.equals(other.getScriptText()))) && */
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.configuration==null && other.getConfiguration()==null) || 
             (this.configuration!=null &&
              this.configuration.equals(other.getConfiguration()))) &&                         
            ((this.transformer==null && other.getTransformer()==null) || 
             (this.transformer!=null &&
              this.transformer.equals(other.getTransformer()))) &&
              ((this.organization==null && other.getOrganization()==null) || 
                      (this.organization!=null &&
                       this.organization.equals(other.getOrganization()))) &&
              ((this.category==null && other.getCategory()==null) || 
             (this.category!=null &&
              this.category.equals(other.getCategory()))) /* &&
           ((this.jsonQuery==null && other.getJsonQuery()==null) || 
             (this.jsonQuery!=null &&
              this.jsonQuery.equals(other.getJsonQuery()))) &&
            ((this.datamarts==null && other.getDatamarts()==null) || 
             (this.datamarts!=null &&
              this.datamarts.equals(other.getDatamarts()))) &&
            ((this.webServiceAddress==null && other.getWebServiceAddress()==null) || 
             (this.webServiceAddress!=null &&
              this.webServiceAddress.equals(other.getWebServiceAddress()))) &&
              ((this.customData==null && other.getCustomData()==null) || 
                      (this.customData!=null &&
                       this.customData.equals(other.getCustomData()))) &&              
            ((this.webServiceOperation==null && other.getWebServiceOperation()==null) || 
             (this.webServiceOperation!=null &&
              this.webServiceOperation.equals(other.getWebServiceOperation())))*/
              ;
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
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
     /*   if (getFileName() != null) {
            _hashCode += getFileName().hashCode();
        }
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getJavaClassName() != null) {
            _hashCode += getJavaClassName().hashCode();
        }
        if (getJdbcDataSourceId() != null) {
            _hashCode += getJdbcDataSourceId().hashCode();
        }
        if (getJdbcQuery() != null) {
            _hashCode += getJdbcQuery().hashCode();
        }
        if (getJdbcQueryScript() != null) {
            _hashCode += getJdbcQueryScript().hashCode();
        }
        if (getJdbcQueryScriptLanguage() != null) {
            _hashCode += getJdbcQueryScriptLanguage().hashCode();
        }*/
        if (getLabel() != null) {
            _hashCode += getLabel().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getNumberingRows() != null) {
            _hashCode += getNumberingRows().hashCode();
        }
        if (get_public() != null) {
            _hashCode += get_public().hashCode();
        }
        if (getParameters() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getParameters());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getParameters(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPivotColumnName() != null) {
            _hashCode += getPivotColumnName().hashCode();
        }
        if (getPivotColumnValue() != null) {
            _hashCode += getPivotColumnValue().hashCode();
        }
        if (getPivotRowName() != null) {
            _hashCode += getPivotRowName().hashCode();
        }
    /*    if (getScriptLanguage() != null) {
            _hashCode += getScriptLanguage().hashCode();
        }
        if (getScriptText() != null) {
            _hashCode += getScriptText().hashCode();
        }*/
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getConfiguration() != null) {
            _hashCode += getConfiguration().hashCode();
        }
        if (getTransformer() != null) {
            _hashCode += getTransformer().hashCode();
        }
        if (getCategory() != null) {
            _hashCode += getCategory().hashCode();
        }
        if (getOrganization() != null) {
            _hashCode += getOrganization().hashCode();
        }
     /*   if (getJsonQuery() != null) {
            _hashCode += getJsonQuery().hashCode();
        }
        if (getDatamarts() != null) {
            _hashCode += getDatamarts().hashCode();
        }
        if (getWebServiceAddress() != null) {
            _hashCode += getWebServiceAddress().hashCode();
        }
        if (getWebServiceOperation() != null) {
            _hashCode += getWebServiceOperation().hashCode();
        }
        if (getCustomData() != null) {
            _hashCode += getCustomData().hashCode();
        }        */
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SDKDataSet.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSet"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "fileName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("versionNum");
        elemField.setXmlName(new javax.xml.namespace.QName("", "versionNum"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("active");
        elemField.setXmlName(new javax.xml.namespace.QName("", "active"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_public");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_public"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("javaClassName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "javaClassName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jdbcDataSourceId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jdbcDataSourceId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jdbcQuery");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jdbcQuery"));
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
        elemField.setFieldName("numberingRows");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numberingRows"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameters");
        elemField.setXmlName(new javax.xml.namespace.QName("", "parameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://bo.datasets.sdk.spagobi.eng.it", "SDKDataSetParameter"));
        elemField.setNillable(true);
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
        elemField.setFieldName("scriptLanguage");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scriptLanguage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scriptText");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scriptText"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("configuration");
        elemField.setXmlName(new javax.xml.namespace.QName("", "configuration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userIn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userIn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userUp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userUp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userDe");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userDe"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sbiVersionIn");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sbiVersionIn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sbiVersionUp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sbiVersionUp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sbiVersionDe");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sbiVersionDe"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metaVersion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "metaVersion"));
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
        elemField.setFieldName("transformer");
        elemField.setXmlName(new javax.xml.namespace.QName("", "transformer"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("category");
        elemField.setXmlName(new javax.xml.namespace.QName("", "category"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jsonQuery");
        elemField.setXmlName(new javax.xml.namespace.QName("", "jsonQuery"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datamarts");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datamarts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("webServiceAddress");
        elemField.setXmlName(new javax.xml.namespace.QName("", "webServiceAddress"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("webServiceOperation");
        elemField.setXmlName(new javax.xml.namespace.QName("", "webServiceOperation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customData");
        elemField.setXmlName(new javax.xml.namespace.QName("", "customData"));
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
    
    

    public java.lang.String getOrganization() {
		return organization;
	}

	public void setOrganization(java.lang.String organization) {
		this.organization = organization;
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
