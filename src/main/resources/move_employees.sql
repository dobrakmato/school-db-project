with cte as (
    -- optimize count(*)
    select case_id, count(*) as cnt
    from assigned_employees
    group by case_id
    order by cnt asc),
     -- high populated case id
     high_case as (select *
                   from cte
                   order by cnt desc
                   limit 1),
     -- low populated case id
     low_case as (select *
                  from cte
                  limit 1),
     -- employees of high populated case
     high_empl as (select *, row_number() over (order by id) as num
                   from high_case
                            join assigned_employees on high_case.case_id = assigned_employees.case_id),
     -- employees to move
     empl_to_move as (select *
                      from high_empl,
                           high_case
                      where num < cast(random() * 0.5 * high_case.cnt as integer))
update assigned_employees
set case_id = low_case.case_id
from empl_to_move,
     low_case
where empl_to_move.id = assigned_employees.id