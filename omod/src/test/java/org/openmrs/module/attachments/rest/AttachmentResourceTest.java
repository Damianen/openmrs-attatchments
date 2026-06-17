package org.openmrs.module.attachments.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.commons.codec.binary.Base64;
import java.text.SimpleDateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.AttachmentsService;
import org.openmrs.module.attachments.obs.Attachment;
import org.openmrs.module.attachments.obs.ComplexDataHelperImpl;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class AttachmentResourceTest {

	@Before
	public void setup() {
		initMocks(this);
		PowerMockito.mockStatic(Context.class);
		AttachmentsContext ctx = mock(AttachmentsContext.class);
		when(ctx.getComplexDataHelper()).thenReturn(new ComplexDataHelperImpl());
		when(Context.getRegisteredComponent(AttachmentsConstants.COMPONENT_ATT_CONTEXT, AttachmentsContext.class))
		        .thenReturn(ctx);
	}

	@Test
	public void get_shouldReturnFilenameProperty() {
		// Arrange
		AttachmentResource res = new AttachmentResource();
		ObsService service = mock(ObsService.class);
		PowerMockito.when(Context.getObsService()).thenReturn(service);
		Obs attachmentObs = new Obs();
		attachmentObs.setUuid("1234");
		attachmentObs.setId(1);
		Concept attachmentConcept = new ConceptComplex();
		ConceptDatatype datatype = new ConceptDatatype();
		datatype.setHl7Abbreviation("ED");
		attachmentConcept.setDatatype(datatype);
		attachmentObs.setConcept(attachmentConcept);
		attachmentObs.setValueComplex("m3ks | instructions.default | text/plain | filename.png");
		when(service.getObsByUuid("1234")).thenReturn(attachmentObs);
		when(service.getComplexObs(1, AttachmentsConstants.ATT_VIEW_CRUD)).thenReturn(attachmentObs);

		// Act
		Attachment attachment = res.getByUniqueId("1234");

		// Assert
		assertThat(attachment.getFilename(), equalTo("filename.png"));
	}

	@Test
	public void search_shouldInvokeApiForEncounterAttachments() {
		// Setup
		AttachmentResource res = new AttachmentResource();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Encounter encounter = new Encounter();

		// Replay
		res.search(attachmentsService, patient, null, encounter, null, true);

		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, encounter, true);
		verifyNoMoreInteractions(attachmentsService);
	}

	@Test
	public void search_shouldInvokeApiForVisitAttachments() {
		// Setup
		AttachmentResource res = new AttachmentResource();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();
		Visit visit = new Visit();

		// Replay
		res.search(attachmentsService, patient, visit, null, null, true);

		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, visit, true);
		verifyNoMoreInteractions(attachmentsService);
	}

	@Test
	public void search_shouldInvokeApiForAllAttachments() {
		// Setup
		AttachmentResource res = new AttachmentResource();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();

		// Replay
		res.search(attachmentsService, patient, null, null, null, true);

		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, true);
		verifyNoMoreInteractions(attachmentsService);
	}

	@Test
	public void search_shouldInvokeApiForEncounterlessAttachments() {
		// Setup
		AttachmentResource res = new AttachmentResource();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();

		// Replay
		res.search(attachmentsService, patient, null, null, "only", true);

		// Verify
		verify(attachmentsService, times(1)).getEncounterlessAttachments(patient, true);
		verifyNoMoreInteractions(attachmentsService);
	}

	@Test
	public void search_shouldInvokeApiForAllAttachmentsButEncounterless() {
		// Setup
		AttachmentResource res = new AttachmentResource();
		AttachmentsService attachmentsService = mock(AttachmentsService.class);
		Patient patient = new Patient();

		// Replay
		res.search(attachmentsService, patient, null, null, "false", true);

		// Verify
		verify(attachmentsService, times(1)).getAttachments(patient, false, true);
		verifyNoMoreInteractions(attachmentsService);
	}

	@Test
	public void base64MultipartFile_shouldAcceptValidDataUri() throws Exception {
		byte[] expectedBytes = "hello".getBytes("UTF-8");
		String base64Content = "data:text/plain;base64," + Base64.encodeBase64String(expectedBytes);

		AttachmentResource.Base64MultipartFile file = new AttachmentResource.Base64MultipartFile(base64Content, "file",
		        "file.txt");

		assertThat(file.getContentType(), equalTo("text/plain"));
		assertThat(file.getOriginalFilename(), equalTo("file.txt"));
		assertThat(file.getBytes(), equalTo(expectedBytes));
	}

	@Test(expected = IllegalRequestException.class)
	public void base64MultipartFile_shouldRejectMissingComma() throws Exception {
		new AttachmentResource.Base64MultipartFile("data:text/plain;base64" + Base64.encodeBase64String("hello".getBytes()),
		        "file", "file.txt");
	}

	@Test(expected = IllegalRequestException.class)
	public void base64MultipartFile_shouldRejectMissingContentType() throws Exception {
		new AttachmentResource.Base64MultipartFile("data:;base64," + Base64.encodeBase64String("hello".getBytes()), "file",
		        "file.txt");
	}

	@Test(expected = IllegalRequestException.class)
	public void base64MultipartFile_shouldRejectNonBase64Payload() throws Exception {
		new AttachmentResource.Base64MultipartFile("data:text/plain;base64,not really base64!", "file", "file.txt");
	}

	@Test(expected = IllegalRequestException.class)
	public void base64MultipartFile_shouldRejectEmptyPayload() throws Exception {
		new AttachmentResource.Base64MultipartFile("data:text/plain;base64,", "file", "file.txt");
	}

	@Test
	public void validateFileExtension_shouldAllowSafeDefaultExtensionWhenAllowlistIsEmpty() {
		AttachmentResource.AllowedExtensionPolicy policy = AttachmentResource.getAllowedExtensionPolicy(new String[0]);

		AttachmentResource.validateFileExtension("png", policy);
	}

	@Test(expected = IllegalRequestException.class)
	public void validateFileExtension_shouldRejectUnsafeExtensionWhenAllowlistIsEmpty() {
		AttachmentResource.AllowedExtensionPolicy policy = AttachmentResource.getAllowedExtensionPolicy(new String[0]);

		AttachmentResource.validateFileExtension("exe", policy);
	}

	@Test(expected = IllegalRequestException.class)
	public void getFileExtension_shouldRejectFileWithoutExtension() {
		AttachmentResource.getFileExtension("attachment");
	}

	@Test(expected = IllegalRequestException.class)
	public void validateFileContentType_shouldRejectMimeMismatch() throws Exception {
		AttachmentResource.AllowedExtensionPolicy policy = AttachmentResource
		        .getAllowedExtensionPolicy(new String[] { "png" });
		MockMultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes("UTF-8"));

		AttachmentResource.validateFileContentType(file, "png", policy);
	}

	@Test
	public void validateFileContentType_shouldAllowConfiguredGenericBinaryExtension() throws Exception {
		AttachmentResource.AllowedExtensionPolicy policy = AttachmentResource
		        .getAllowedExtensionPolicy(new String[] { "dat" });
		MockMultipartFile file = new MockMultipartFile("file", "legacy.dat", "application/octet-stream",
		        new byte[] { 0, 1, 2, 3 });

		AttachmentResource.validateFileContentType(file, "dat", policy);
	}

	@Test
	public void buildAttachmentUploadLogMessage_shouldNotIncludePatientPii() throws Exception {
		Patient patient = new Patient();
		patient.setPatientId(123);
		patient.setUuid("patient-uuid-123");
		patient.addName(new PersonName("Alice", "Secret", "Patient"));
		patient.setBirthdate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-02"));
		patient.addIdentifier(new PatientIdentifier("MRN-12345", null, null));
		Visit visit = new Visit();
		visit.setUuid("visit-uuid-123");
		Encounter encounter = new Encounter();
		encounter.setUuid("encounter-uuid-123");

		String message = AttachmentResource.buildAttachmentUploadLogMessage("SUCCESS", patient, visit, encounter,
		    "attachment-uuid-123", "dat", "application/octet-stream", 20, null);

		assertThat(message.contains("event=ATTACHMENT_UPLOAD"), equalTo(true));
		assertThat(message.contains("patientUuid=patient-uuid-123"), equalTo(true));
		assertThat(message.contains("visitUuid=visit-uuid-123"), equalTo(true));
		assertThat(message.contains("encounterUuid=encounter-uuid-123"), equalTo(true));
		assertThat(message.contains("attachmentUuid=attachment-uuid-123"), equalTo(true));
		assertThat(message.contains("Alice"), equalTo(false));
		assertThat(message.contains("Secret"), equalTo(false));
		assertThat(message.contains("Patient"), equalTo(false));
		assertThat(message.contains("1990"), equalTo(false));
		assertThat(message.contains("id=123"), equalTo(false));
		assertThat(message.contains("MRN-12345"), equalTo(false));
	}

	@Test
	public void buildAttachmentLifecycleLogMessage_shouldNotIncludeDeleteReasonText() {
		String message = AttachmentResource.buildAttachmentLifecycleLogMessage("ATTACHMENT_DELETE", "SUCCESS",
		    "attachment-uuid-123", "encounter-uuid-123", null);

		assertThat(message.contains("event=ATTACHMENT_DELETE"), equalTo(true));
		assertThat(message.contains("attachmentUuid=attachment-uuid-123"), equalTo(true));
		assertThat(message.contains("encounterUuid=encounter-uuid-123"), equalTo(true));
		assertThat(message.contains("fileCaption"), equalTo(false));
		assertThat(message.contains("patientName"), equalTo(false));
	}

	@Test
	public void buildAttachmentUploadLogMessage_shouldSanitizeMultilineValues() {
		String message = AttachmentResource.buildAttachmentUploadLogMessage("SUCCESS\nFAKE", null, null, null,
		    "attachment-uuid-123", "dat", "application/octet-stream\r\nforged=true", 20, "bad\tvalue");

		assertThat(message.contains("\n"), equalTo(false));
		assertThat(message.contains("\r"), equalTo(false));
		assertThat(message.contains("\t"), equalTo(false));
		assertThat(message.contains("forged=true"), equalTo(true));
	}
}
