package com.lotaris.maven.plugin.dbunit;

/*
 * The MIT License
 *
 * Copyright (c) 2006, The Codehaus
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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dbunit.ant.Operation;
import org.dbunit.database.IDatabaseConnection;

/**
 * Execute DbUnit's Database Operation with an external dataset file.
 * 
 * @goal operation
 * 
 * @author Laurent Prevost, laurent.prevost@lotaris.com
 * 
 * @author <a href="mailto:dantran@gmail.com">Dan Tran</a>
 * @author <a href="mailto:topping@codehaus.org">Brian Topping</a>
 */
public class OperationMojo extends AbstractDbUnitMojo {
	/**
	 * Type of Database operation to perform. Supported types are UPDATE, INSERT, DELETE, DELETE_ALL,
	 * REFRESH, CLEAN_INSERT, MSSQL_INSERT, MSSQL_REFRESH, MSSQL_CLEAN_INSERT
	 *
	 * @parameter expression="${type}" 
	 * 
	 * @required
	 */
	protected String type;

	/**
	 * When true, place the entired operation in one transaction
	 *
	 * @parameter expression="${transaction}" default-value="false"
	 */
	protected boolean transaction;

	/**
	 * DataSet file Please use sources instead.
	 *
	 * @parameter expression="${src}"
	 *
	 * @deprecated 1.0
	 */
	protected File src;
    
	/**
	 * DataSet files.
	 *
	 * @parameter
	 */
	protected File[] sources;

	/**
	 * Dataset file format type. Valid types are: flat, xml, csv, and dtd
	 *
	 * @parameter expression="${format}" default-value="xml";
	 *
	 * @required
	 */
	protected String format;
	
	/**
	 * Allows to clear all the tables
	 * 
	 * @parameter default-value="false"
	 */
	protected Boolean clearAllTables;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			this.getLog().info("Skip operation: " + type + " execution");

			return;
		}

		super.execute();

		List concatenatedSources = new ArrayList();
		CollectionUtils.addIgnoreNull(concatenatedSources, src);
		if (sources != null) {
			concatenatedSources.addAll(Arrays.asList(sources));
		}

		try {
			IDatabaseConnection connection = createConnection();
				
			// Force the database table to be empty before importing data
			if (clearAllTables) {
				Connection con = connection.getConnection();
				DatabaseMetaData meta = con.getMetaData();

				// Get the tables from metadata
				ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"});

				// Iterate the results
				while (rs.next()) {
					String tName = rs.getString("TABLE_NAME");

					// Truncate the data
					con.createStatement().execute("TRUNCATE " + tName + ";");
				}

				rs.close();
			}			
			
			try {
				for (Iterator i = concatenatedSources.iterator(); i.hasNext();) {
					File source = (File) i.next();
					Operation op = new Operation();
					op.setFormat(format);
					op.setSrc(source);
					op.setTransaction(transaction);
					op.setType(type);
					op.execute(connection);
				}
			} finally {
				connection.close();
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Error executing database operation: " + type, e);
		}
	}
}

