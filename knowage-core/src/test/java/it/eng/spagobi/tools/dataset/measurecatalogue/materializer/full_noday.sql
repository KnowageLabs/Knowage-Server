select * from (

select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	state stato,
	comune_ita comune
from
	fact_table fac,
	dim_day dimday,
	dim_geo dimgeo
where 
	fac.geo_id = dimgeo.codice_istat and
	fac.time_id =  dimday.day_key and
	comune_ita  like '%Badia%' and
	day_num=1
group by anno,mese,comune,stato

) x,(

select 
	sum(measure1) as val,
	year_num as anno,
	month_name mese,
	state stato,
	comune_ita comune
from
	fact_table fac,
	dim_day dimday,
	dim_geo dimgeo
where 
	fac.geo_id = dimgeo.codice_istat and
	fac.time_id =  dimday.day_key and
	comune_ita  like '%ia%' and
	day_num/5<2
group by anno,mese,comune,stato

)
y 
where
x.comune= y.comune and
x.stato = y.stato and
x.anno = y.anno and
x.mese = y.mese