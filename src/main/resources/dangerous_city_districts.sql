WITH for_months as (select extract(year from created_at)                    as ye,
                           extract(quarter from created_at)                 as qu,
                           extract(month from created_at)                   as mo,
                           dist.name                                        as district,
                           count(cas.id)                                    as cases_count,
                           count(cas.id) filter ( where cas.case_type = 0 ) as type0_count,
                           count(cas.id) filter ( where cas.case_type = 1 ) as type1_count
                    from cases cas
                             join connections con on con.case_id = cas.id
                             join crime_scenes cri on cri.id = con.crime_scene_id
                             join city_districts dist on cri.city_district_id = dist.id
                    group by extract(year from created_at), extract(quarter from created_at),
                             extract(month from created_at), dist.name
                    order by extract(year from created_at), extract(quarter from created_at),
                             extract(month from created_at), cases_count DESC)
/* individual months */
select *
from (select cast(ye as text),
             '-'                                                                    as qu,
             cast(mo as text),
             ROW_NUMBER() OVER (PARTITION BY ye, mo ORDER BY SUM(cases_count) DESC) as pos,
             district,
             SUM(cases_count)                                                       as cases,
             SUM(type0_count)                                                       as type0,
             SUM(type1_count)                                                       as type1
      from for_months
      group by ye, mo, district
      order by ye, cast(mo as integer), cases desc) x
where pos <= 3

union all

/* quarters */
select *
from (select cast(ye as text),
             cast(qu as text),
             '-'                                                                    as mo,
             ROW_NUMBER() OVER (PARTITION BY ye, qu ORDER BY SUM(cases_count) DESC) as pos,
             district,
             SUM(cases_count)                                                       as cases,
             SUM(type0_count)                                                       as type0,
             SUM(type1_count)                                                       as type1
      from for_months
      group by ye, qu, district
      order by ye, qu, cases desc) x
where pos <= 3

union all

/* years */
select *
from (select cast(ye as text),
             '-'                                                                as qu,
             '-'                                                                as mo,
             ROW_NUMBER() OVER (PARTITION BY ye ORDER BY SUM(cases_count) DESC) as pos,
             district,
             SUM(cases_count)                                                   as cases,
             SUM(type0_count)                                                   as type0,
             SUM(type1_count)                                                   as type1
      from for_months
      group by ye, district
      order by cases desc) x
where pos <= 3

