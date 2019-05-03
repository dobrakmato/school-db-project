select employees.*, count(*) as cnt
from employees
         join assigned_employees ae on employees.id = ae.employee_id
         join cases c on ae.case_id = c.id
where c.closed_by_id is not null
and employees.type != 0 /* IKT_OFFICER */
and ae.case_id != ?
and employees.id not in (?)
group by employees.id
order by cnt
limit 50