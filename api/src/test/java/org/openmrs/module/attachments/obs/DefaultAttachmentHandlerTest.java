package org.openmrs.module.attachments.obs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultAttachmentHandlerTest {
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Test
	public void getAttachmentByPath_shouldReadFileInsideAttachmentDirectory() throws Exception {
		File attachmentDir = temporaryFolder.newFolder("attachments");
		File attachment = new File(attachmentDir, "allowed.txt");
		byte[] expectedBytes = "allowed content".getBytes("UTF-8");
		Files.write(attachment.toPath(), expectedBytes);
		
		byte[] actualBytes = new DefaultAttachmentHandler().getAttachmentByPath(attachmentDir.getAbsolutePath(),
		    attachment.getName());
		
		Assert.assertArrayEquals(expectedBytes, actualBytes);
	}
	
	@Test(expected = IOException.class)
	public void getAttachmentByPath_shouldRejectTraversalOutsideAttachmentDirectory() throws Exception {
		File attachmentDir = temporaryFolder.newFolder("attachments");
		File outsideFile = temporaryFolder.newFile("secret.txt");
		Files.write(outsideFile.toPath(), "secret".getBytes("UTF-8"));
		
		new DefaultAttachmentHandler().getAttachmentByPath(attachmentDir.getAbsolutePath(),
		    ".." + File.separator + outsideFile.getName());
	}
	
	@Test(expected = IOException.class)
	public void getAttachmentByPath_shouldRejectAbsolutePathOutsideAttachmentDirectory() throws Exception {
		File attachmentDir = temporaryFolder.newFolder("attachments");
		File outsideFile = temporaryFolder.newFile("absolute-secret.txt");
		Files.write(outsideFile.toPath(), "secret".getBytes("UTF-8"));
		
		new DefaultAttachmentHandler().getAttachmentByPath(attachmentDir.getAbsolutePath(), outsideFile.getAbsolutePath());
	}
}
