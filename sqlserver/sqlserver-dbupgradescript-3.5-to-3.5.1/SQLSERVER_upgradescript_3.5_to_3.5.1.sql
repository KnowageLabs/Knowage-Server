INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'FinalUsersManagement','FinalUsersManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement'));
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement'));
commit;

ALTER TABLE SBI_EXT_ROLES ADD MANAGE_USERS BIT DEFAULT 0;

--Deleted 'DATE_DEFAULT' as parameter type. The only parameters types usable are: date, num and string!
update SBI_PARAMETERS set PAR_TYPE_CD = 'DATE', PAR_TYPE_ID = 
	(select VALUE_ID from SBI_DOMAINS where value_cd = 'DATE' and domain_cd = 'PAR_TYPE') WHERE PAR_TYPE_CD = 'DATE_DEFAULT';
delete from SBI_DOMAINS  where VALUE_NM = 'sbidomains.nm.date.default';
commit;