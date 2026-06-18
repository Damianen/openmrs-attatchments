package org.openmrs.module.attachments.rest;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class AttachmentBytesResourceTest {
	
	@Before
	public void setup() {
		initMocks(this);
		PowerMockito.mockStatic(Context.class);
	}
	
	@Test(expected = APIAuthenticationException.class)
	public void getFile_shouldRejectUnauthenticatedUsers() throws Exception {
		PowerMockito.when(Context.isAuthenticated()).thenReturn(false);
		
		new AttachmentBytesResource().getFile("attachment-uuid", null, mock(HttpServletResponse.class));
	}
	
	@Test(expected = APIAuthenticationException.class)
	public void getFile_shouldRejectUsersWithoutViewAttachmentsPrivilege() throws Exception {
		PowerMockito.when(Context.isAuthenticated()).thenReturn(true);
		PowerMockito.when(Context.hasPrivilege(AttachmentsConstants.VIEW_ATTACHMENTS)).thenReturn(false);
		
		new AttachmentBytesResource().getFile("attachment-uuid", null, mock(HttpServletResponse.class));
	}

	@Test
	public void buildAttachmentBytesLogMessage_shouldOnlyIncludeTechnicalContext() {
		String message = AttachmentBytesResource.buildAttachmentBytesLogMessage("SUCCESS", "attachment-uuid-123",
		    "thumbnail", "image/png", 42, null);

		assertTrue(message.contains("event=ATTACHMENT_BYTES_DOWNLOAD"));
		assertTrue(message.contains("result=SUCCESS"));
		assertTrue(message.contains("attachmentUuid=attachment-uuid-123"));
		assertTrue(message.contains("view=thumbnail"));
		assertTrue(message.contains("mimeType=image/png"));
		assertFalse(message.contains("patientName"));
		assertFalse(message.contains("birthdate"));
		assertFalse(message.contains("identifier"));
		assertFalse(message.contains("filename"));
	}

	@Test
	public void buildAttachmentBytesLogMessage_shouldSanitizeMultilineValues() {
		String message = AttachmentBytesResource.buildAttachmentBytesLogMessage("SUCCESS\nFAKE", "attachment-uuid-123",
		    "thumbnail\tview", "image/png\r\nforged=true", 42, "bad\tvalue");

		assertFalse(message.contains("\n"));
		assertFalse(message.contains("\r"));
		assertFalse(message.contains("\t"));
		assertTrue(message.contains("forged=true"));
	}
}
