-- 22/12/2020 Marco Balestri
-- CATALOG FUNCTION TABLES UPDATE

-- START

-- CLEAN old tables
DELETE FROM sbi_catalog_function;
DELETE FROM sbi_function_input_variable;

-- DROP old tables
DROP TABLE IF EXISTS sbi_function_output, sbi_function_input_dataset, sbi_function_input_file;

-- ------------------------- TABLE sbi_catalog_function -------------------------
ALTER TABLE sbi_catalog_function
ADD (BENCHMARKS text(4000), FAMILY text(30), ONLINE_SCRIPT text(4000), OFFLINE_SCRIPT_TRAIN text(4000), OFFLINE_SCRIPT_USE text(4000)),
DROP SCRIPT, DROP URL, DROP REMOTE;

-- ----------------------- TABLE sbi_function_input_column -----------------------
CREATE TABLE IF NOT EXISTS sbi_function_input_column LIKE .sbi_function_input_variable;
  
ALTER TABLE sbi_function_input_column
CHANGE VAR_NAME COL_NAME varchar(100),
CHANGE VAR_VALUE COL_TYPE varchar(100);

-- ---------------------- TABLE sbi_function_input_variable ---------------------
ALTER TABLE sbi_function_input_variable
ADD (VAR_TYPE varchar(100));

-- ---------------------- TABLE sbi_function_output_column -----------------------
CREATE TABLE IF NOT EXISTS sbi_function_output_column LIKE sbi_function_input_column;

ALTER TABLE sbi_function_output_column
ADD (COL_FIELD_TYPE varchar(100));

-- --------------------------- TABLE sbi_obj_function ---------------------------
CREATE TABLE IF NOT EXISTS sbi_obj_function LIKE sbi_obj_data_set;

ALTER TABLE sbi_obj_function
DROP IS_DETAIL,
CHANGE BIOBJ_DS_ID BIOBJ_FUNCTION_ID int,
CHANGE DS_ID FUNCTION_ID int,
ADD FOREIGN KEY (BIOBJ_ID) REFERENCES sbi_objects(BIOBJ_ID),
ADD FOREIGN KEY (FUNCTION_ID) REFERENCES sbi_catalog_function(FUNCTION_ID);

-- END