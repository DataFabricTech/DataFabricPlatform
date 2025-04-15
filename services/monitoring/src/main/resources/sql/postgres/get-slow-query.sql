SELECT
    queryid AS query_id,
    query AS sql_text,
    calls AS total_count,
    ROUND((total_exec_time / calls / 1000)::numeric, 3) AS avg_exec_time
FROM
    pg_stat_statements
WHERE
    calls > 0
ORDER BY
    avg_exec_time_sec DESC;