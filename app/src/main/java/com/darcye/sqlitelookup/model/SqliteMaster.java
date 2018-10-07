package com.darcye.sqlitelookup.model;

import com.darcye.sqlite.Table;
import com.darcye.sqlite.Table.Column;

@Table(name="sqlite_master")
public class SqliteMaster {
	
	@Column(name="type",type=Column.TYPE_STRING)
	public String type;
	
	@Column(name="name",type=Column.TYPE_STRING)
	public String name;
	
	@Column(name="tbl_name",type=Column.TYPE_STRING)
	public String tblName;
	
	@Column(name="rootpage",type=Column.TYPE_INTEGER)
	public Integer rootpage;
	
	@Column(name="sql",type=Column.TYPE_STRING)
	public String sql;
}
