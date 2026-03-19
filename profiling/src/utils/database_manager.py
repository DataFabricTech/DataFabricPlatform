from typing import (
    Any,
    List,
    Sequence,
)

from sqlalchemy.orm import Session
from sqlalchemy.sql.expression import Executable
from sqlalchemy import Connection


class SessionMixin:

    def __init__(self, db_session: Session, trino_connection: Connection) -> None:
        self.db_session = db_session
        self.trino_connection = trino_connection


class DataBaseManager(SessionMixin):
    """business logic"""

    def add_one(self, model: Any) -> None:
        self.db_session.add(model)

    def add_all(self, models: Sequence[Any]) -> None:
        self.db_session.add_all(models)

    def get_one(self, select_stmt: Executable) -> Any:
        return self.db_session.scalar(select_stmt)

    def get_all(self, select_stmt: Executable) -> List[Any]:
        return list(self.db_session.scalars(select_stmt).all())

    def execute(self, stmt):
        return self.db_session.execute(stmt)

    def add_commit(self, obj):
        self.add_one(obj)
        self.db_session.commit()
        self.db_session.refresh(obj)

    def update_commit(self, statement):
        result = self.execute(statement)
        self.db_session.commit()
        return result

    def trino_get_all(self, stmt):
        return self.trino_connection.execute(stmt).fetchall()

    def trino_get_one(self, stmt):
        return self.trino_connection.execute(stmt).fetchone()

