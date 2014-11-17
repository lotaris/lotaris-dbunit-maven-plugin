package com.lotaris.maven.plugin.dbunit.model;

/**
 * Represent a column information
 * @author Laurent Prévost, laurent.prevost@lotaris.com
 */
public class UnitColumn implements Comparable<UnitColumn> {
	
	private String columnName;
	
	//<editor-fold defaultstate="collapsed" desc="Constructors">
	/**
	 * Constructor
	 * @param columName The column name
	 */
	public UnitColumn(String columName) {
		this.columnName = columName;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Getters & Setters">
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Overrides">
	public int compareTo(UnitColumn o) {
		return columnName.compareTo(o.columnName);
	}
	
	@Override
	public String toString() {
		return columnName;
	}
	//</editor-fold>
}
