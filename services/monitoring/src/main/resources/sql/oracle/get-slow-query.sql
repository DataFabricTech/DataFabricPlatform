SELECT elapsed_time/1000000 AS execTime, sql_text as sqlText
FROM v$sql
WHERE elapsed_time/1000000 > 2  -- 2초 이상 실행된 쿼리
ORDER BY elapsed_time_sec DESC