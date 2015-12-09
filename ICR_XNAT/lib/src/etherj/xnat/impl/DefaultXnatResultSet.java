/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.xnat.impl;

import etherj.Xml;
import etherj.xnat.XnatResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jamesd
 */
public class DefaultXnatResultSet implements XnatResultSet
{
	private static final Logger logger =
		LoggerFactory.getLogger(DefaultXnatResultSet.class);

	private static final String ATTR_ELEMENT_NAME = "element_name";
	private static final String ATTR_HEADER = "header";
	private static final String ATTR_ID = "id";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_XPATH = "xPATH";
	private static final String ATTR_TITLE = "title";
	private static final String ATTR_TOTAL_RECORDS = "totalRecords";
	private static final String NODE_CELL = "cell";
	private static final String NODE_COLUMN = "column";
	private static final String NODE_COLUMNS = "columns";
	private static final String NODE_ROW = "row";
	private static final String NODE_ROWS = "rows";
	private static final String NODE_RESULT_SET = "ResultSet";
	private static final String NODE_RESULTS = "results";
	private static final String NODE_TEXT = "#text";

	private final List<XnatResultSet.Column> columns = new ArrayList<>();
	private final List<XnatResultSet.Row> rows = new ArrayList<>();
	private final Map<String,Integer> elementNameToIdx = new HashMap<>();
	private final Map<String,Integer> headerToIdx = new HashMap<>();
	private final Map<String,Integer> idToIdx = new HashMap<>();
	private final Map<String,Integer> nameToIdx = new HashMap<>();
	private final Map<String,Integer> xPathToIdx = new HashMap<>();
	private String title;

	DefaultXnatResultSet(Document doc)
	{
		Element rootNode = doc.getDocumentElement();
		if (!rootNode.getNodeName().equals(NODE_RESULT_SET))
		{
			throw new IllegalArgumentException(
				"Incorrect doc type: "+rootNode.getNodeName());
		}
		rootNode.normalize();
		int totalRecords = -1;
		try
		{
			String records = rootNode.getAttribute(ATTR_TOTAL_RECORDS);
			if (!records.isEmpty())
			{
				totalRecords = Integer.parseInt(records);
			}
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalArgumentException("Invalid totalRecords attribute: "+
				rootNode.getAttribute(ATTR_TOTAL_RECORDS));
		}
		String value = rootNode.getAttribute(ATTR_TITLE);
		title = (value != null) ? value : "";
		NodeList childNodes = rootNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_RESULTS:
					parseResults(node);
					break;

				default:
			}
		}
		if ((totalRecords > 0) && (rows.size() != totalRecords))
		{
			logger.warn("Records found: {}, expected {}", rows.size(), totalRecords);
		}
	}

	@Override
	public void display()
	{
		display("", false);
	}

	@Override
	public void display(boolean recurse)
	{
		display("", recurse);
	}

	@Override
	public void display(String indent)
	{
		display(indent, false);
	}

	@Override
	public void display(String indent, boolean recurse)
	{
		System.out.println(indent+getClass().getName());
		String pad = indent+"  * ";
		if (title != null)
		{
			System.out.println(pad+"Title: "+title);
		}
		if (columns.isEmpty())
		{
			return;
		}
		System.out.println(pad+"ColumnCount: "+columns.size());
		if (!recurse)
		{
			StringBuilder sb = new StringBuilder("  "+pad);
			for (XnatResultSet.Column column : columns)
			{
				sb.append(column.getName()).append(" || ");
			}
			sb.setLength(sb.length()-4);
			System.out.println(sb.toString());
			System.out.println(pad+"RowCount: "+rows.size());
			return;
		}
		for (XnatResultSet.Column column : columns)
		{
			column.display(indent+"  ");
		}
		System.out.println(pad+"RowCount: "+rows.size());
		StringBuilder sb = new StringBuilder("  "+pad);
		for (XnatResultSet.Column column : columns)
		{
			sb.append(column.getName()).append(" || ");
		}
		sb.setLength(sb.length()-4);
		System.out.println(sb.toString());
		for (XnatResultSet.Row row : rows)
		{
			row.display(indent+"  ");
		}
	}

	@Override
	public String get(int rowIdx, int columnIdx)
	{
		return rows.get(rowIdx).get(columnIdx);
	}

	@Override
	public String get(int rowIdx, String columnName)
	{
		Integer idx = nameToIdx.get(columnName);
		if (idx == null)
		{
			throw new IllegalArgumentException("Invalid column name: "+columnName);
		}
		return get(rowIdx, idx);
	}

	@Override
	public int getColumnCount()
	{
		return columns.size();
	}

	@Override
	public XnatResultSet.Column getColumn(int index)
	{
		return columns.get(index);
	}

	@Override
	public int getColumnIndex(String name)
	{
		Integer idx = nameToIdx.get(name);
		return (idx != null) ? idx : -1;
	}

	@Override
	public int getColumnIndexByElementName(String elementName)
	{
		Integer idx = elementNameToIdx.get(elementName);
		return (idx != null) ? idx : -1;
	}

	@Override
	public int getColumnIndexByHeader(String header)
	{
		Integer idx = headerToIdx.get(header);
		return (idx != null) ? idx : -1;
	}

	@Override
	public int getColumnIndexById(String id)
	{
		Integer idx = idToIdx.get(id);
		return (idx != null) ? idx : -1;
	}

	@Override
	public int getColumnIndexByXPath(String xPath)
	{
		Integer idx = xPathToIdx.get(xPath);
		return (idx != null) ? idx : -1;
	}

	@Override
	public String getColumnName(int index)
	{
		return columns.get(index).getName();
	}

	@Override
	public List<XnatResultSet.Column> getColumns()
	{
		return Collections.unmodifiableList(columns);
	}

	@Override
	public int getInt(int rowIdx, int columnIdx)
	{
		return Integer.parseInt(rows.get(rowIdx).get(columnIdx));
	}

	@Override
	public int getInt(int rowIdx, String columnName)
	{
		Integer idx = nameToIdx.get(columnName);
		if (idx == null)
		{
			throw new IllegalArgumentException("Invalid column name: "+columnName);
		}
		return Integer.parseInt(get(rowIdx, idx));
	}

	@Override
	public int getRowCount()
	{
		return rows.size();
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	private void processColumn(Node colNode, int idx)
	{
		if (colNode.getNodeName().equals(NODE_TEXT))
		{
			return;
		}
		Node textNode = colNode.getFirstChild();
		if (textNode == null)
		{
			return;
		}
		String value = textNode.getNodeValue();
		NamedNodeMap attrs = colNode.getAttributes();
		String type = Xml.getAttrStr(attrs, ATTR_HEADER, StringType);
		Column column = new Column(value, idx, type);
		columns.add(column);
		nameToIdx.put(value, idx);
		String elementName = Xml.getAttrStr(attrs, ATTR_ELEMENT_NAME);
		column.setElementName(elementName);
		insertToMap(elementName, elementNameToIdx, idx);
		String header = Xml.getAttrStr(attrs, ATTR_HEADER);
		column.setHeader(header);
		insertToMap(Xml.getAttrStr(attrs, ATTR_HEADER), headerToIdx, idx);
		String id = Xml.getAttrStr(attrs, ATTR_ID);
		column.setId(id);
		insertToMap(id, idToIdx, idx);
		String xPath = Xml.getAttrStr(attrs, ATTR_XPATH);
		column.setXPath(xPath);
		insertToMap(xPath, xPathToIdx, idx);
	}

	private void parseResults(Node resultNode)
	{
		NodeList childNodes = resultNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_COLUMNS:
					NodeList colNodes = node.getChildNodes();
					for (int j=0; j<colNodes.getLength(); j++)
					{
						processColumn(colNodes.item(j), j);
					}
					break;

				case NODE_ROWS:
					NodeList rowNodes = node.getChildNodes();
					for (int j=0; j<rowNodes.getLength(); j++)
					{
						parseRow(rowNodes.item(j));
					}
					break;

				default:
			}
		}
	}

	private void parseRow(Node rowNode)
	{
		if (rowNode.getNodeName().equals(NODE_TEXT))
		{
			return;
		}
		Row row = new Row();
		NodeList childNodes = rowNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_CELL:
					Node textNode = node.getFirstChild();
					row.add((textNode != null) ? textNode.getNodeValue() : "");
					break;

				default:
			}
		}
		if (row.size() == columns.size())
		{
			rows.add(row);
		}
		else
		{
			logger.error(
				"Row length doesn't match column count. Expected: {} Actual: {}",
				columns.size(), row.size());
		}
	}

	private void insertToMap(String attrStr, Map<String, Integer> map, int idx)
	{
		if (attrStr != null)
		{
			map.put(attrStr, idx);
		}
	}

	/*
	 * Setters only visible in this class so they can be built here but are
	 * effectively immutable outside without a class cast. Setters prevent nulls
	 * being set.
	 */
	private class Column implements XnatResultSet.Column
	{
		private String elementName = "";
		private String header = "";
		private String id = "";
		private final int idx;
		private final String name;
		private String type = StringType;
		private String xPath = "";

		Column(String name, int idx, String type)
		{
			if ((name == null) || (idx < 0))
			{
				throw new IllegalArgumentException(
					"Column must have non-null name and index >= 0");
			}
			this.name = name;
			this.idx = idx;
		}

		@Override
		public void display()
		{
			display("", false);
		}

		@Override
		public void display(boolean recurse)
		{
			display("", recurse);
		}

		@Override
		public void display(String indent)
		{
			display(indent, false);
		}

		@Override
		public void display(String indent, boolean recurse)
		{
			System.out.println(indent+getClass().getName());
			String pad = indent+"  * ";
			System.out.println(pad+"Name: "+name);
			System.out.println(pad+"Type: "+type);
			System.out.println(pad+"Index: "+idx);
			if (!header.isEmpty())
			{
				System.out.println(pad+"Header: "+header);
			}
			if (!id.isEmpty())
			{
				System.out.println(pad+"Id: "+id);
			}
			if (!elementName.isEmpty())
			{
				System.out.println(pad+"ElementName: "+elementName);
			}
			if (!xPath.isEmpty())
			{
				System.out.println(pad+"XPath: "+xPath);
			}
		}

		@Override
		public String getElementName()
		{
			return elementName;
		}

		@Override
		public String getHeader()
		{
			return header;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String getType()
		{
			return type;
		}

		@Override
		public String getXPath()
		{
			return xPath;
		}

		/**
		 * @param elementName the elementName to set
		 */
		public void setElementName(String elementName)
		{
			this.elementName = (elementName != null) ? elementName : "";
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id)
		{
			this.id = (id != null) ? id : "";
		}

		/**
		 * @param header the header to set
		 */
		public void setHeader(String header)
		{
			this.header = (header != null) ? header : "";
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type)
		{
			this.type = (type != null) ? type : "";
		}

		/**
		 * @param xPath the xPath to set
		 */
		public void setXPath(String xPath)
		{
			this.xPath = (xPath != null) ? xPath : "";
		}
	}

	private class Row extends ArrayList<String> implements XnatResultSet.Row
	{
		@Override
		public void display()
		{
			display("", false);
		}

		@Override
		public void display(boolean recurse)
		{
			display("", recurse);
		}

		@Override
		public void display(String indent)
		{
			display(indent, false);
		}

		@Override
		public void display(String indent, boolean recurse)
		{
			System.out.println(indent+getClass().getName());
			String pad = indent+"  * ";
			StringBuilder sb = new StringBuilder(pad);
			for (String value : this)
			{
				sb.append(value).append(" || ");
			}
			sb.setLength(sb.length()-4);
			System.out.println(sb.toString());
		}
	}

}
