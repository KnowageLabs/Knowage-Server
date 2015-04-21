INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'FinalUsersManagement','FinalUsersManagement', 'server', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';\p\g
commit;\p\g

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement'));\p\g
commit;\p\g

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement'));\p\g
commit;\p\g

ALTER TABLE SBI_EXT_ROLES ADD COLUMN MANAGE_USERS TINYINT DEFAULT 0;\p\g

--Deleted 'DATE_DEFAULT' as parameter type. The only parameters types usable are: date, num and string!
update SBI_PARAMETERS set PAR_TYPE_CD = 'DATE', PAR_TYPE_ID = 
	(select VALUE_ID from SBI_DOMAINS where value_cd = 'DATE' AND domain_cd = 'PAR_TYPE') WHERE PAR_TYPE_CD = 'DATE_DEFAULT';\p\g
delete from SBI_DOMAINS  where VALUE_NM = 'sbidomains.nm.date.default';\p\g
commit;\p\g

