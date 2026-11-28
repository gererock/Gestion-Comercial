BEGIN ISOLATION LEVEL REPEATABLE READ READ ONLY;
SELECT current_database();
SELECT format(
    'SELECT %L, count(*), md5(coalesce(string_agg(md5(to_jsonb(t)::text), '''' ORDER BY md5(to_jsonb(t)::text)), '''')) FROM %I.%I t;',
    tablename, schemaname, tablename
)
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename
\gexec
COMMIT;
