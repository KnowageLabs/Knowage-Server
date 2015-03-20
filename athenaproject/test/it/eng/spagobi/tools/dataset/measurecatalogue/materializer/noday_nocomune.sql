select * from (


select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	state stato
from
	fact_table fac,
	dim_day dimday,
	dim_geo dimgeo
where 
	fac.geo_id = dimgeo.codice_istat and
	fac.time_id =  dimday.day_key and
	day_num=1
group by anno,mese,stato


) x,(
select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	state stato
from
	fact_table fac,
	dim_day dimday,
	dim_geo dimgeo
where 
	fac.geo_id = dimgeo.codice_istat and
	fac.time_id =  dimday.day_key and
	comune_ita  like '%ia%' and
	day_num/5<2
group by anno,mese,stato


)
y 
where
x.stato = y.stato and
x.anno = y.anno and
x.mese = y.mese