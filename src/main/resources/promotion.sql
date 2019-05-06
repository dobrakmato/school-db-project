WITH department_heads AS (SELECT d.id as department_id, COUNT(*) as closed_cases
                          FROM departments d
                                   JOIN cases c ON c.head_employee_id = d.head_employee_id
                          where c.closed_by_id IS NOT NULL
                          GROUP BY d.id),
     employee_heads AS (SELECT e.id, e.department_id, COUNT(*) as closed_cases
                        FROM employees e
                                 JOIN cases c ON c.head_employee_id = e.id
                        where c.closed_by_id IS NOT NULL
                        GROUP BY e.id
                        order by department_id ASC, closed_cases DESC
     ),
     for_update AS (select DISTINCT ON (x.department_id) x.department_id,
                                                         x.id                        as employee_id,
                                                         x.closed_cases,
                                                         COALESCE(y.closed_cases, 0) as dep_head_closed
                    from employee_heads x
                             left join department_heads y on x.department_id = y.department_id
                    where x.closed_cases > 1.2 * y.closed_cases)
UPDATE departments
SET head_employee_id = for_update.employee_id
FROM for_update
WHERE for_update.department_id = departments.id;

