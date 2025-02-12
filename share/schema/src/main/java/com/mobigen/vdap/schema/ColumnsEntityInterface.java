package com.mobigen.vdap.schema;

import com.mobigen.vdap.schema.type.Column;

import java.util.List;

public interface ColumnsEntityInterface {

//  String getFullyQualifiedName();

  List<Column> getColumns();
}
