SELECT month,
       e1.name as max_cases_name,
       e2.name as max_connection_name
FROM (SELECT
        -- Month Number --
        m         as month,

        -- Employee with max closed cases for specific month --
        (SELECT closed_by_id
         FROM cases
         WHERE EXTRACT(MONTH FROM created_at) = m
           AND closed_by_id IS NOT NULL
         GROUP BY closed_by_id
         ORDER BY COUNT(*) DESC
         LIMIT 1) as max_closed_cases,

        -- Employee with max confirmed cases for specific month --
        (SELECT confirmed_by_id
         FROM connections
         WHERE EXTRACT(MONTH FROM confirmed_at) = m
           AND confirmed_by_id IS NOT NULL
         GROUP BY confirmed_by_id
         ORDER BY COUNT(*) DESC
         LIMIT 1) as max_confirmed_connections
      FROM generate_series(1, 12) as m) x
       JOIN employees e1 ON max_closed_cases = e1.id
       JOIN employees e2 ON max_confirmed_connections = e2.id