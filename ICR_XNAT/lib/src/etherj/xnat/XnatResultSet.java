/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat;

import etherj.Displayable;
import java.util.List;

/**
 *
 * @author jamesd
 */
public interface XnatResultSet extends Displayable
{
	public static final String IntegerType = "integer";
	public static final String StringType = "string";

	/**
	 *
	 * @param row
	 * @param column
	 * @return
	 */
	public String get(int row, int column);

	/**
	 *
	 * @param row
	 * @param columnName
	 * @return
	 */
	public String get(int row, String columnName);

	/**
	 *
	 * @param index
	 * @return
	 */
	public XnatResultSet.Column getColumn(int index);

	/**
	 *
	 * @return
	 */
	public int getColumnCount();

	/**
	 *
	 * @param name
	 * @return
	 */
	public int getColumnIndex(String name);

	/**
	 *
	 * @param elementName
	 * @return
	 */
	public int getColumnIndexByElementName(String elementName);

	/**
	 *
	 * @param header
	 * @return
	 */
	public int getColumnIndexByHeader(String header);

	/**
	 *
	 * @param id
	 * @return
	 */
	public int getColumnIndexById(String id);

	/**
	 *
	 * @param xPath
	 * @return
	 */
	public int getColumnIndexByXPath(String xPath);

	/**
	 *
	 * @param index
	 * @return
	 */
	public String getColumnName(int index);

	/**
	 *
	 * @return
	 */
	public List<XnatResultSet.Column> getColumns();

	/**
	 *
	 * @param row
	 * @param column
	 * @return
	 */
	public int getInt(int row, int column);

	/**
	 *
	 * @param row
	 * @param columnName
	 * @return
	 */
	public int getInt(int row, String columnName);

	/**
	 *
	 * @return
	 */
	public int getRowCount();

	/**
	 *
	 * @return
	 */
	public String getTitle();

	/**
	 *
	 */
	public interface Column extends Displayable
	{
		public String getElementName();
		public String getId();
		public String getHeader();
		public String getName();
		public String getType();
		public String getXPath();
	}

	/**
	 *
	 */
	public interface Row extends List<String>, Displayable
	{
	}
}
