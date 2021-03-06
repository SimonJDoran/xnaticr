/*****************************************************************************
 *
 * DicomObjectSerializerSJD.java
 *
 * Simon J Doran
 *
 * First created on December 4, 2007, 4:33 PM
 *
 * For some reason the version of DicomObjectSerializer in the library
 * appears to be restricted for use within the org.dcm4che2.data. This
 * is just a copy for me to use externally. Not quite sure why it is
 * needed.
 *
 *****************************************************************************/

package obselete;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

public class DicomObjectSerializerSJD implements Serializable {

	private static final long serialVersionUID = 3257002159626139955L;

	private transient DicomObject attrs;

	public DicomObjectSerializerSJD(DicomObject attrs) {
		this.attrs = attrs;
	}
		
	private Object readResolve()
			throws ObjectStreamException {
		return attrs;
	}

	private void writeObject(ObjectOutputStream s)
			throws IOException {
		s.defaultWriteObject();
		DicomOutputStream dos = new DicomOutputStream((OutputStream) s);
		dos.writeDicomFile(attrs);
	}

	private void readObject(ObjectInputStream s)
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		DicomInputStream dis = 
				new DicomInputStream(s, TransferSyntax.ExplicitVRLittleEndian);
		
		attrs = new BasicDicomObject();
		dis.readDicomObject(attrs, -1);
	}
}
