SELECT
    CAST(sql_text AS CHAR) AS sql_text,
    COUNT(*) AS total_count,
    ROUND(AVG(query_time), 1) AS avg_exec_time,
FROM
    mysql.slow_log
GROUP BY
    sql_text
ORDER BY
    avg_exec_time DESC;