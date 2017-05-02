select * from (
select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	day_num giorno
from
	fact_table fac,
	dim_day dimday,
	dim_geo dimgeo
where 
	fac.geo_id = dimgeo.codice_istat and
	fac.time_id =  dimday.day_key and
	comune_ita  like '%Badia%' and
	day_num=1
group by anno,mese,giorno
) x,(

select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	day_num giorno
from
	fact_table fac,
	dim_day dimday
where 
	fac.time_id =  dimday.day_key and
	day_num/5<2
group by anno,mese,giorno)
y 
where
x.anno = y.anno and
x.mese = y.mese and
x.giorno = y.giorno 