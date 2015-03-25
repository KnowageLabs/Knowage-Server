/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.DataConnectionManager;
import it.eng.spago.dbaccess.SQLStatements;
import it.eng.spago.dbaccess.Utils;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.InformationDataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.util.ContextScooping;
import it.eng.spago.util.QueryExecutor;
import it.eng.spago.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.DialectResolver;
import org.hibernate.dialect.resolver.StandardDialectResolver;


public class DelegatedQueryExecutor extends QueryExecutor {
	static private Logger logger = Logger.getLogger(DelegatedQueryExecutor.class); 
	
    public static final String CREATE = "CREATE";
    public static final String READ = "READ";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";

    /**
     * Creates the command to execute dependent on request type.
     * 
     * @param dataConnection the data connection
     * @param type type of query to execute: CREATE, READ, UPDATE, DELETE
     * @param statement the statement
     * 
     * @return the SQL command
     */
    public static SQLCommand createStatementSql(final DataConnection dataConnection,
            final String statement, final String type) {
    	
        SQLCommand sqlCommand = null;
        if (type.equalsIgnoreCase("INSERT"))
            sqlCommand = dataConnection.createInsertCommand(statement);
        else if (type.equalsIgnoreCase("UPDATE"))
            sqlCommand = dataConnection.createUpdateCommand(statement);
        else if (type.equalsIgnoreCase("DELETE"))
            sqlCommand = dataConnection.createDeleteCommand(statement);
        else
            sqlCommand = dataConnection.createSelectCommand(statement);
        return sqlCommand;
    }

    /**
     * Opens the pool connection.
     * 
     * @param pool the pool
     * 
     * @return the data connection
     * 
     * @throws EMFInternalError the EMF internal error
     */
    public static DataConnection openConnection(final String pool) throws EMFInternalError {
        return DataConnectionManager.getInstance().getConnection(pool);
    }

    /**
     * Execs the commands: SQL SELECT, INSERT, DELETE, UPDATE.
     * The connection is gone by the connection pool.
     * 
     * @param requestContainer the request container
     * @param responseContainer the response container
     * @param pool pool's name
     * @param query the SourceBean that contains the configuration of the query
     * @param type type of query: CREATE, READ, UPDATE, DELETE
     * 
     * @return the object
     */
    public static Object executeQuery(final RequestContainer requestContainer,
            final ResponseContainer responseContainer, final String pool, final SourceBean query,
            final String type) {
	logger.debug("IN");
        Object result = null;
        DataConnection dataConnection = null;

        try {
            // Open the connection
            dataConnection = openConnection(pool);
            result = executeQuery(requestContainer, responseContainer, dataConnection, query, type);
        } // try
        catch (EMFInternalError ex) {
            logger.error((Exception) ex);
            responseContainer.getErrorHandler().addError(
                    new EMFInternalError(EMFErrorSeverity.ERROR, ex.getNativeException()));
        } // catch (Exception ex) try
        finally {
            Utils.releaseResources(dataConnection, null, null);
            logger.debug("OUT");
        } // finally try
        return result;
    } // public static Object executeQuery(RequestContainer

    /**
     * Execs the commands: SQL SELECT, INSERT, DELETE, UPDATE.
     * 
     * The connection is taken by parameter (for manually transactions)
     * 
     * @param requestContainer the request container
     * @param responseContainer the response container
     * @param dataConnection connection on db
     * @param query the SourceBean that contains the configuration
     * @param type type of query: CREATE, READ, UPDATE, DELETE
     * 
     * @return the object
     */
    public static Object executeQuery(final RequestContainer requestContainer,
            final ResponseContainer responseContainer, DataConnection dataConnection,
            final SourceBean query, final String type) {
	logger.debug("IN");
        Object result = null;

        try {
            // Create the command to execute         	
            String partialStatement = SQLStatements.getStatement((String) query
                    .getAttribute("STATEMENT"));
            ArrayList inputParameters = new ArrayList();
            StringBuffer statement = new StringBuffer(partialStatement);

            // Elaborate normal parameters (with type 'PARAMETER')
            List parameters = query.getAttributeAsList("PARAMETER");
            for (int i = 0; i < parameters.size(); i++) {
                SourceBean parameter = (SourceBean) parameters.get(i);
                DelegatedQueryExecutor.handleParameter(requestContainer, responseContainer, parameter, inputParameters,
                        dataConnection);
            } // for

            // Elaborate filter parameters (with type 'FILTER_PARAMETER')
            List filterParameters = query.getAttributeAsList("FILTER_PARAMETER");
            String condizioneSql = (partialStatement.indexOf("WHERE") != -1) ? " AND " : " WHERE ";
            for (int i = 0; i < filterParameters.size(); i++) {
                SourceBean filterParameter = (SourceBean) filterParameters.get(i);

                if (DelegatedQueryExecutor.handleFilterParameter(requestContainer, responseContainer, filterParameter,
                        inputParameters, dataConnection, statement, condizioneSql)) {
                    condizioneSql = " AND ";
                }
            } // for

            // Elaborate all sections of type "GROUP_BY"
            SourceBean groupByParameters = (SourceBean) query.getAttribute("GROUP_BY");
            if (groupByParameters != null) {
            	DelegatedQueryExecutor.handleOrderByParameter(groupByParameters, statement, " GROUP BY ");
            }

            // Elaborate all sections of type "ORDER_BY"
            List lstOrderBy = query.getAttributeAsList("ORDER_BY");
            for (int i = 0; i < lstOrderBy.size(); i++) {
                SourceBean orderBy = (SourceBean) lstOrderBy.get(i);
                condizioneSql = " ORDER BY ";
                if(i>0)  condizioneSql=" ";
                DelegatedQueryExecutor.handleOrderByParameter(requestContainer, responseContainer, orderBy, statement,
                		condizioneSql);
            } // for

/*            SourceBean orderByParameters = (SourceBean) query.getAttribute("ORDER_BY");
            if (orderByParameters != null) {            	
            	DelegatedQueryExecutor.handleOrderByParameter(requestContainer, responseContainer, orderByParameters, statement, " ORDER BY ");            
            }
*/            

            
            
            result = executeQuery(dataConnection, statement.toString(), type, inputParameters);
        } // try
        catch (EMFInternalError ex) {
            logger.error((Exception) ex);
            responseContainer.getErrorHandler().addError(
                    new EMFInternalError(EMFErrorSeverity.ERROR, ex.getNativeException()));
        } // catch (Exception ex) try
        catch (Exception ex) {
            logger.error((Exception) ex);
            responseContainer.getErrorHandler().addError(
                    new EMFInternalError(EMFErrorSeverity.ERROR, ex));
        } // catch (Exception ex) try
        finally{
            logger.debug("OUT");
        }
        return result;
    } // public static Object executeQuery(RequestContainer

    /**
     * Execs statement SQL with explicit parameters.
     * 
     * @param dataConnection connection on database:
     * @param type type of query: CREATE, READ, UPDATE, DELETE
     * @param query the SourceBean that contains the configuration of the query
     * @param parameters The parameters to add into statement
     * 
     * @return the object
     * 
     * @throws Exception the exception
     */
    public static Object executeQuery(DataConnection dataConnection, String type, SourceBean query,
            ArrayList parameters) throws Exception {
        logger.debug("IN");
        Object result = null;
        String statement = SQLStatements.getStatement((String) query.getAttribute("STATEMENT"));
        try {
            ArrayList inputParameters = new ArrayList();
            if (parameters != null) {
                for (Iterator it = parameters.iterator(); it.hasNext();) {
                    inputParameters.add(dataConnection.createDataField("", Types.VARCHAR,
                            (String) it.next()));
                }
            }
            result = executeQuery(dataConnection, statement, type, inputParameters);
            return result;
        } // try
        catch (Exception ex) {
            logger.error("", ex);
            throw ex;
        } // catch (Exception ex) try
        finally{
            logger.debug("OUT");
        }
    } // public static SourceBean executeQuery(DataConnection dataConnection,

    protected static Object executeQuery(DataConnection dataConnection, final String statement,
            final String type, final ArrayList inputParameters) throws Exception {
        
    	logger.debug("IN");
        SQLCommand sqlCommand = null;
        DataResult dataResult = null;
        Object result = null;
        try {
            // Create the command to execute
            sqlCommand = createStatementSql(dataConnection, statement, type);
            if ((inputParameters != null) && (inputParameters.size() != 0)) {
                dataResult = sqlCommand.execute(inputParameters);
            } else
                dataResult = sqlCommand.execute();
            if (type.equalsIgnoreCase("SELECT")) {
                ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult
                        .getDataObject();
                result = scrollableDataResult.getSourceBean();
            } // if (type.equalsIgnoreCase("SELECT"))
            else {
                InformationDataResult informationDataResult = (InformationDataResult) dataResult
                        .getDataObject();
                result = new Boolean(informationDataResult.getAffectedRows() > 0);
            } // if (type.equalsIgnoreCase("SELECT")) else
            return result;
        } // try
        catch (Exception ex) {
            logger.error("", ex);
            throw ex;
        } // catch (Exception ex) try
        finally {
            Utils.releaseResources(null, sqlCommand, dataResult);
            logger.debug("OUT");
        }
    }

    /**
     * Treatment of standard parameters
     * 
     * @param requestContainer
     * @param responseContainer
     * @param parameter
     * @param inputParameters
     * @param dataConnection
     * @return
     */
    protected static boolean handleParameter(final RequestContainer requestContainer,
            final ResponseContainer responseContainer, SourceBean parameter,
            ArrayList inputParameters, DataConnection dataConnection) {

        return handleParameter(requestContainer, responseContainer, parameter, inputParameters,
                dataConnection, false, null, null);
    }

    /**
     * Treatment of Filter's parameters
     *  
     * @param requestContainer
     * @param responseContainer
     * @param parameter
     * @param inputParameters
     * @param dataConnection
     * @param statement
     * @param condizioneSql
     * @return
     */
    protected static boolean handleFilterParameter(final RequestContainer requestContainer,
            final ResponseContainer responseContainer, SourceBean parameter,
            ArrayList inputParameters, DataConnection dataConnection, final StringBuffer statement,
            final String condizioneSql) {

        return handleParameter(requestContainer, responseContainer, parameter, inputParameters,
                dataConnection, true, statement, condizioneSql);
    }

    /**
     * General management for normal and filter parameters      
     * 
     * @param requestContainer
     * @param responseContainer
     * @param parameter
     * @param inputParameters
     * @param dataConnection
     * @param isFilterParameter
     * @param statement
     * @param condizioneSql
     * @return
     */
    protected static boolean handleParameter(final RequestContainer requestContainer,
            final ResponseContainer responseContainer, final SourceBean parameter,
            ArrayList inputParameters, DataConnection dataConnection,
            final boolean isFilterParameter, StringBuffer statement, final String condizioneSql) {
	logger.debug("IN");
        boolean parameterUsed = false;
        // gets attibutes of type 'PARAMETER'
        String parameterType = (String) parameter.getAttribute("TYPE");
        String parameterValue = (String) parameter.getAttribute("VALUE");
        String parameterScope = (String) parameter.getAttribute("SCOPE");

        String inParameterValue = null;
        boolean skipParameterInsertion = false;
        //Set the TRUE value based on the database type
        if (parameterType.equalsIgnoreCase("TRUE_VALUE")){
        	DatabaseMetaData dbMetadata = null;
			try {
				Connection connection  = dataConnection.getInternalConnection();
				if (connection != null){
					dbMetadata = connection.getMetaData();	
				}
			} catch (SQLException e) {
				logger.error("Error getting database metadata");
			}
        	DialectResolver resolver =  new StandardDialectResolver();
        	Dialect dialect = resolver.resolveDialect(dbMetadata);
        	String dialectName = dialect.toString();
        	if (dialectName.contains("PostgreSQL")){
        		//inParameterValue = "true";
        		//add parameter value as SQL TYPE BOOLEAN
        		inputParameters.add(dataConnection.createDataField("", Types.BOOLEAN, true));

        	} else {
        		inParameterValue = "1";
        		inputParameters.add(dataConnection.createDataField("", Types.VARCHAR, inParameterValue));

        	}
    		skipParameterInsertion = true;
            parameterUsed = true;
        	
        } else if (parameterType.equalsIgnoreCase("ABSOLUTE"))
            inParameterValue = parameterValue;
        else {
            Object parameterValueObject = ContextScooping.getScopedParameter(requestContainer,
                    responseContainer, parameterValue, parameterScope, parameter);
            if (parameterValueObject != null)
                inParameterValue = parameterValueObject.toString();
        } // if (parameterType.equalsIgnoreCase("ABSOLUTE")) else

        
        if (inParameterValue == null)
            inParameterValue = "";
        
        if(!skipParameterInsertion){
            if (!isFilterParameter) { // normal parameter
                inputParameters
                        .add(dataConnection.createDataField("", Types.VARCHAR, inParameterValue));
                parameterUsed = true;
            } else { // filter parameter

                // if the filter parameter has really a value
                if (!inParameterValue.equals("")) {

                    // read attribute "sql" of parameter
                    String sqlToAdd = (String) parameter.getAttribute("SQL");
                    int indexLike = sqlToAdd.indexOf(" LIKE ");

                    //adds the parameter to input parameters of SQL, if necessary                 
                    int countParams = StringUtils.count(sqlToAdd, '?');
                    if (countParams != 0) {
                        // Aggiungo il parametro tante volte quanti sono i caratteri
                        // '?' presenti nella stringa
                        for (int i = 0; i < countParams; i++) {
                            inputParameters.add(dataConnection.createDataField("", Types.VARCHAR,
                                    inParameterValue + (indexLike >= 0 ? "%" : "")));
                        }
                        statement.append(condizioneSql);
                    }

                    // addinbg the sql command to the statement (change directly the object StringBuffer)
                    statement.append(sqlToAdd);

                    parameterUsed = true;

                } // if !inParameterValue
            } // else !isFilterParameter
        }

        logger.debug("OUT");
        return parameterUsed;
    } // elaboraParametro

    /**
     * Management of option  ORDER_BY
     * 
     * @param parameter
     * @param statement
     * @param condizioneSql
     */
    protected static void handleOrderByParameter(final RequestContainer requestContainer,final ResponseContainer responseContainer,
    		final SourceBean parameter, StringBuffer statement, final String condizioneSql) {
	logger.debug("IN");
    	 String parameterType = (String) parameter.getAttribute("TYPE");
         String parameterValue = (String) parameter.getAttribute("VALUE");
         String parameterScope = (String) parameter.getAttribute("SCOPE");

         //gets order by attribute (dynamically)
         String inParameterValue = null;
         if (parameterType.equalsIgnoreCase("ABSOLUTE"))
             inParameterValue = parameterValue;
         else {
             Object parameterValueObject = ContextScooping.getScopedParameter(requestContainer,
                     responseContainer, parameterValue, parameterScope, parameter);
             if (parameterValueObject != null)
                 inParameterValue = parameterValueObject.toString();
         } // if (parameterType.equalsIgnoreCase("ABSOLUTE")) else
         
         // if the value of the column for the ordination is again null or empty string, try to get it from SQL attribute
         //(like general spago management)
         if (inParameterValue == null || inParameterValue.equals(""))
        	 inParameterValue = (String) parameter.getAttribute("SQL");
        //String sqlToAdd = (String) parameter.getAttribute("SQL");
        

        // Aggiungo la parte di SQL allo statement (modifico direttamente
        // l'oggetto StringBuffer)
        statement.append(condizioneSql);
        //statement.append(sqlToAdd);
        statement.append(inParameterValue);   
        logger.debug("OUT");

    } 
}
