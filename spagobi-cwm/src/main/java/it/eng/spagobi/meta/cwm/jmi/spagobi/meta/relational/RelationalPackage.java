package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations.EnumerationsPackage;
import javax.jmi.reflect.RefPackage;

public abstract interface RelationalPackage
  extends RefPackage
{
  public abstract EnumerationsPackage getEnumerations();
  
  public abstract CwmCatalogClass getCwmCatalog();
  
  public abstract CwmSchemaClass getCwmSchema();
  
  public abstract CwmColumnSetClass getCwmColumnSet();
  
  public abstract CwmNamedColumnSetClass getCwmNamedColumnSet();
  
  public abstract CwmTableClass getCwmTable();
  
  public abstract CwmViewClass getCwmView();
  
  public abstract CwmQueryColumnSetClass getCwmQueryColumnSet();
  
  public abstract CwmSqldataTypeClass getCwmSqldataType();
  
  public abstract CwmSqldistinctTypeClass getCwmSqldistinctType();
  
  public abstract CwmSqlsimpleTypeClass getCwmSqlsimpleType();
  
  public abstract CwmSqlstructuredTypeClass getCwmSqlstructuredType();
  
  public abstract CwmColumnClass getCwmColumn();
  
  public abstract CwmProcedureClass getCwmProcedure();
  
  public abstract CwmTriggerClass getCwmTrigger();
  
  public abstract CwmSqlindexClass getCwmSqlindex();
  
  public abstract CwmUniqueConstraintClass getCwmUniqueConstraint();
  
  public abstract CwmForeignKeyClass getCwmForeignKey();
  
  public abstract CwmSqlindexColumnClass getCwmSqlindexColumn();
  
  public abstract CwmPrimaryKeyClass getCwmPrimaryKey();
  
  public abstract CwmRowClass getCwmRow();
  
  public abstract CwmColumnValueClass getCwmColumnValue();
  
  public abstract CwmCheckConstraintClass getCwmCheckConstraint();
  
  public abstract CwmRowSetClass getCwmRowSet();
  
  public abstract CwmSqlparameterClass getCwmSqlparameter();
  
  public abstract TriggerUsingColumnSet getTriggerUsingColumnSet();
  
  public abstract TableOwningTrigger getTableOwningTrigger();
  
  public abstract ColumnSetOfStructuredType getColumnSetOfStructuredType();
  
  public abstract ColumnRefStructuredType getColumnRefStructuredType();
  
  public abstract ColumnOptionsColumnSet getColumnOptionsColumnSet();
  
  public abstract DistinctTypeHasSimpleType getDistinctTypeHasSimpleType();
}
