from pydantic import BaseModel
import configparser

import os, sys
from dotenv import load_dotenv
from Lib.CryptHandler import CryptHandler


class PostgresqlConfig(BaseModel):

    host: str
    port: str
    database: str
    user: str
    password: str

    def get_dns(self):
        return f"postgresql+psycopg2://{self.user}:{self.password}@{self.host}:{self.port}/{self.database}"


class TrinoConfig(BaseModel):

    host: str
    port: str
    user: str

    def get_dns(self):
        return f"trino://{self.user}@{self.host}:{self.port}"


class ProfilingConfig(BaseModel):

    workers: int = 0
    big_workers: int = 1
    cpu_ratio: float = 0.5
    limit_rows: int = 1000000
    incremental: bool = False
    refresh_days: int = 7


class Config(BaseModel):

    crypt_key: str
    log_config_file: str
    profiling: ProfilingConfig
    database: PostgresqlConfig
    trino: TrinoConfig

    @classmethod
    def get_config(cls):
        # .env 파일 로드
        load_dotenv()

        config_file = os.getenv("CONFIG_FILE")
        if config_file is None:
            print("env 에 프로세스 환경설정 파일을 설정하세요 [예: CONFIG_FILE = process_config.ini]")
            sys.exit()
        process_config = configparser.ConfigParser()
        process_config.read(config_file, encoding="utf-8")

        crypt_key = process_config.get("security", "cryptkey")
        defaults = ProfilingConfig()

        def profiling_option(key: str, getter: str = "get"):
            if not process_config.has_option("profiling", key):
                return getattr(defaults, key)
            return getattr(process_config, getter)("profiling", key)

        return cls(
            crypt_key=crypt_key,
            log_config_file=process_config.get("profiling", "log_config_file"),
            profiling=ProfilingConfig(
                workers=profiling_option("workers", "getint"),
                big_workers=profiling_option("big_workers", "getint"),
                cpu_ratio=profiling_option("cpu_ratio", "getfloat"),
                limit_rows=profiling_option("limit_rows", "getint"),
                incremental=profiling_option("incremental", "getboolean"),
                refresh_days=profiling_option("refresh_days", "getint"),
            ),
            database=PostgresqlConfig(
                host=process_config.get("postgres", "host"),
                port=process_config.get("postgres", "port"),
                database=process_config.get("postgres", "database"),
                user=process_config.get("postgres", "user"),
                password=CryptHandler.decryptWithcryptkey(crypt_key, process_config.get("postgres", "password")),
            ),
            trino=TrinoConfig(
                host=process_config.get("trino", "host"),
                port=process_config.get("trino", "port"),
                user=process_config.get("trino", "user"),
            ),
        )


process_config = Config.get_config()

