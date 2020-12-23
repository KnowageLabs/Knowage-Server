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
ADD COLUMN BENCHMARKS text,
ADD COLUMN FAMILY text,
ADD COLUMN ONLINE_SCRIPT text,
ADD COLUMN OFFLINE_SCRIPT_TRAIN text,
ADD COLUMN OFFLINE_SCRIPT_USE text,
DROP COLUMN SCRIPT,
DROP COLUMN URL,
DROP COLUMN REMOTE;

-- ----------------------- TABLE sbi_function_input_column -----------------------
CREATE TABLE IF NOT EXISTS sbi_function_input_column (LIKE sbi_function_input_variable);

ALTER TABLE sbi_function_input_column
ALTER COLUMN VAR_NAME TYPE varchar(100),
ALTER COLUMN VAR_VALUE type varchar(100);


ALTER TABLE sbi_function_input_column
RENAME COLUMN VAR_NAME TO COL_NAME;

ALTER TABLE sbi_function_input_column
RENAME COLUMN VAR_VALUE TO COL_TYPE;

-- ---------------------- TABLE sbi_function_input_variable ---------------------
ALTER TABLE sbi_function_input_variable
ADD COLUMN VAR_TYPE varchar(100);

-- ---------------------- TABLE sbi_function_output_column -----------------------
CREATE TABLE IF NOT EXISTS sbi_function_output_column (LIKE sbi_function_input_column);

ALTER TABLE sbi_function_output_column
ADD COLUMN COL_FIELD_TYPE varchar(100);

-- --------------------------- TABLE sbi_obj_function ---------------------------
CREATE TABLE IF NOT EXISTS sbi_obj_function (LIKE sbi_obj_data_set);

ALTER TABLE sbi_obj_function
DROP IS_DETAIL;

ALTER TABLE sbi_obj_function
ALTER COLUMN BIOBJ_DS_ID TYPE int,
ALTER COLUMN DS_ID TYPE int;

ALTER TABLE sbi_obj_function
RENAME COLUMN BIOBJ_DS_ID to BIOBJ_FUNCTION_ID;

ALTER TABLE sbi_obj_function
RENAME COLUMN  DS_ID to FUNCTION_ID;

ALTER TABLE sbi_obj_function
ADD FOREIGN KEY (BIOBJ_ID) REFERENCES sbi_objects(BIOBJ_ID),
ADD FOREIGN KEY (FUNCTION_ID) REFERENCES sbi_catalog_function(FUNCTION_ID);

-- END