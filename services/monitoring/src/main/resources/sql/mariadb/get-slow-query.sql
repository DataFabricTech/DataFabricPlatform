SELECT
    sql_text,
    COUNT(*) AS total_count,
    ROUND(AVG(query_time), 1) AS avg_exec_time
FROM
    mysql.slow_log
GROUP BY
    sql_text
order by
    avg_exec_time DESC;