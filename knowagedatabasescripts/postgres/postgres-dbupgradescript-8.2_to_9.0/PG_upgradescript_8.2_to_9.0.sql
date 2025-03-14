DELETE FROM SBI_EVENTS_ROLES ;
DROP TABLE SBI_EVENTS_ROLES ;

ALTER TABLE SBI_CACHE_ITEM
ADD COLUMN PARAMETERS TEXT NULL DEFAULT NULL;

DELETE
FROM
	SBI_PRODUCT_TYPE_ENGINE
where
	ENGINE_ID in (
	select
		ENGINE_ID
	from
		SBI_ENGINES
	WHERE
		label in('knowagegisengine', 'knowageprocessengine', 'knowagesvgviewerengine')

);
DELETE FROM SBI_ENGINES WHERE label in('knowagegisengine', 'knowageprocessengine', 'knowagesvgviewerengine');