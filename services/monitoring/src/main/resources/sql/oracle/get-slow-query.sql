SELECT elapsed_time/1000000 AS exec_time, sql_text as sql_text
FROM v$sql
WHERE elapsed_time/1000000 > 2
order by execTime desc