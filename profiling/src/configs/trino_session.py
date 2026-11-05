from trino.dbapi import connect

from contextlib import contextmanager

from src.configs.process_config import process_config
from src.utils.logger import get_logger

'''
    https://github.com/trinodb/trino-python-client/tree/master
'''


@contextmanager
def create_session():

    logger = get_logger()

    # Trino 연결 설정
    conn = connect(
        host=process_config.trino.host,
        port=process_config.trino.port,
        user=process_config.trino.user,
    )

    try:
        # 연결된 커넥션의 커서 반환 (yield)
        yield conn.cursor()
    except Exception as e:
        # 예외가 발생하면 예외를 로깅하거나 처리
        logger.error(f"trino session Error occurred: {e}")
        raise  # 예외를 다시 던짐 (필요에 따라 처리 가능)
    finally:
        # 연결이 끝나면 자원을 안전하게 닫기
        conn.close()
        logger.info("Trino Connection closed.")

