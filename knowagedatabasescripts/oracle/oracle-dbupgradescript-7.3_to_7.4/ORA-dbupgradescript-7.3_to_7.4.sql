-- 22/12/2020 Marco Balestri
-- CATALOG FUNCTION TABLES UPDATE

-- START

-- CLEAN old tables
DELETE FROM sbi_catalog_function;
DELETE FROM sbi_function_input_variable;

-- DROP old tables
DROP TABLE sbi_function_output;
DROP TABLE sbi_function_input_dataset;
DROP TABLE sbi_function_input_file;

-- ------------------------- TABLE sbi_catalog_function -------------------------
ALTER TABLE sbi_catalog_function ADD (BENCHMARKS varchar2(4000), FAMILY varchar2(30), ONLINE_SCRIPT varchar2(4000), OFFLINE_SCRIPT_TRAIN varchar2(4000), OFFLINE_SCRIPT_USE varchar2(4000));
ALTER TABLE sbi_catalog_function DROP (SCRIPT, URL, REMOTE);

-- ----------------------- TABLE sbi_function_input_column -----------------------
CREATE TABLE sbi_function_input_column AS SELECT * FROM sbi_function_input_variable WHERE 1=0;
  
ALTER TABLE sbi_function_input_column RENAME COLUMN VAR_NAME TO COL_NAME;
ALTER TABLE sbi_function_input_column RENAME COLUMN VAR_VALUE TO COL_TYPE;
ALTER TABLE sbi_function_input_column MODIFY  COL_NAME varchar2(100);
ALTER TABLE sbi_function_input_column MODIFY COL_TYPE varchar2(100);

-- ---------------------- TABLE sbi_function_input_variable ---------------------
ALTER TABLE sbi_function_input_variable ADD VAR_TYPE varchar2(100);

-- ---------------------- TABLE sbi_function_output_column -----------------------
CREATE TABLE sbi_function_output_column AS SELECT * FROM sbi_function_input_column WHERE 1=0;

ALTER TABLE sbi_function_output_column ADD COL_FIELD_TYPE varchar2(100);

-- --------------------------- TABLE sbi_obj_function ---------------------------
CREATE TABLE sbi_obj_function AS SELECT * FROM sbi_obj_data_set WHERE 1=0;

ALTER TABLE sbi_obj_function DROP COLUMN IS_DETAIL;
ALTER TABLE sbi_obj_function RENAME COLUMN BIOBJ_DS_ID TO BIOBJ_FUNCTION_ID;
ALTER TABLE sbi_obj_function RENAME COLUMN DS_ID TO FUNCTION_ID;
ALTER TABLE sbi_obj_function MODIFY BIOBJ_FUNCTION_ID integer;
ALTER TABLE sbi_obj_function MODIFY FUNCTION_ID integer;

ALTER TABLE sbi_obj_function ADD FOREIGN KEY (BIOBJ_ID) REFERENCES sbi_objects(BIOBJ_ID);
ALTER TABLE sbi_obj_function ADD FOREIGN KEY (FUNCTION_ID) REFERENCES sbi_catalog_function(FUNCTION_ID);

-- END