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
     -- employees of low populated case
     low_empl as (select *
                  from low_case
                           join assigned_employees on low_case.case_id = assigned_employees.case_id),
     -- employees to move (may violate unique constr)
     empl_to_move as (select *
                      from high_empl,
                           high_case
                      where num < cast(random() * 0.5 * high_case.cnt as integer)),
     -- employees to move (safe)
     empl_to_move_safe as (select *
                           from empl_to_move
                           where empl_to_move.employee_id not in (select low_empl.employee_id from low_empl))
update assigned_employees
set case_id = low_case.case_id
from empl_to_move_safe,
     low_case
where empl_to_move_safe.id = assigned_employees.id
