SELECT a.month,
       a.row_num as closed_position,
       pa.name   as closed_by,
       a.closed_cases,
       b.row_num as confirmed_position,
       pb.name   as confirmed_by,
       b.confirmed_cases
FROM (SELECT EXTRACT(MONTH FROM created_at)                                                         as month,
             closed_by_id,
             count(*)                                                                               as closed_cases,
             ROW_NUMBER() OVER (PARTITION BY EXTRACT(MONTH FROM created_at) ORDER BY count(*) DESC) as row_num
      FROM cases
      WHERE created_at < date_trunc('year', now()) + INTERVAL '1 YEAR'
        AND created_at > date_trunc('year', now())
        AND closed_by_id IS NOT NULL
      GROUP BY EXTRACT(MONTH FROM created_at), closed_by_id) a
         JOIN (
    SELECT EXTRACT(MONTH FROM confirmed_at)                                                         as month,
           confirmed_by_id,
           count(*)                                                                                 as confirmed_cases,
           ROW_NUMBER() OVER (PARTITION BY EXTRACT(MONTH FROM confirmed_at) ORDER BY count(*) DESC) as row_num
    FROM connections
             JOIN persons p on connections.person_id = p.id
    WHERE confirmed_at < '2020-01-01'
      AND confirmed_at > '2019-01-01'
      AND p.person_type = 0 /* podozrivy */
      AND confirmed_by_id IS NOT NULL
    GROUP BY EXTRACT(MONTH FROM confirmed_at), confirmed_by_id
) b ON a.month = b.month AND a.row_num = b.row_num
         JOIN persons pa ON closed_by_id = pa.id
         JOIN persons pb ON confirmed_by_id = pb.id
WHERE a.row_num < 3
   OR b.row_num < 3
ORDER BY a.month ASC, a.closed_cases DESC