from __future__ import annotations

from typing import Any, List, Sequence, Tuple


def fetch_as_dataframe(trino_cursor: Any, query: str):
    """
    Trino DB-API cursor로부터 결과를 pandas.DataFrame 으로 변환한다.
    (프로젝트의 trino 세션은 cursor 를 주입하므로 cursor 인터페이스에 맞춤)
    """
    try:
        import pandas as pd  # type: ignore
    except Exception as e:  # pragma: no cover
        raise RuntimeError("pandas 가 필요합니다. (pip install pandas)") from e

    trino_cursor.execute(query)
    rows: Sequence[Tuple[Any, ...]] = trino_cursor.fetchall() or []
    desc: List[Tuple] = list(getattr(trino_cursor, "description", None) or [])

    columns = [d[0] for d in desc] if desc else None
    return pd.DataFrame(list(rows), columns=columns)

