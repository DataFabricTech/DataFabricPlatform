# SQL Generator

SQL 을 생성 하는 라이브러리 입니다.
String 으로 SQL 을 구성하는 것이 아닌 함수 형태로 구성 할 수 있도록 개발 되었습니다.

## 추가된 기능

- DML
    - 테이블 기반의 1차원 쿼리 생성 가능 - 서브 쿼리 사용 불가

## 추가 해야 하는 기능

- 테이블 지정시 서브쿼리 가능 하도록
- DDL
- DCL
- TCL
- where 조건 절
- 컬럼 타입 지정시 타입 체크 기능
- join, where 함수 기능이 중복된 것 리펙토링

## 사용 방법

### DML (Data Manipulation Language)

**1. select**

```java
import com.mobigen.lib.sqlgen.SQLBuilder.*;

class Sample {
    public static void main(String[] args) {
        var table = SqlTable.of("test");
        var col1 = SqlColumn.of("col1", table);
        var col2 = SqlColumn.of("col2", table);
        var statementProvider = select(col1, col2)
                .from(table)
                .generate();

        var sql = sqlBuilder.getStatement();
        System.out.println(sql);
    }
}
```

**2. insert**

```java
import com.mobigen.lib.sqlgen.SQLBuilder.*;

class Sample {
    public static void main(String[] args) {
        var table = SqlTable.of("test");
        var col1 = SqlColumn.of("col1", table);
        var col2 = SqlColumn.of("col2", table);
        var statementProvider = insert(table)
                .columns(col1, col2)
                .values("abc", 123)
                .generate();

        var sql = sqlBuilder.getStatement();
        System.out.println(sql);
    }
}
```

**3. update**

```java
import com.mobigen.lib.sqlgen.SQLBuilder.*;

class Sample {
    public static void main(String[] args) {
        var table = SqlTable.of("test");
        var col1 = SqlColumn.of("col1", table);
        var col2 = SqlColumn.of("col2", table);
        var statementProvider = update(table)
                .columns(col2)
                .values(123)
                .where(Equal.of(col1, "abc"))
                .generate();

        var sql = sqlBuilder.getStatement();
        System.out.println(sql);
    }
}
```

**4. delete**

```java
import com.mobigen.lib.sqlgen.SQLBuilder.*;

class Sample {
    public static void main(String[] args) {
        var table = SqlTable.of("test");
        var col1 = SqlColumn.of("col1", table);
        var col2 = SqlColumn.of("col2", table);
        var statementProvider = delete(table)
                .where(Equal.of(col1, "abc"))
                .generate();

        var sql = sqlBuilder.getStatement();
        System.out.println(sql);
    }
}
```

### DDL (Data Definition Language)

### DCL (Data Control Language)

### TCL (Transaction Control Language)
