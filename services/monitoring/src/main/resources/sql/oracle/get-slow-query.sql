SELECT *
FROM (
         SELECT
             sql_id,
             COUNT(*) AS total_count,
             ROUND(SUM(elapsed_time) / 1000000 / SUM(executions), 3) AS avg_exec_time,
             MIN(sql_text) AS sql_text
         FROM
             v$sql
         WHERE
             executions > 0
         GROUP BY
             sql_id
     ) t
WHERE
    avg_exec_time > 4
ORDER BY
    avg_exec_time DESC;
