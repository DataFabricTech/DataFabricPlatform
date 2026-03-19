from typing import Iterator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from contextlib import contextmanager

from DataProfiling.configs.process_config import process_config
from DataProfiling.utils.logger import get_logger

SessionFactory = sessionmaker(
    bind=create_engine(process_config.database.get_dns()),
    autocommit=False,
    autoflush=False,
    expire_on_commit=False,
)


@contextmanager
def create_session() -> Iterator[Session]:

    logger = get_logger()

    session = SessionFactory()
    try:
        yield session
        session.commit()
    except Exception as e:
        session.rollback()
        logger.error(f"postgres session Error occurred: {e}")
        raise
    finally:
        session.close()
        logger.info("Postgres Connection closed.")

