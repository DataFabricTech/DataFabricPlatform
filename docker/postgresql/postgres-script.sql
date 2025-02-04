CREATE DATABASE vdap_db;
CREATE DATABASE airflow_db;
CREATE USER vdap_admin WITH PASSWORD 'vdap_admin_pass';
CREATE USER airflow_user WITH PASSWORD 'airflow_pass';
ALTER DATABASE vdap_db OWNER TO vdap_admin;
ALTER DATABASE airflow_db OWNER TO airflow_user;
ALTER USER airflow_user SET search_path = public;
commit;