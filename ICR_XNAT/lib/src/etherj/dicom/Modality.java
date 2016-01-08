/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etherj.dicom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jamesd
 */
public class Modality
{
	public static final String AU = "AU";
	public static final String BI = "BI";
	public static final String CD = "CD";
	public static final String CR = "CR";
	public static final String CT = "CT";
	public static final String DD = "DD";
	public static final String DG = "DG";
	public static final String DSA = "DSA";
	public static final String DX = "DX";
	public static final String ECG = "ECG";
	public static final String EPS = "EPS";
	public static final String ES = "ES";
	public static final String GM = "GM";
	public static final String HC = "HC";
	public static final String HD = "HD";
	public static final String IO = "IO";
	public static final String IVUS = "IVUS";
	public static final String LS = "LS";
	public static final String MG = "MG";
	public static final String MR = "MR";
	public static final String NM = "NM";
	public static final String OCT = "OCT";
	public static final String OP = "OP";
	public static final String OPM = "OPM";
	public static final String OPR = "OPR";
	public static final String OPV = "OPV";
	public static final String OT = "OT";
	public static final String PR = "PR";
	public static final String PET = "PET";
	public static final String PX = "PX";
	public static final String REG = "REG";
	public static final String RF = "RF";
	public static final String RG = "RG";
	public static final String RTDOSE = "RTDOSE";
	public static final String RTIMAGE = "RTIMAGE";
	public static final String RTPLAN = "RTPLAN";
	public static final String RTRECORD = "RTRECORD";
	public static final String RTSTRUCT = "RTSTRUCT";
	public static final String SEG = "SEG";
	public static final String SM = "SM";
	public static final String SMR = "SMR";
	public static final String SR = "SR";
	public static final String ST = "ST";
	public static final String TG = "TG";
	public static final String US = "US";
	public static final String XA = "XA";
	public static final String XC = "XC";
	private static final long AU_MASK = 1L;
	private static final long BI_MASK = 1L << 1;
	private static final long CD_MASK = 1L << 2;
	private static final long CR_MASK = 1L << 3;
	private static final long CT_MASK = 1L << 4;
	private static final long DD_MASK = 1L << 5;
	private static final long DG_MASK = 1L << 6;
	private static final long DSA_MASK = 1L << 7;
	private static final long DX_MASK = 1L << 8;
	private static final long ECG_MASK = 1L << 9;
	private static final long EPS_MASK = 1L << 10;
	private static final long ES_MASK = 1L << 11;
	private static final long GM_MASK = 1L << 12;
	private static final long HC_MASK = 1L << 13;
	private static final long HD_MASK = 1L << 14;
	private static final long IO_MASK = 1L << 15;
	private static final long IVUS_MASK = 1L << 16;
	private static final long LS_MASK = 1L << 17;
	private static final long MG_MASK = 1L << 18;
	private static final long MR_MASK = 1L << 19;
	private static final long NM_MASK = 1L << 20;
	private static final long OCT_MASK = 1L << 21;
	private static final long OP_MASK = 1L << 22;
	private static final long OPM_MASK = 1L << 23;
	private static final long OPR_MASK = 1L << 24;
	private static final long OPV_MASK = 1L << 25;
	private static final long OT_MASK = 1L << 26;
	private static final long PR_MASK = 1L << 27;
	private static final long PET_MASK = 1L << 28;
	private static final long PX_MASK = 1L << 29;
	private static final long REG_MASK = 1L << 30;
	private static final long RF_MASK = 1L << 31;
	private static final long RG_MASK = 1L << 32;
	private static final long RTDOSE_MASK = 1L << 33;
	private static final long RTIMAGE_MASK = 1L << 34;
	private static final long RTPLAN_MASK = 1L << 35;
	private static final long RTRECORD_MASK = 1L << 36;
	private static final long RTSTRUCT_MASK = 1L << 37;
	private static final long SEG_MASK = 1L << 38;
	private static final long SM_MASK = 1L << 39;
	private static final long SMR_MASK = 1L << 40;
	private static final long SR_MASK = 1L << 41;
	private static final long ST_MASK = 1L << 42;
	private static final long TG_MASK = 1L << 43;
	private static final long US_MASK = 1L << 44;
	private static final long XA_MASK = 1L << 45;
	private static final long XC_MASK = 1L << 46;
	private static final Map<String,Long> stringToMask = new HashMap<>();
	private static final Map<Long,String> maskToString = new HashMap<>();
	private static final Map<String,String> stringToDesc = new HashMap<>();

	static
	{
		stringToMask.put(AU, AU_MASK);
		stringToMask.put(BI, BI_MASK);
		stringToMask.put(CD, CD_MASK);
		stringToMask.put(CR, CR_MASK);
		stringToMask.put(CT, CT_MASK);
		stringToMask.put(DD, DD_MASK);
		stringToMask.put(DG, DG_MASK);
		stringToMask.put(DSA, DSA_MASK);
		stringToMask.put(DX, DX_MASK);
		stringToMask.put(ECG, ECG_MASK);
		stringToMask.put(EPS, EPS_MASK);
		stringToMask.put(ES, ES_MASK);
		stringToMask.put(GM, GM_MASK);
		stringToMask.put(HC, HC_MASK);
		stringToMask.put(HD, HD_MASK);
		stringToMask.put(IO, IO_MASK);
		stringToMask.put(IVUS, IVUS_MASK);
		stringToMask.put(LS, LS_MASK);
		stringToMask.put(MG, MG_MASK);
		stringToMask.put(MR, MR_MASK);
		stringToMask.put(NM, NM_MASK);
		stringToMask.put(OCT, OCT_MASK);
		stringToMask.put(OP, OP_MASK);
		stringToMask.put(OPM, OPM_MASK);
		stringToMask.put(OPR, OPR_MASK);
		stringToMask.put(OPV, OPV_MASK);
		stringToMask.put(OT, OT_MASK);
		stringToMask.put(PR, PR_MASK);
		stringToMask.put(PET, PET_MASK);
		stringToMask.put(PX, PX_MASK);
		stringToMask.put(REG, REG_MASK);
		stringToMask.put(RF, RF_MASK);
		stringToMask.put(RG, RG_MASK);
		stringToMask.put(RTDOSE, RTDOSE_MASK);
		stringToMask.put(RTIMAGE, RTDOSE_MASK);
		stringToMask.put(RTPLAN, RTPLAN_MASK);
		stringToMask.put(RTRECORD, RTRECORD_MASK);
		stringToMask.put(RTSTRUCT, RTSTRUCT_MASK);
		stringToMask.put(SEG, SEG_MASK);
		stringToMask.put(SM, SM_MASK);
		stringToMask.put(SMR, SMR_MASK);
		stringToMask.put(SR, SR_MASK);
		stringToMask.put(ST, ST_MASK);
		stringToMask.put(TG, TG_MASK);
		stringToMask.put(US, US_MASK);
		stringToMask.put(XA, XA_MASK);
		stringToMask.put(XC, XC_MASK);

		maskToString.put(AU_MASK, AU);
		maskToString.put(BI_MASK, BI);
		maskToString.put(CD_MASK, CD);
		maskToString.put(CR_MASK, CR);
		maskToString.put(CT_MASK, CT);
		maskToString.put(DD_MASK, DD);
		maskToString.put(DG_MASK, DG);
		maskToString.put(DSA_MASK, DSA);
		maskToString.put(DX_MASK, DX);
		maskToString.put(ECG_MASK, ECG);
		maskToString.put(EPS_MASK, EPS);
		maskToString.put(ES_MASK, ES);
		maskToString.put(GM_MASK, GM);
		maskToString.put(HC_MASK, HC);
		maskToString.put(HD_MASK, HD);
		maskToString.put(IO_MASK, IO);
		maskToString.put(IVUS_MASK, IVUS);
		maskToString.put(LS_MASK, LS);
		maskToString.put(MG_MASK, MG);
		maskToString.put(MR_MASK, MR);
		maskToString.put(NM_MASK, NM);
		maskToString.put(OCT_MASK, OCT);
		maskToString.put(OP_MASK, OP);
		maskToString.put(OPM_MASK, OPM);
		maskToString.put(OPR_MASK, OPR);
		maskToString.put(OPV_MASK, OPV);
		maskToString.put(OT_MASK, OT);
		maskToString.put(PR_MASK, PR);
		maskToString.put(PET_MASK, PET);
		maskToString.put(PX_MASK, PX);
		maskToString.put(REG_MASK, REG);
		maskToString.put(RF_MASK, RF);
		maskToString.put(RG_MASK, RG);
		maskToString.put(RTDOSE_MASK, RTDOSE);
		maskToString.put(RTIMAGE_MASK, RTDOSE);
		maskToString.put(RTPLAN_MASK, RTPLAN);
		maskToString.put(RTRECORD_MASK, RTRECORD);
		maskToString.put(RTSTRUCT_MASK, RTSTRUCT);
		maskToString.put(SEG_MASK, SEG);
		maskToString.put(SM_MASK, SM);
		maskToString.put(SMR_MASK, SMR);
		maskToString.put(SR_MASK, SR);
		maskToString.put(ST_MASK, ST);
		maskToString.put(TG_MASK, TG);
		maskToString.put(US_MASK, US);
		maskToString.put(XA_MASK, XA);
		maskToString.put(XC_MASK, XC);

		stringToDesc.put(AU, "Audio");
		stringToDesc.put(BI, "Biomagnetic Imaging");
		stringToDesc.put(CD, "Color Flow Doppler");
		stringToDesc.put(CR, "Computed Radiography");
		stringToDesc.put(CT, "Computed Tomography");
		stringToDesc.put(DD, "Duplex Doppler");
		stringToDesc.put(DG, "Diaphanography");
		stringToDesc.put(DSA, "Digital Subtraction Angiography");
		stringToDesc.put(DX, "Digital Radiography");
		stringToDesc.put(ECG, "Electrocardiography");
		stringToDesc.put(EPS, "Cardiac Electrophysiology");
		stringToDesc.put(ES, "Endoscopy");
		stringToDesc.put(GM, "General Microscopy");
		stringToDesc.put(HC, "Hard Copy");
		stringToDesc.put(HD, "Hemodynamic Waveform");
		stringToDesc.put(IO, "Intra-Oral Radiography");
		stringToDesc.put(IVUS, "Intravascular Ultrasound");
		stringToDesc.put(LS, "Laser Surface Scan");
		stringToDesc.put(MG, "Mammography");
		stringToDesc.put(MR, "Magnetic Resonance");
		stringToDesc.put(NM, "Nuclear Medicine");
		stringToDesc.put(OCT, "Optical Coherence Tomography");
		stringToDesc.put(OP, "Ophthalmic Photography");
		stringToDesc.put(OPM, "Ophthalmic Mapping");
		stringToDesc.put(OPR, "Ophthalmic Refraction");
		stringToDesc.put(OPV, "Ophthalmic Visual Field");
		stringToDesc.put(OT, "Other");
		stringToDesc.put(PR, "Presentation State");
		stringToDesc.put(PET, "Positron Emission Tomography");
		stringToDesc.put(PX, "Panoramic X-Ray");
		stringToDesc.put(REG, "Registration");
		stringToDesc.put(RF, "Radio Fluoroscopy");
		stringToDesc.put(RG, "Radiographic Imaging");
		stringToDesc.put(RTDOSE, "Radiotherapy Dose");
		stringToDesc.put(RTIMAGE, "Radiotherapy Image");
		stringToDesc.put(RTPLAN, "Radiotherapy Plan");
		stringToDesc.put(RTRECORD, "RT Treatment Record");
		stringToDesc.put(RTSTRUCT, "Radiotherapy Structure Set");
		stringToDesc.put(SEG, "Segmentation");
		stringToDesc.put(SM, "Slide Microscopy");
		stringToDesc.put(SMR, "Stereometric Relationship");
		stringToDesc.put(SR, "SR Document");
		stringToDesc.put(ST, "Single-Photon Emission Computed Tomography");
		stringToDesc.put(TG, "Thermography");
		stringToDesc.put(US, "Ultrasound");
		stringToDesc.put(XA, "X-Ray Angiography");
		stringToDesc.put(XC, "External-Camera Photography");
	}

	public static long bitmask(String modality)
	{
		Long value = stringToMask.get(modality);
		return (value == null) ? 0L : value;
	}

	public static String description(String modality)
	{
		return stringToDesc.get(modality);
	}

	public static String string(long bitmask)
	{
		return maskToString.get(bitmask);
	}

	public static String allStrings(long modality)
	{
		String value = "";
		Set<Long> keys = maskToString.keySet();
		for (long key : keys)
		{
			key &= modality;
			String strValue = maskToString.get(key);
			if (strValue != null)
			{
				value += strValue+",";
			}
		}
		return value.substring(0, value.length()-1);
	}
}
