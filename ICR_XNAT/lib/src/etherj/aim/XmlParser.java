/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.aim;

import etherj.Xml;
import etherj.XmlException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jamesd
 */
public class XmlParser
{
	private static final org.slf4j.Logger logger =
		LoggerFactory.getLogger(XmlParser.class);
	private static final String AIM_V4_0 = "AIMv4_0";
	private static final String ATTR_AIM_VERSION = "aimVersion";
	private static final String ATTR_CODE = "code";
	private static final String ATTR_CODE_SYSTEM = "codeSystem";
	private static final String ATTR_CODE_SYSTEM_NAME = "codeSystemName";
	private static final String ATTR_CODE_SYSTEM_VERSION = "codeSystemVersion";
	private static final String ATTR_ROOT = "root";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_XSI_TYPE = "xsi:type";
	private static final String NODE_ANNOTATION = "ImageAnnotation";
	private static final String NODE_ANNOTATIONS = "imageAnnotations";
	private static final String NODE_ANNOTATION_COLLECTION =
		"ImageAnnotationCollection";
	private static final String NODE_COMMENT = "comment";
	private static final String NODE_COORDINATE_INDEX = "coordinateIndex";
	private static final String NODE_DATE_TIME = "dateTime";
	private static final String NODE_EQUIPMENT = "equipment";
	private static final String NODE_EQUIPMENT_MANUFACTURER_NAME =
		"manufacturerName";
	private static final String NODE_EQUIPMENT_MANUFACTURER_MODEL_NAME =
		"manufacturerModelName";
	private static final String NODE_EQUIPMENT_DEVICE_SERIAL_NUMBER =
		"deviceSerialNumber";
	private static final String NODE_EQUIPMENT_SOFTWARE_VERSION =
		"softwareVersion";
	private static final String NODE_IMAGE = "Image";
	private static final String NODE_IMAGE_COLLECTION = "imageCollection";
	private static final String NODE_IMAGE_REFERENCE_UID = "imageReferenceUid";
	private static final String NODE_IMAGE_SERIES = "imageSeries";
	private static final String NODE_IMAGE_STUDY = "imageStudy";
	private static final String NODE_INCLUDE_FLAG = "includeFlag";
	private static final String NODE_INSTANCE_UID = "instanceUid";
	private static final String NODE_MARKUP = "MarkupEntity";
	private static final String NODE_MARKUP_COLLECTION = "markupEntityCollection";
	private static final String NODE_MODALITY = "modality";
	private static final String NODE_NAME = "name";
	private static final String NODE_IMAGE_REFERENCE = "ImageReferenceEntity";
	private static final String NODE_IMAGE_REFERENCE_COLLECTION = "imageReferenceEntityCollection";
	private static final String NODE_REFERENCED_FRAME_NUMBER = "referencedFrameNumber";
	private static final String NODE_PERSON = "person";
	private static final String NODE_PERSON_BIRTHDATE = "birthDate";
	private static final String NODE_PERSON_ETHNIC_GROUP = "ethnicGroup";
	private static final String NODE_PERSON_ID = "id";
	private static final String NODE_PERSON_NAME = "name";
	private static final String NODE_PERSON_SEX = "sex";
	private static final String NODE_SHAPE_ID = "shapeIdentifier";
	private static final String NODE_SOP_CLASS_UID = "sopClassUid";
	private static final String NODE_SOP_INSTANCE_UID = "sopInstanceUid";
	private static final String NODE_START_DATE = "startDate";
	private static final String NODE_START_TIME = "startTime";
	private static final String NODE_TEXT = "#text";
	private static final String NODE_UID = "uniqueIdentifier";
	private static final String NODE_USER = "user";
	private static final String NODE_USER_LOGIN_NAME = "loginName";
	private static final String NODE_USER_NAME = "name";
	private static final String NODE_USER_ROLE = "roleInTrial";
	private static final String NODE_USER_NUMBER_IN_ROLE = "numberWithinRoleOfClinicalTrial";
	private static final String NODE_X = "x";
	private static final String NODE_Y = "y";
	private static final String NODE_2D_COORDINATE = "TwoDimensionSpatialCoordinate";
	private static final String NODE_2D_COORDINATE_COLLECTION = "twoDimensionSpatialCoordinateCollection";

	/**
	 *
	 * @param path
	 * @return
	 * @throws etherj.XmlException
	 * @throws java.io.IOException
	 */
	public static ImageAnnotationCollection parse(String path)
		throws XmlException, IOException
	{
		return parse(new File(path));
	}

	/**
	 *
	 * @param file
	 * @return
	 * @throws etherj.XmlException
	 * @throws java.io.IOException
	 */
	public static ImageAnnotationCollection parse(File file)
		throws XmlException, IOException
	{
		return parse(new FileInputStream(file), file.getAbsolutePath());
	}

	/**
	 *
	 * @param stream
	 * @return
	 * @throws XmlException
	 * @throws IOException
	 */
	public static ImageAnnotationCollection parse(InputStream stream)
		throws XmlException, IOException
	{
		return parse(stream, "");
	}

	/**
	 *
	 * @param stream
	 * @param path
	 * @return
	 * @throws XmlException
	 * @throws IOException
	 */
	public static ImageAnnotationCollection parse(InputStream stream,
		String path) throws XmlException, IOException
	{
		ImageAnnotationCollection iac = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(stream);
			Element rootNode = doc.getDocumentElement();
			if (!rootNode.getNodeName().equals(NODE_ANNOTATION_COLLECTION))
			{
				throw new IllegalArgumentException(
					"Incorrect doc type: "+rootNode.getNodeName());
			}
			rootNode.normalize();
			iac = new ImageAnnotationCollection();
			if ((path != null) && !path.isEmpty())
			{
				iac.setPath(path);
			}
			iac.setAimVersion(rootNode.getAttribute(ATTR_AIM_VERSION));
			NodeList childNodes = rootNode.getChildNodes();
			for (int i=0; i<childNodes.getLength(); i++)
			{
				Node node = childNodes.item(i);
				switch (node.getNodeName())
				{
					case NODE_TEXT:
						continue;

					case NODE_UID:
						iac.setUid(Xml.getAttrStr(node.getAttributes(), ATTR_ROOT));
						break;

					case NODE_DATE_TIME:
						iac.setDateTime(Xml.getAttrStr(node.getAttributes(), ATTR_VALUE));
						break;

					case NODE_USER:
						parseUser(node, iac);
						break;

					case NODE_EQUIPMENT:
						parseEquipment(node, iac);
						break;

					case NODE_PERSON:
						parsePerson(node, iac);
						break;

					case NODE_ANNOTATIONS:
						NodeList annotations = node.getChildNodes();
						for (int j=0; j<annotations.getLength(); j++)
						{
							parseAnnotation(annotations.item(j), iac);
						}

					default:
				}
			}
		}
		catch (ParserConfigurationException | SAXException ex)
		{
			throw new XmlException(ex);
		}
		return iac;
	}

	private static void parseAnnotation(Node annoNode,
		ImageAnnotationCollection iac)
	{
		ImageAnnotation annotation = new ImageAnnotation();
		NodeList childNodes = annoNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_UID:
					annotation.setUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				case NODE_DATE_TIME:
					annotation.setDateTime(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_NAME:
					annotation.setName(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_COMMENT:
					annotation.setComment(Xml.getAttrStr(attrs, ATTR_VALUE));

				case NODE_MARKUP_COLLECTION:
					NodeList markups = node.getChildNodes();
					for (int j=0; j<markups.getLength(); j++)
					{
						parseMarkup(markups.item(j), annotation);
					}
					break;

				case NODE_IMAGE_REFERENCE_COLLECTION:
					NodeList references = node.getChildNodes();
					for (int j=0; j<references.getLength(); j++)
					{
						parseReference(references.item(j), annotation);
					}
					break;

				default:
			}
		}
		if (!annotation.getUid().isEmpty())
		{
			iac.addAnnotation(annotation);
		}
	}

	private static void parseEquipment(Node equipNode, ImageAnnotationCollection iac)
	{
		Equipment equipment = new Equipment();
		NodeList childNodes = equipNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_EQUIPMENT_MANUFACTURER_NAME:
					equipment.setManufacturerName(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_EQUIPMENT_MANUFACTURER_MODEL_NAME:
					equipment.setManufacturerModelName(
						Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_EQUIPMENT_DEVICE_SERIAL_NUMBER:
					equipment.setDeviceSerialNumber(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_EQUIPMENT_SOFTWARE_VERSION:
					equipment.setSoftwareVersion(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				default:
			}
		}
		if (!equipment.getManufacturerName().isEmpty())
		{
			iac.setEquipment(equipment);
		}
	}

	private static void parseImage(Node imageNode, ImageSeries series)
	{
		Image image = new Image();
		NodeList childNodes = imageNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_SOP_CLASS_UID:
					image.setSopClassUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				case NODE_SOP_INSTANCE_UID:
					image.setInstanceUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				default:
			}
		}
		if (!image.getInstanceUid().isEmpty())
		{
			series.addImage(image);
		}
	}

	private static void parseMarkup(Node markupNode, ImageAnnotation annotation)
	{
		TwoDimensionGeometricShape shape = null;
		if (markupNode.getNodeName().equals(NODE_TEXT))
		{
			return;
		}
		String clazz = Xml.getAttrStr(markupNode.getAttributes(), ATTR_XSI_TYPE);
		switch (clazz)
		{
			case "TwoDimensionPolyline":
				shape = new TwoDimensionPolyline();
				break;

			default:
		}
		if (shape == null)
		{
			return;
		}
		NodeList childNodes = markupNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			int number;
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_UID:
					shape.setUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				case NODE_SHAPE_ID:
					number = Xml.getAttrInt(attrs, ATTR_VALUE);
					if (number >= 0)
					{
						shape.setShapeId(number);
					}
					break;

				case NODE_INCLUDE_FLAG:
					shape.setIncludeFlag("true".equals(Xml.getAttrStr(attrs, ATTR_VALUE)));
					break;

				case NODE_IMAGE_REFERENCE_UID:
					shape.setImageReferenceUid(Xml.getAttrStr(
						attrs, XmlParser.ATTR_ROOT));
					break;

				case NODE_REFERENCED_FRAME_NUMBER:
					number = Xml.getAttrInt(attrs, ATTR_VALUE);
					if (number >= 0)
					{
						shape.setReferencedFrameNumber(number);
					}
					break;

				case NODE_2D_COORDINATE_COLLECTION:
					NodeList coords = node.getChildNodes();
					for (int j=0; j<coords.getLength(); j++)
					{
						parseTwoDCoordinate(coords.item(j), shape);
					}
					break;

				default:
			}
		}
		if (!shape.getUid().isEmpty())
		{
			annotation.addMarkup(shape);
		}
	}

	private static void parsePerson(Node personNode, ImageAnnotationCollection iac)
	{
		Person person = new Person();
		NodeList childNodes = personNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_PERSON_BIRTHDATE:
					person.setBirthDate(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_PERSON_ETHNIC_GROUP:
					person.setEthnicGroup(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_PERSON_ID:
					person.setId(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_PERSON_NAME:
					person.setName(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_PERSON_SEX:
					person.setSex(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				default:
			}
		}
		if (!person.getName().isEmpty() && !person.getId().isEmpty())
		{
			iac.setPerson(person);
		}
	}

	private static void parseReference(Node refNode, ImageAnnotation annotation)
	{
		DicomImageReference ref = null;
		if (refNode.getNodeName().equals(NODE_TEXT))
		{
			return;
		}
		NamedNodeMap refAttrs = refNode.getAttributes();
		String clazz = Xml.getAttrStr(refAttrs, XmlParser.ATTR_XSI_TYPE);
		switch (clazz)
		{
			case "DicomImageReferenceEntity":
				ref = new DicomImageReference();
				break;

			default:
		}
		if (ref == null)
		{
			return;
		}
		NodeList childNodes = refNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_UID:
					ref.setUid(Xml.getAttrStr(attrs, XmlParser.ATTR_ROOT));
					break;

				case NODE_IMAGE_STUDY:
					parseStudy(node, ref);
					break;

				default:
			}
		}
		if (!ref.getUid().isEmpty())
		{
			annotation.addReference(ref);
		}
	}

	private static void parseSeries(Node seriesNode, ImageStudy study)
	{
		ImageSeries series = new ImageSeries();
		NodeList childNodes = seriesNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_INSTANCE_UID:
					series.setInstanceUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				case NODE_MODALITY:
					Code modality = series.getModality();
					modality.setCode(Xml.getAttrStr(attrs, ATTR_CODE, ""));
					modality.setCodeSystem(Xml.getAttrStr(attrs, ATTR_CODE_SYSTEM, ""));
					modality.setCodeSystemName(Xml.getAttrStr(attrs, 
						ATTR_CODE_SYSTEM_NAME, ""));
					modality.setCodeSystemVersion(Xml.getAttrStr(attrs,
						ATTR_CODE_SYSTEM_VERSION, ""));
					break;

				case NODE_IMAGE_COLLECTION:
					NodeList images = node.getChildNodes();
					for (int j=0; j<images.getLength(); j++)
					{
						parseImage(images.item(j), series);
					}
					break;

				default:
			}
		}
		if (!series.getInstanceUid().isEmpty())
		{
			study.setSeries(series);
		}
	}

	private static void parseStudy(Node studyNode, DicomImageReference ref)
	{
		ImageStudy study = new ImageStudy();
		NodeList childNodes = studyNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_INSTANCE_UID:
					study.setInstanceUid(Xml.getAttrStr(attrs, ATTR_ROOT));
					break;

				case NODE_START_DATE:
					study.setStartDate(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_START_TIME:
					study.setStartTime(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_IMAGE_SERIES:
					parseSeries(node, study);
					break;

				default:
			}
		}
		if (!study.getInstanceUid().isEmpty())
		{
			ref.setStudy(study);
		}
	}

	private static void parseTwoDCoordinate(Node coordNode,
		TwoDimensionGeometricShape shape)
	{
		NodeList childNodes = coordNode.getChildNodes();
		int idx = -1;
		double x = Double.NaN;
		double y = Double.NaN;
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_COORDINATE_INDEX:
					int value = Xml.getAttrInt(attrs, ATTR_VALUE);
					if (value >= 0)
					{
						idx = value;
					}
					break;

				case NODE_X:
					x = Xml.getAttrDouble(attrs, ATTR_VALUE);
					break;

				case NODE_Y:
					y = Xml.getAttrDouble(attrs, ATTR_VALUE);
					break;

				default:
			}
		}
		if ((idx >= 0) && !Double.isNaN(x) && !Double.isNaN(y))
		{
			shape.addCoordinate(new TwoDimensionCoordinate(idx, x, y));
		}
	}

	private static void parseUser(Node userNode, ImageAnnotationCollection iac)
	{
		User user = new User();
		NodeList childNodes = userNode.getChildNodes();
		for (int i=0; i<childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			NamedNodeMap attrs = node.getAttributes();
			switch (node.getNodeName())
			{
				case NODE_TEXT:
					continue;

				case NODE_USER_NAME:
					user.setName(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_USER_LOGIN_NAME:
					user.setLoginName(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_USER_ROLE:
					user.setRoleInTrial(Xml.getAttrStr(attrs, ATTR_VALUE));
					break;

				case NODE_USER_NUMBER_IN_ROLE:
					user.setNumberWithinRoleOfClinicalTrial(
						Xml.getAttrInt(attrs, ATTR_VALUE, 0));
					break;

				default:
			}
		}
		if (!user.getName().isEmpty() && !user.getLoginName().isEmpty())
		{
			iac.setUser(user);
		}
	}

	private XmlParser()
	{}

}
