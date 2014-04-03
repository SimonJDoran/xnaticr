/*****************************************************************************
 *
 * DICOMTypes.java
 *
 * Simon J Doran
 *
 * First created on September 19, 2007, 3:40 PM
 *
 *****************************************************************************/

package obselete;

import java.util.Hashtable;
import java.sql.Types;

public class DICOMTypes
{
	private static Hashtable<String, Integer> equivSQLType	= new Hashtable<String, Integer>();
	private static Hashtable<String, Integer> equivSQLn		= new Hashtable<String, Integer>();
	private static Hashtable<String, Integer> equivSQLSize	= new Hashtable<String, Integer>();

	/* equivSQLType tells us what SQL variable type corresponds to each DICOM
	 * value representation. equivSQLSize tells us how long the variable needs
	 * to be. For example, Section 6.2 of the DICOM definition Part 5: Data
	 * Structures and Encoding tells us that a person specifier is at most 320
	 * characters long. Hence, we should use PN = VARCHAR(320).
	 *
	 * However, many fields (potentially) contain multiple person names and this
	 * number might be different for each DICOM file. Hence, a record with a
	 * pre-defined maximum length would not be suitable. A value multiplicity
	 * (VM) that is a priori undetermined is specified in the DICOM element
	 * description with "n". When this occurs, we replace the data type given
	 * in equivSQLType with that in equivSQLn. Hence, fields with value
	 * representation (VR) of "PN" are given the SQL type LONGTEXT.
	 */ 

	/*
	public static final int STRING	= 1;
	public static final int INT		= 2;
	public static final int DATE		= 3;
	public static final int FLOAT		= 4;
	public static final int	DOUBLE	= 5;
	public static final int BYTEARR	= 6;
	 */
	
	static
	{
		equivSQLType.put("AE", Types.VARCHAR);		// Application Entity
		equivSQLn.put   ("AE", Types.LONGVARCHAR);
		equivSQLSize.put("AE", 16);
		
		equivSQLType.put("AS", Types.CHAR);			// Age String
		equivSQLn.put   ("AS", Types.LONGVARCHAR);
		equivSQLSize.put("AS", 4);
		
		equivSQLType.put("AT", Types.INTEGER);		// Attribute tag
		equivSQLn.put   ("AT", Types.BLOB);
		equivSQLSize.put("AT", 1);
		
		equivSQLType.put("CS", Types.VARCHAR);		// Code String
		equivSQLn.put   ("CS", Types.LONGVARCHAR);
		equivSQLSize.put("CS", 16);
		
		equivSQLType.put("DA", Types.DATE);			// Date
		equivSQLn.put   ("DA", Types.LONGVARCHAR);
		equivSQLSize.put("DA", 1);
		
		equivSQLType.put("DS", Types.VARCHAR);		// Decimal String
		equivSQLn.put   ("DS", Types.LONGVARCHAR);
		equivSQLSize.put("DS", 16);
		
		equivSQLType.put("DT", Types.DATE);			// Date Time
		equivSQLn.put   ("DT", Types.LONGVARCHAR);
		equivSQLSize.put("DT", 1);
		
		equivSQLType.put("FL", Types.FLOAT);			// Floating Point Single
		equivSQLn.put   ("FL", Types.BLOB);
		equivSQLSize.put("FL", 1);
		
		equivSQLType.put("FD", Types.DOUBLE);			// Floating Point Double
		equivSQLn.put   ("FD", Types.BLOB);
		equivSQLSize.put("FD", 1);
		
		equivSQLType.put("IS", Types.VARCHAR);			// Integer String
		equivSQLn.put   ("IS", Types.LONGVARCHAR);
		equivSQLSize.put("IS", 12);
		
		equivSQLType.put("LO", Types.VARCHAR);			// Long String
		equivSQLn.put   ("LO", Types.LONGVARCHAR);
		equivSQLSize.put("LO", 64);
		
		equivSQLType.put("LT", Types.LONGVARCHAR);	// Long Text
		equivSQLn.put   ("LT", Types.LONGVARCHAR);
		equivSQLSize.put("LT", 1);
		
		equivSQLType.put("OB", Types.LONGVARBINARY);	// Other Byte String
		equivSQLn.put   ("OB", Types.LONGVARBINARY);	
		equivSQLSize.put("OB", 1);
		
		equivSQLType.put("OF", Types.LONGVARBINARY);	// Other Float String
		equivSQLn.put   ("OF", Types.LONGVARBINARY);
		equivSQLSize.put("OF", 1);
		
		equivSQLType.put("OW", Types.LONGVARBINARY);	// Other Word String
		equivSQLn.put   ("OW", Types.LONGVARBINARY);
		equivSQLSize.put("OW", 1);
		
		equivSQLType.put("PN", Types.VARCHAR);			// Person Name
		equivSQLn.put   ("PN", Types.LONGVARCHAR);
		equivSQLSize.put("PN", 320);
		
		equivSQLType.put("SH", Types.VARCHAR);			// Short String
		equivSQLn.put   ("SH", Types.LONGVARCHAR);
		equivSQLSize.put("SH", 16);
		
		
		equivSQLType.put("SL", Types.INTEGER);			// Signed Long
		equivSQLn.put   ("SL", Types.BLOB);
		equivSQLSize.put("SL", 1);
		
		equivSQLType.put("SQ", Types.LONGVARBINARY);	// Sequence of Items
		equivSQLn.put   ("SQ", Types.LONGVARBINARY);	// For completeness only: the
		equivSQLSize.put("SQ", 1);										// current version does not support
																				// sequences in the DB.
		
		equivSQLType.put("SS", Types.SMALLINT);		// Signed Short
		equivSQLn.put   ("SS", Types.BLOB);
		equivSQLSize.put("SS", 1);
		
		equivSQLType.put("ST", Types.VARCHAR);			// Short Text
		equivSQLn.put   ("ST", Types.LONGVARCHAR);
		equivSQLSize.put("ST", 1024);
		
		equivSQLType.put("TM", Types.TIME);				// Time
		equivSQLn.put   ("TM", Types.LONGVARCHAR);
		equivSQLSize.put("TM", 1);
		
		equivSQLType.put("UI", Types.VARCHAR);			// Unique Identifier
		equivSQLn.put   ("UI", Types.LONGVARCHAR);
		equivSQLSize.put("UI", 64);
		
		equivSQLType.put("UL", Types.INTEGER);			// Unsigned Long
		equivSQLn.put   ("UL", Types.BLOB);
		equivSQLSize.put("UL", 1);
		
		equivSQLType.put("UN", Types.LONGVARBINARY);	// Unknown - safety net, should never appear
		equivSQLn.put   ("UN", Types.LONGVARBINARY );
		equivSQLSize.put("UN", 1);
		
		equivSQLType.put("US", Types.SMALLINT);		// Unsigned Short
		equivSQLn.put   ("US", Types.BLOB );
		equivSQLSize.put("US", 1);
		
		equivSQLType.put("UT", Types.LONGVARCHAR);	// Unlimited Text
		equivSQLn.put   ("UT", Types.LONGVARCHAR);
		equivSQLSize.put("UT", 1);
		
		/* Some entries in the DICOM header are not single types, but specifications
		 * that we have one type or another, e.g., "US | SS". Rather than develop
		 * complicated code that handles every conceivable combination, I simply
		 * look at the various combinations that exist in the DICOM dictionary
		 * and code each one separately. In the cases where the different data
		 * types have different widths, I choose the widest corresponding SQL
		 * type.
		 */
		equivSQLType.put("US|SS", Types.SMALLINT);
		equivSQLn.put   ("US|SS", Types.SMALLINT);
		equivSQLSize.put("US|SS", 1);
		
		equivSQLType.put("US|SS|OW", Types.SMALLINT);
		equivSQLn.put   ("US|SS|OW", Types.BLOB);
		equivSQLSize.put("US|SS|OW", 1);
		
		equivSQLType.put("OB|OW", Types.BLOB);
		equivSQLn.put   ("OB|OW", Types.BLOB );
		equivSQLSize.put("OB|OW", 1);
		
		equivSQLType.put("OW|OB", Types.LONGVARBINARY);
		equivSQLn.put   ("OW|OB", Types.LONGVARBINARY);
		equivSQLSize.put("OW|OB", 1);

		/* Code the case of VR="" explicitly, as this is the easiest way to deal
		 * with this awkward case when setting up the database IMAGE_LEVEL table.
		 */
		equivSQLType.put("", Types.BLOB);
		equivSQLn.put   ("", Types.BLOB);
		equivSQLSize.put("", 1);
	}
	
	
	/** DICOMTypes is never instantiated. */
	private DICOMTypes()
	{

	}
	
	/** Return the SQL equivalent data type for a given DICOM type **/
	public static int getSQLType(String DICOMType)
	{
		return (int) equivSQLType.get(DICOMType);
	}
	
	
	/** Return the SQL data type to use if the length of the field is
	 *  undetermined prior to reading in the data. */
	public static int getSQLn(String DICOMType)
	{
		return (int) equivSQLn.get(DICOMType);
	}
	
	
	
	/** Return the array size to which an SQL variable has to be dimensioned
	 *  to match one of the DICOM data types.
	 */
	public static int getSQLSize(String DICOMType)
	{
		return (Integer) equivSQLSize.get(DICOMType);
	}
	
}
