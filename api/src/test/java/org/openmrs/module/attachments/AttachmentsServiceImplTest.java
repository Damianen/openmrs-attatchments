package org.openmrs.module.attachments;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

public class AttachmentsServiceImplTest {
	
	@Test
	public void buildPatientAttachmentFetchLogMessage_shouldNotIncludePatientPii() throws Exception {
		Patient patient = new Patient();
		patient.setPatientId(123);
		patient.setUuid("patient-uuid-123");
		patient.addName(new PersonName("Alice", "Secret", "Patient"));
		patient.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-02"));
		patient.addIdentifier(new PatientIdentifier("MRN-12345", null, null));
		
		String message = AttachmentsServiceImpl.buildPatientAttachmentFetchLogMessage(patient, true, false);
		
		assertTrue(message.contains("patientUuid=patient-uuid-123"));
		assertTrue(message.contains("includeEncounterless=true"));
		assertTrue(message.contains("includeVoided=false"));
		assertFalse(message.contains("id=123"));
		assertFalse(message.contains("Alice"));
		assertFalse(message.contains("Secret"));
		assertFalse(message.contains("Patient"));
		assertFalse(message.contains("1990"));
		assertFalse(message.contains("MRN-12345"));
	}
}
