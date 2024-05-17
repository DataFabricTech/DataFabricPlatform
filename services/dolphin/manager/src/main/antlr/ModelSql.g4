/*
sqlite 의 SQL 문법을 참고하여 파싱 문법을 정의 합니다.
https://sqlite.org/lang.html

2024-01-08 기준 SELECT 문법에 대한 내용이 작성 되어 있습니다.
*/

// DataFabric SQL 은 무조건 1개의 statement 만 확인한다.

grammar ModelSql;
@header {
package com.mobigen.dolphin.antlr;
}

// EOF 사용 이유 : https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#start-rules-and-eof
parse
: SCOL* sql_stmt SCOL* EOF
;

// statements
sql_stmt
: (K_EXPLAIN (K_QUERY K_PLAN)?)? ( select_stmt
//                                 | update_stmt
//                                 | update_stmt_limited )
)
;

// https://sqlite.org/lang_select.html
// 실행 순서
//   from
//   join
//   where
//   group by
//     having
//   select
//   distinct
//   order by
//   limit
//   union
select_stmt
: select_core (compound_operator select_core)*
  order_by_?
  limit_?
;

//update_stmt:;
//update_stmt_limited:;

select_core
: select_
  from_?
  where_?
  group_by_?  // group by 없이 having 절을 사용하는 경우는 제외 함
;

select_
: K_SELECT (K_DISTINCT | K_ALL)? result_column (COMMA result_column)*
;

from_
: K_FROM (table_or_subquery | join_clause)
;

where_
: K_WHERE expr
;

group_by_
: K_GROUP K_BY expr (COMMA expr)* (K_HAVING expr)?
;

order_by_
: K_ORDER K_BY ordering_term (COMMA ordering_term)*
;

limit_  // sqlite 는 limit expr 이 가능 하다고 표현되어 있지만, 우리는 정수만 받는다.
: K_LIMIT INTEGER_LITERAL ((K_OFFSET | COMMA) INTEGER_LITERAL)?
;

// operator 우선순위 https://sqlite.org/lang_expr.html
// unary operator
// ||
// * / %
// + -
// & | << >>
// ESCAPE
// < > <= >=
// =   ==   <>   !=   IS   IS NOT   IS DISTINCT FROM   IS NOT DISTINCT FROM
//     BETWEEN expr AND expr   IN   MATCH   LIKE   REGEXP   GLOB
//     ISNULL   NOTNULL   NOT NULL
// NOT expr
// AND
// OR
expr
: literal_value
| BIND_PARAMETER
| function_name OPEN_PAR function_arguments CLOSE_PAR
| (((catalog_name DOT)? schema_name DOT)? model_name DOT)? column_name
| unary_operator expr
| expr PIPE2 expr
| expr (STAR | DIV | MOD) expr
| expr (PLUS | MINUS) expr
| expr (AMP | PIPE | GT2 | LT2) expr
| expr (GT | LT | GT_EQ | LT_EQ) expr
| expr (EQ | EQ2 | NOT_EQ1 | NOT_EQ2) expr
| OPEN_PAR expr (COMMA expr)* CLOSE_PAR
| K_CAST OPEN_PAR expr K_AS TYPE_NAME CLOSE_PAR
| expr K_COLLATE collation_name
| expr K_NOT? (K_LIKE expr (K_ESCAPE expr)? | (K_GLOB | K_REGEXP | K_MATCH) expr)
| expr (K_ISNULL | K_NOTNULL | K_NOT K_NULL)
| expr K_IS K_NOT? (K_DISTINCT K_FROM)? expr
| expr K_NOT? K_BETWEEN expr K_AND expr
| expr K_NOT? K_IN (OPEN_PAR (select_stmt | expr (COMMA expr)*)? CLOSE_PAR
                   | ((catalog_name DOT)? schema_name DOT)? model_name)  // schema_name.function(...) 은 사용 안함
| (K_NOT? K_EXISTS)? OPEN_PAR select_stmt CLOSE_PAR
| K_CASE expr? (K_WHEN expr K_THEN expr)+ (K_ELSE expr)? K_END
| expr K_AND expr
| expr K_OR expr
| raise_function
;

join_clause
: table_or_subquery (join_operator table_or_subquery join_constraint?
                    (join_operator table_or_subquery join_constraint?)*)?
;

ordering_term
: expr (K_COLLATE collation_name)? (K_ASC | K_DESC)? (K_NULLS (K_FIRST | K_LAST))?
;

result_column
: expr (K_AS? column_alias)?
| STAR
| model_name DOT STAR
;

table_or_subquery
: (((catalog_name DOT)? schema_name DOT)? model_name
| OPEN_PAR select_stmt CLOSE_PAR
| OPEN_PAR join_clause CLOSE_PAR) (K_AS? table_alias)?
;

function_arguments
: K_DISTINCT? expr (COMMA expr)* (K_ORDER K_BY ordering_term (COMMA ordering_term)*)?
| STAR?
;

raise_function
: K_RAISE OPEN_PAR ( K_IGNORE
                   | (K_ROLLBACK | K_ABORT | K_FAIL) COMMA error_message)
  CLOSE_PAR
;


error_message
: STRING_LITERAL
;

join_constraint
: K_ON expr
| K_USING OPEN_PAR column_name (COMMA column_name)* CLOSE_PAR
;

keyword  // https://sqlite.org/lang_keywords.html
: K_ABORT
//| K_ACTION
//| K_ADD
//| K_AFTER
| K_ALL
//| K_ALTER
//| K_ANALYZE
| K_AND
| K_AS
| K_ASC
//| K_ATTACH
//| K_AUTOINCREMENT
//| K_BEFORE
//| K_BEGIN
| K_BETWEEN
| K_BY
//| K_CASCADE
| K_CASE
| K_CAST
//| K_CHECK
| K_COLLATE
//| K_COLUMN
//| K_COMMIT
//| K_CONFLICT
//| K_CONSTRAINT
//| K_CREATE
| K_CROSS
| K_CURRENT_DATE
| K_CURRENT_TIME
| K_CURRENT_TIMESTAMP
//| K_DATABASE
| K_DEFAULT
//| K_DEFERRABLE
//| K_DEFERRED
//| K_DELETE
| K_DESC
//| K_DETACH
| K_DISTINCT
//| K_DROP
//| K_EACH
| K_ELSE
| K_END
//| K_ENABLE
| K_ESCAPE
| K_EXCEPT
//| K_EXCLUSIVE
| K_EXISTS
| K_EXPLAIN
| K_FAIL
//| K_FOR
//| K_FOREIGN
| K_FROM
| K_FULL
| K_GLOB
| K_GROUP
| K_HAVING
//| K_IF
| K_IGNORE
//| K_IMMEDIATE
| K_IN
//| K_INDEX
| K_INDEXED
//| K_INITIALLY
| K_INNER
//| K_INSERT
//| K_INSTEAD
| K_INTERSECT
//| K_INTO
| K_IS
| K_ISNULL
| K_JOIN
//| K_KEY
| K_LEFT
| K_LIKE
| K_LIMIT
| K_MATCH
| K_NATURAL
| K_NO
| K_NOT
| K_NOTNULL
| K_NULL
//| K_OF
| K_OFFSET
| K_ON
| K_OR
| K_ORDER
| K_OUTER
| K_PLAN
//| K_PRAGMA
//| K_PRIMARY
| K_QUERY
| K_RAISE
| K_RECURSIVE
//| K_REFERENCES
| K_REGEXP
//| K_REINDEX
//| K_RELEASE
//| K_RENAME
//| K_REPLACE
//| K_RESTRICT
| K_RIGHT
| K_ROLLBACK
| K_ROW
//| K_SAVEPOINT
| K_SELECT
//| K_SET
//| K_TABLE
//| K_TEMP
//| K_TEMPORARY
| K_THEN
//| K_TO
//| K_TRANSACTION
//| K_TRIGGER
| K_UNION
//| K_UNIQUE
//| K_UPDATE
| K_USING
//| K_VACUUM
| K_VALUES
//| K_VIEW
//| K_VIRTUAL
| K_WHEN
| K_WHERE
| K_WITH
//| K_WITHOUT
//| K_NEXTVAL
;

// names

collation_name: any_name;
column_alias: any_name;
column_name: any_name;
function_name: any_name;
catalog_name: any_name;
schema_name: any_name;
table_alias: any_name;
model_name: any_name;

any_name
: IDENTIFIER
| BACKTICK keyword BACKTICK
| STRING_LITERAL
| OPEN_PAR any_name CLOSE_PAR
;

compound_operator
: K_UNION K_ALL?
| K_INTERSECT
| K_EXCEPT
;

join_operator
: COMMA
| (K_NATURAL? ((K_LEFT | K_RIGHT | K_FULL) K_OUTER? | K_INNER)?
  | K_CROSS?)
  K_JOIN
;

unary_operator
: MINUS
| PLUS
| TILDE
| K_NOT
;

literal_value
: INTEGER_LITERAL
| REAL_LITERAL
| HEX_LITERAL
| STRING_LITERAL
| BLOB_LITERAL
| K_NULL
| K_TRUE
| K_FALSE
| K_CURRENT_TIME
| K_CURRENT_DATE
| K_CURRENT_TIMESTAMP
;

// keywords
// A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
K_ABORT: A B O R T;
K_ALL: A L L;
K_AND: A N D;
K_AS: A S;
K_ASC: A S C;
K_BETWEEN: B E T W E E N;
K_BY: B Y;
K_CASE: C A S E;
K_CAST: C A S T;
K_COLLATE: C O L L A T E;
K_CROSS: C R O S S;
K_CURRENT: C U R R E N T;
K_CURRENT_TIME: C U R R E N T '_' T I M E;
K_CURRENT_DATE: C U R R E N T '_' D A T E;
K_CURRENT_TIMESTAMP: C U R R E N T '_' T I M E S T A M P;
K_DEFAULT: D E F A U L T;
K_DESC: D E S C;
K_DISTINCT: D I S T I N C T;
K_ELSE: E L S E;
K_END: E N D;
K_ESCAPE: E S C A P E;
K_EXCEPT: E X C E P T;
K_EXCLUDE: E X C L U D E;
K_EXISTS: E X I S T S;
K_EXPLAIN: E X P L A I N;
K_FAIL: F A I L;
K_FALSE: F A L S E;
K_FILTER: F I L T E R;
K_FIRST: F I R S T;
K_FOLLOWING: F O L L O W I N G;
K_FROM: F R O M;
K_FULL: F U L L;
K_GLOB: G L O B;
K_GROUP: G R O U P;
K_GROUPS: G R O U P S;
K_HAVING: H A V I N G;
K_IGNORE: I G N O R E;
K_IN: I N;
K_INDEXED: I N D E X E D;
K_INNER: I N N E R;
K_INTERSECT: I N T E R S E C T;
K_IS: I S;
K_ISNULL: I S N U L L;
K_JOIN: J O I N;
K_LAST: L A S T;
K_LEFT: L E F T;
K_LIKE: L I K E;
K_LIMIT: L I M I T;
K_MATCH: M A T C H;
K_MATERIALIZED: M A T E R I A L I Z E D;
K_NATURAL: N A T U R A L;
K_NO: N O;
K_NOT: N O T;
K_NOTNULL: N O T N U L L;
K_NULL: N U L L;
K_NULLS: N U L L S;
K_OFFSET: O F F S E T;
K_ON: O N;
K_OR: O R;
K_ORDER: O R D E R;
K_OTHERS: O T H E R S;
K_OUTER: O U T E R;
K_OVER: O V E R;
K_PARTITION: P A R T I T I O N;
K_PLAN: P L A N;
K_PRECEDING: P R E C E D I N G;
K_QUERY: Q U E R Y;
K_RAISE: R A I S E;
K_RANGE: R A N G E;
K_RECURSIVE: R E C U R S I V E;
K_REGEXP: R E G E X P;
K_RIGHT: R I G H T;
K_ROLLBACK: R O L L B A C K;
K_ROW: R O W;
K_ROWS: R O W S;
K_SELECT: S E L E C T;
K_THEN: T H E N;
K_TIES: T I E S;
K_TRUE: T R U E;
K_UNBOUNDED: U N B O U N D E D;
K_UNION: U N I O N;
K_USING: U S I N G;
K_VALUES: V A L U E S;
K_WHEN: W H E N;
K_WHERE: W H E R E;
K_WINDOW: W I N D O W;
K_WITH: W I T H;

TYPE_NAME
: T E X T
| I N T E G E R
| B I G I N T
| R E A L
| T I M E S T A M P
| D A T E
| B O O L
;

IDENTIFIER  // TODO check: 더 필요한 정보 있으면 추가 해야 함
: '`' (~'`' | '``')* '`'
| [a-zA-Z_ㄱ-ㅎ가-힣] [a-zA-Z_ㄱ-ㅎ가-힣0-9]*
;

BIND_PARAMETER  // https://www.sqlite.org/c3ref/bind_blob.html
: '?' DIGIT*
| [:@$] IDENTIFIER
;

// literals https://sqlite.org/lang_expr.html
REAL_LITERAL
: (DIGIT+ '.' DIGIT*
| '.' DIGIT+) (EXPONENTIAL_MINUS | EXPONENTIAL_PLUS)?
;

HEX_LITERAL
: '0x' HEXDIGIT+
;

INTEGER_LITERAL
: DIGIT+ EXPONENTIAL_PLUS?
;

STRING_LITERAL
: '\'' ( ~'\'' | '\'\'' )* '\''
;

BLOB_LITERAL
: X STRING_LITERAL
;

SINGLE_LINE_COMMENT
: '--' ~[\r\n]* -> channel(HIDDEN)
;

MULTILINE_COMMENT
: '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
;

SPACES
: [ \u000B\t\r\n] -> channel(HIDDEN)
;

BACKTICK  : '`';
SCOL      : ';';
DOT       : '.';
OPEN_PAR  : '(';
CLOSE_PAR : ')';
COMMA     : ',';
STAR      : '*';
PLUS      : '+';
MINUS     : '-';
TILDE     : '~';
PIPE2     : '||';
DIV       : '/';
MOD       : '%';
LT2       : '<<';
GT2       : '>>';
AMP       : '&';
PIPE      : '|';
LT        : '<';
LT_EQ     : '<=';
GT        : '>';
GT_EQ     : '>=';
EQ        : '=';
EQ2       : '==';
NOT_EQ1   : '!=';
NOT_EQ2   : '<>';

UNEXPECTED_CHAR
: .
;

fragment EXPONENTIAL_MINUS : E '-' DIGIT+;
fragment EXPONENTIAL_PLUS : E '+' DIGIT+;

fragment DIGIT : [0-9];
fragment HEXDIGIT : [0-9A-F];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
