// anonScriptSimple.das
// DicomEdit anonymization script
// DICOM Basic Application Level Confidentiality Profile

// Original by Kevin Archie
// Modified by Simon J. Doran (Simon.Doran@icr.ac.uk) 6.2.15

- (0008,0092)  // Referring Physician's Address
- (0008,0094)  // Referring Physician's Telephone Numbers
- (0010,0020)  // Patient ID
- (0010,0030)  // Patient's Birth Date
- (0010,0032)  // Patient's Birth Time
- (0010,1001)  // Other Patient Names
- (0010,1090)  // Medical Record Locator
- (0010,2180)  // Occupation

(0010,4000) := "Project: <INSERTED-PROJ-ID>; Subject: <INSERTED-SUBJ-NAME>; Session: {0}_{1}_{2}; AA:true" (0008,0020), substring[(0008,0030), 0, 6], (0008,1090) // Patient comments
(0010,0010) := <INSERTED-SUBJ-NAME>