// DicomBrowser anonymization script
// by Kevin Archie, karchie@npg.wustl.edu
// removes all attributes specified in the
// DICOM Basic Application Level Confidentiality Profile

// Modified by Simon J. Doran Dec 2016 to match DICOM 142 basic profile
// This is a pretty brutal profile and removes almost anything of additional interest.
// However, note that this does NOT remove scan date or any UIDs, which we regard as vital for the integrity of the files.

- (0008,0050)  // Accession Number
- (0018,4000)  // Acquisition Comments
- (0040,4035)  // Actual Human Performers Sequence
- (0040,A078)  // Author Observer Sequence
- (0040,3001)  // Confidentiality Constraint on Patient Data Description
- (0070,0084)  // Content Creator's Name
- (0070,0086)  // Content Creator's Identification Code Sequence
- (0038,0300)  // Current Patient Location
- (4008,011A)  // Distribution Address
- (4008,0119)  // Distribution Name
- (0040,4037)  // Human Performer's Name
- (0040,4036)  // Human Performer's Organization
- (0008,4000)  // Identifying Comments
- (0020,4000)  // Image Comments
- (0028,4000)  // Image Presentation Comments
- (0040,2400)  // Imaging Service Request Comments
- (0008,0081)  // Institution Address
- (0008,0080)  // Institution Name
- (0008,1040)  // Institution Department Name
- (0010,1050)  // Insurance Plan Identification
- (0040,1011)  // Intended Recipients of Results Identification Sequence
- (4008,0111)  // Interpretation Approver Sequence
- (4008,010C)  // Interpretation Author
- (4008,0202)  // Interpretation ID Issuer
- (4008,0102)  // Interpretation Recorder
- (4008,010B)  // Interpretation Text
- (4008,010A)  // Interpretation Transcriber
- (0038,0011)  // Issuer of Admission ID
- (0010,0021)  // Issuer of Patient ID
- (0038,0061)  // Issuer of Service Episode ID
- (0010,1090)  // Medical Record Locator
- (0010,1080)  // Military Rank
- (0020,3406)  // Modified Image Description
- (0008,1060)  // Name of Physician Reading Study
- (0040,1010)  // Names of Intended Recipient of Results
- (0010,2180)  // Occupation
- (0008,1072)  // Operator's Identification Sequence
- (0008,1070)  // Operator's Name
- (0040,2010)  // Order Callback Phone Number
- (0040,2008)  // Order Entered By
- (0040,2009)  // Order Enterer Location
- (0010,1000)  // Other Patient IDs
- (0010,1002)  // Other Patient IDs Sequence
- (0010,1001)  // Other Patient Names
- (0040,A07A)  // Participant Sequence
- (0010,1040)  // Patient Address
- (0038,0500)  // Patient State
- (0040,1004)  // Patient Transport Arrangements
- (0010,1010)  // Patient's Age
(0010,0030) := "19010101"
- (0010,1005)  // Patient's Birth Name
- (0010,0032)  // Patient's Birth Time
- (0038,0400)  // Patient's Institution Residence
- (0010,0050)  // Patient's Insurance Plan Code Sequence
- (0010,1060)  // Patient's Mother's Birth Name
- (0010,0010)  // Patient's Name
- (0010,0101)  // Patient's Primary Language Code Sequence
- (0010,0102)  // Patient's Primary Language Modifier Code Sequence
- (0010,21F0)  // Patient's Religious Preference
- (0010,1020)  // Patient's Size
- (0010,2154)  // Patient's Telephone Number
- (0010,1030)  // Patient's Weight
- (0040,0243)  // Performed Location
- (0040,0241)  // Performed Station AE Title
- (0040,4030)  // Performed Station Geographic Location Code Sequence
- (0040,0242)  // Performed Station Name
- (0040,0248)  // Performed Station Name Code Sequence
- (0008,1052)  // Performing Physician's Identifying Sequence
- (0008,1050)  // Performing Physician's Name
- (0040,1102)  // Person Address
- (0040,1101)  // Person Identification Code Sequence
(0040,A123) := "Anon" // Person Name
- (0040,1103)  // Person Telephone Numbers
- (4008,0114)  // Physician Approving Interpretation
- (0008,1062)  // Physician Reading Study Identification Sequence
- (0008,1048)  // Physician(s) of Record)
- (0008,1049)  // Physician(s) of Record Identification Sequence
- (0040,2016)  // Placer Order Number of Imaging Service Request
- (0018,1004)  // Plate ID
- (0040,0012)  // Pre-Medication
- (0010,21C0)  // Pregnancy Status
- (0018,1030)  // Protocol Name
- (0040,2001)  // Reason for Imaging Service Request
- (0032,1030)  // Reason for Study
- (0400,0402)  // Referenced Digital Signature Sequence
- (0038,1234)  // Reference Patient Alias Sequence
- (0008,1120)  // Referenced Patient Sequence
- (0008,0092)  // Referring Physician's Address
- (0008,0096)  // Referring Physician's Identification Sequence
- (0008,0090)  // Referring Physician's Name
- (0008,0094)  // Referring Physician's Telephone Numbers
- (0010,2152)  // Region of Residence
- (0040,1400)  // Requested Procedure Comments
- (0032,1060)  // Requested Procedure Description
- (0040,1001)  // Requested Procedure ID
- (0040,1005)  // Requested Procedure Location
- (0032,1032)  // Requesting Physician
- (0032,1032)  // Requesting Service
- (0010,2299)  // Responsible Organization
- (0010,2297)  // Responsible Person
- (4008,4000)  // Results Comments
- (4008,0118)  // Results Distribution List Sequence
- (4008,0042)  // Results ID issuer
- (300E,0008)  // Reviewer Name
- (0040,4034)  // Scheduled Human Performers Sequence
- (0038,001E)  // Scheduled Patient Institution Residence
- (0040,0008)  // Scheduled Performing Physician Identification Sequence
- (0040,0006)  // Scheduled Performing Physician Name
- (0040,0011)  // Scheduled Procedure Step Location
- (0040,0010)  // Scheduled Station AE Title
- (0040,4027)  // Scheduled Station Geographic Location Code Sequence
- (0040,0010)  // Scheduled Station Name
- (0040,4025)  // Scheduled Station Name Code Sequence
- (0032,1020)  // Scheduled Study Location
- (0032,1021)  // Scheduled Study Location AE Title
- (0010,21A0)  // Smoking Status
- (0038,0050)  // Special Needs
- (0008,1010)  // Station Name
- (0032,4000)  // Study Comments
- (0008,1030)  // Study Description
- (0020,0010)  // Study ID
- (0032,0012)  // Study ID Issuer
- (4000,4000)  // Text Comments
//- (2030,0020)  // Text String
- (0088,0910)  // Topic Author
- (0088,0912)  // Topic Key Words
- (0088,0906)  // Topic Subject
- (0088,0904)  // Topic Title
- (0040,A088)  // Verifying Observer Identification Code Sequence
(0040,A075) := "Anon" // Verifying Observer Name
- (0040,A073)  // Verifying Observer Sequence
- (0040,A027)  // Verifying Organization
- (0038,4000)  // Visit Comments

// Delete overlays
-(6000,0010)
-(6000,0011)
-(6000,0040)
-(6000,0050)
-(6000,0100)
-(6000,0102)
-(6000,3000)
-(6001,0010)
-(6001,1001)

(0012,0062) := "YES" // Patient identity removed
(0012,0063) := "DicomEdit 4 anonymisation script" // Deidentification method 


// Use information from the DICOM file to modify the routing of the scan when it gets to XNAT.

// The use of format[] and replace[] here allows us to cope gracefully with the situation where a
// field is absent. The "null" entry caused XNAT problems on import.

describe scanDate1 hidden
describe scanDate2 hidden
scanDate1 := format["{0}", (0008,0020)]
scanDate2 := replace[scanDate1, "null", "noDate"]

// Use Study Time, not Series Time or Acquisition Time, because the other two could
// potentially generate different labels for each DICOM file, rather than put all data from
// the same study into the same XNAT session

describe scanTime1 hidden
describe scanTime2 hidden
scanTime1 := format["{0}", (0008,0030)]  
scanTime2 := substring[replace[scanTime1, "null", "noTime"], 0, 6]


describe scannerName1 hidden
describe scannerName2 hidden
scannerName1 := format["_{0}", (0008,1090)]
scannerName2 := replace[scannerName1, "_null", ""]

describe sessionName1 hidden
describe sessionName2 hidden
describe sessionName3 hidden
sessionName1 := "{0}_{1}{2}", scanDate2, scanTime2, scannerName2
sessionName2 := "<INSERTED-SESSION-NAME>"
sessionName3 := replace[sessionName2, "DEFAULT", sessionName1]

(0010,0010) := "<INSERTED-SUBJ-NAME>"
(0010,0010) := "<INSERTED-SUBJ-ID>"
(0010,4000) := "Project: <INSERTED-PROJ-NAME>; Subject: <INSERTED-SUBJ-NAME>; Session: {0}; AA:true" sessionName3 // Patient comments tag = XNAT routing info



