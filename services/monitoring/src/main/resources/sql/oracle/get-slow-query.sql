SELECT elapsed_time/1000000 AS execTime, sql_text as sqlText
FROM v$sql
WHERE elapsed_time/1000000 > 2
order by execTime desc