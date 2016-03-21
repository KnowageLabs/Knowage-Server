/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaRelationship;
import it.eng.knowage.meta.generator.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractJpaRelationship implements IJpaRelationship {
	AbstractJpaTable jpaTable;
	String cardinality;
	boolean bidirectional;
	
	
	
	public boolean isBidirectional() {
		return bidirectional;
	}

	public String getCardinality() {
		return cardinality;
	}

	public boolean isOneToMany() {
		return JpaRelationship.ONE_TO_MANY.equals( cardinality ) || JpaRelationship.OPTIONAL_ONE_TO_MANY.equals( cardinality ) ||  JpaRelationship.ONE_TO_OPTIONAL_MANY.equals( cardinality );
	}
	
	public boolean isOneToOne() {
		return JpaRelationship.ONE_TO_ONE.equals( cardinality ) || JpaRelationship.OPTIONAL_ONE_TO_ONE.equals( cardinality ) ||  JpaRelationship.ONE_TO_OPTIONAL_ONE.equals( cardinality );
	}
	
	public boolean isManyToMany() {
		return JpaRelationship.MANY_TO_MANY.equals( cardinality );
	}
	
	public String getDescription()  {
		if (isBidirectional()){
			return "Bidirectional " +  getCardinality() + " association to " + getReferencedTable().getClassName();	
		} else {
			return  getCardinality() + " association to " + getReferencedTable().getClassName();
		}
	}
	
	public AbstractJpaTable getJpaTable() {
		return jpaTable;
	}
	
	/**
	 * @return the name of the metod GETTER
	 */
	public String getGetter(String par) {
		return "get"+StringUtils.initUpper(par);
	}
	/**
	 * @return the name of the metod SETTER
	 */
	public String getSetter(String par) {
		return "set"+StringUtils.initUpper(par);
	}
	
	/**
	 * TODO ... implementazione da verificare.!!!!!!!!
	 * @param role
	 * @return
	 */
	private String genCascades() {
		
        //List cascades = StringUtil.strToList(role.getCascade(), ',', true);
		List<String> cascades = new ArrayList<String>();
		cascades.add("all");

        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        int i = 0;
        for(int n = cascades.size(); i < n; i++)
        {
            String cascade = (String)cascades.get(i);
            String enumStr;
            if(cascade.equals("all"))
                enumStr = "CascadeType.ALL";
            else
            if(cascade.equals("persist"))
                enumStr = "CascadeType.PERSIST";
            else
            if(cascade.equals("merge"))
                enumStr = "CascadeType.MERGE";
            else
            if(cascade.equals("remove"))
            {
                enumStr = "CascadeType.REMOVE";
            } else
            {
                enumStr = "CascadeType.REFRESH";
            }
            if(i != 0)
                buffer.append(", ");
            buffer.append(enumStr);
        }

        buffer.append('}');
        return buffer.toString();
    }
	
	/**
	 * TODO ... da verificare
	 * @param s
	 * @param memberName
	 * @param memberValue
	 * @param quote
	 * @return
	 */
	    private String appendAnnotation(String s, String memberName, String memberValue, boolean quote)
	    {
	        if(memberValue == null || memberValue.length() == 0)
	            return s;
	        StringBuffer buffer = new StringBuffer(s);
	        if(buffer.length() != 0)
	            buffer.append(", ");
	        buffer.append(memberName);
	        buffer.append('=');
	        if(quote)
	            buffer.append('"');
	        buffer.append(memberValue);
	        if(quote)
	            buffer.append('"');
	        return buffer.toString();
	    }
	    
	    /**
	     * 
	     * @return
	     */
	    public String getGenCascadesWithAnnotation(){
	    	return appendAnnotation("", "cascade",genCascades(),false);
	    }
	    public String getGenCascadesWithAnnotation(String parameter){
	    	return appendAnnotation(parameter, "cascade",genCascades(),false);
	    }    
	    public String getGenFetchWithAnnotation(String s){
	    	return appendAnnotation(s, "fetch",genFetch(),false);
	    }    

	    
	    private String genFetch()
	    {
	        String fetch = jpaTable.getDefaultFetch();
	        if(fetch == null || "defaultFetch".equals(fetch))
	            return "";
	        if(fetch.equals("lazy"))
	            return "FetchType.LAZY";
	        else
	            return "FetchType.EAGER";
	    }
	    
	    /**
		 * @return
		 */
		public String getCollectionType(){
			
			return "java.util.Set";
		}
		
		//protected abstract String getOppositeRoleName();
		
		public String getOppositeWithAnnotation(){
			return appendAnnotation("", "mappedBy", getOppositeRoleName(),true);

		}
		
		public String getOppositeOneToOneWithAnnotation(){
			return appendAnnotation("", "mappedBy", getBidirectionalPropertyName(),true);

		}

	
}
