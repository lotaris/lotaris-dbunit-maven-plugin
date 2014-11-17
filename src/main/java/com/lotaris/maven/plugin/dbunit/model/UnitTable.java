package com.lotaris.maven.plugin.dbunit.model;

import java.util.Set;
import java.util.TreeSet;
import org.dbunit.ant.Query;

/**
 * Represent a table information with ordered column names
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 */
public class UnitTable {
	private String tableName;
	
	private Set<UnitColumn> columns = new TreeSet<UnitColumn>(); 
	
	//<editor-fold defaultstate="collapsed" desc="Constructors">
	public UnitTable(String tableName) {
		this.tableName = tableName;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Getters & Setters">
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void addColumn(String columnName) {
		columns.add(new UnitColumn(columnName));
	}
	//</editor-fold>
	
	//<editor-fold defaultstate="collapsed" desc="Overrides">
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (UnitColumn uc : columns) {
			sb.append(uc).append(", ");
		}
		
		return tableName + "[" + sb.toString().replaceAll(", $", "") + "]";
	}
	//</editor-fold>
	
	/**
	 * Build the query to extract the data with the insurance
	 * that the columns are always alphabetically ordered.
	 * @return The query ready for DB Unit
	 */
	public Query buildExtractQuery() {
		StringBuilder sb = new StringBuilder("SELECT ");
		
		for (UnitColumn uc : columns) {
			sb.append(uc).append(", ");
		}
		int length = sb.length();
		sb.delete(length - 2, length);
		
		sb.append(" FROM ").append(tableName).append(";");
		
		Query query = new Query();
		query.setName(tableName);
		query.setSql(sb.toString());
			
		return query;
	}
}
