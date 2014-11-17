package com.lotaris.maven.plugin.dbunit;

/*
 * The MIT License
 *
 * Copyright (c) 2006, The Codehaus
 * Copyright (c) 2012, Lotaris SA (forked, enriched)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

import com.lotaris.maven.plugin.dbunit.model.UnitTable;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.dbunit.ant.Export;
import org.dbunit.ant.Query;
import org.dbunit.ant.Table;
import org.dbunit.database.IDatabaseConnection;

/**
 * Execute DbUnit Export operation
 *
 * @author <a href="mailto:dantran@gmail.com">Dan Tran</a>
 * @author <a href="mailto:topping@codehaus.org">Brian Topping</a>
 * @author <a href="mailto:david@codehaus.org">David J. M. Karlsen</a>
 * @author Laurent Prévost <laurent.prevost@lotaris.com>
 */
@Mojo(name = "export", requiresDependencyCollection = ResolutionScope.COMPILE)
public class ExportMojo extends AbstractDbUnitMojo {
	/**
	 * Location of exported DataSet file
	 */
	@Parameter(defaultValue = "${project.build.directory}/dbunit/export.xml")
	protected File dest;
	
	/**
	 * DataSet file format
	 */
	@Parameter(defaultValue = "xml")
	protected String format;
	
	/**
	 * doctype
	 */
	@Parameter
	protected String doctype;
	
	/**
	 * List of DbUnit's Table. See DbUnit's JavaDoc for details
	 */
	@Parameter
	protected Table[] tables;
	
	/**
	 * List of DbUnit's Query. See DbUnit's JavaDoc for details
	 */
	@Parameter
	protected Query[] queries;
	
	/**
	 * Set to true to order exported data according to integrity constraints defined in DB.
	 */
	@Parameter
	protected boolean ordered;
	
	/**
	 * Encoding of exported data.
	 */
	@Parameter(defaultValue = "${project.build.sourceEncoding}")
	protected String encoding;
	
	/**
	 * List of table to exclude.
	 */
	@Parameter
	private String[] excludes;
	
	/**
	 * Allows to not extract any data of empty tables
	 */
	@Parameter(defaultValue = "${false}")
	private Boolean excludeEmptyTables = false;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			this.getLog().info("Skip export execution");
			return;
		}

		super.execute();

		try {
			//dbunit require dest directory is ready
			dest.getParentFile().mkdirs();

			IDatabaseConnection connection = createConnection();
			try {
				// Check if the standard behavior of the DBUnit plugin could be overrided or not
				List<UnitTable> tablesToExtract = null;
				if ((tables == null || tables.length == 0) && (queries == null || queries.length == 0)) {
					Connection con = connection.getConnection();
					DatabaseMetaData meta = con.getMetaData();

					// Create the list of tables to extract
					tablesToExtract = new ArrayList<>();
					try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
						while (rs.next()) {
							String tName = rs.getString("TABLE_NAME");
							
							// Flag for exclusion
							boolean excluded = false;
							
							// Check if exclusion rules are configured
							if (excludes != null) {
								// Iterate the exclusion rules
								for (String exclude : excludes) {
									// Check for exclusion
									if (tName.matches(exclude)) {
										excluded = true;
										break;
									}
								}
							}

							// Check if empty tables must be excluded
							if (!excluded && excludeEmptyTables) {
								try (ResultSet cntRs = con.createStatement().executeQuery("SELECT COUNT(*) AS CNT FROM " + tName  + ";")) {
									while (cntRs.next()) {
										if (cntRs.getInt("CNT") == 0) {
											excluded = true;
										}
									}
								}
							}
							
							if (!excluded) {
								// Create the table information
								UnitTable ut = new UnitTable(tName);

								// Add the table to the list of table to extract
								tablesToExtract.add(ut);
								try (ResultSet rst = meta.getColumns(null, null, ut.getTableName(), null)) {
									while (rst.next()) {
										// Get the column name
										ut.addColumn(rst.getString("COLUMN_NAME"));
									}
								}
								
								// Logging
								if (verbose) {
									getLog().info(ut.toString());
								}
							}
						}
					}
				}
					
				Export export = new Export();
				export.setOrdered(ordered);

				// Custom behavior (extract only tables that are wanted with the warranty that the 
				// columns are ordered alphabetically
				if (tablesToExtract != null) {
					for (UnitTable ut : tablesToExtract) {
						Query q = ut.buildExtractQuery();
						if (verbose) {
							getLog().info(q.getName() + " : " + q.getSql());
						}
						export.addQuery(q);
					}
				}
				
				// Standard behavior
				else {
					for (int i = 0; queries != null && i < queries.length; ++i) {
						export.addQuery((Query) queries[i]);
					}
					for (int i = 0; tables != null && i < tables.length; ++i) {
						export.addTable((Table) tables[i]);
					}
				}
					
				// Standard options from dbunit plugin
				export.setDest(dest);
				export.setDoctype(doctype);
				export.setFormat(format);
				export.setEncoding(encoding);

				export.execute(connection);
			} 
			finally {
				connection.close();
			}
		} 
		catch (Exception e) {
			throw new MojoExecutionException("Error executing export", e);
		}
	}
}
