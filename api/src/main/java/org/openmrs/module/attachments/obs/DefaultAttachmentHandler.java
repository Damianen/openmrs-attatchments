package org.openmrs.module.attachments.obs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openmrs.Obs;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.BinaryDataHandler;

public class DefaultAttachmentHandler extends AbstractAttachmentHandler {
	
	public DefaultAttachmentHandler() {
		super();
	}
	
	protected void setParentComplexObsHandler() {
		setParent(new BinaryDataHandler());
	}
	
	protected ComplexData readComplexData(Obs obs, ValueComplex valueComplex, String view) {
		// We invoke the parent to inherit from the file reading routines.
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(valueComplex.getFileName()); // Temp obs used as a safety
		
		ComplexData complexData;
		if (view.equals(AttachmentsConstants.ATT_VIEW_THUMBNAIL)) {
			// This handler doesn't have data for thumbnails, we return a null content
			complexData = new ComplexData(valueComplex.getFileName(), null);
		} else {
			tmpObs = getParent().getObs(tmpObs, AttachmentsConstants.BINARYDATA_HANDLER_VIEW); // BinaryDataHandler
			                                                                                   // doesn't handle
			                                                                                   // several views
			complexData = tmpObs.getComplexData();
		}
		
		// Then we build our own custom complex data
		return getComplexDataHelper().build(valueComplex.getInstructions(), complexData.getTitle(), complexData.getData(),
		    valueComplex.getMimeType()).asComplexData();
	}
	
	protected boolean deleteComplexData(Obs obs, AttachmentComplexData complexData) {
		// We use a temp obs whose value complex points to the file name
		Obs tmpObs = new Obs();
		tmpObs.setValueComplex(complexData.asComplexData().getTitle()); // Temp obs used as a safety
		return getParent().purgeComplexData(tmpObs);
	}
	
	protected ValueComplex saveComplexData(Obs obs, AttachmentComplexData complexData) {
		// We invoke the parent to inherit from the file saving routines.
		obs = getParent().saveObs(obs);
		
		File savedFile = AbstractHandler.getComplexDataFile(obs);
		String savedFileName = savedFile.getName();
		
		return new ValueComplex(complexData.getInstructions(), complexData.getMimeType(), savedFileName);
	}
	
	/**
	 * Retrieves the raw bytes of an attachment file given its storage directory and file name. Intended
	 * for administrative export and backup operations.
	 *
	 * @param attachmentDir base directory where attachments are stored
	 * @param fileName file name as supplied by the client request
	 * @return byte array of the file contents
	 * @throws IOException if the file cannot be read or resolves outside the attachment directory
	 */
	public byte[] getAttachmentByPath(String attachmentDir, String fileName) throws IOException {
		try {
			Path attachmentRoot = Paths.get(attachmentDir).toAbsolutePath().normalize();
			Path attachmentPath = attachmentRoot.resolve(fileName).normalize();
			
			if (!attachmentPath.startsWith(attachmentRoot) || !Files.isRegularFile(attachmentPath)) {
				throw new IOException("Invalid attachment path");
			}
			
			return Files.readAllBytes(attachmentPath);
		}
		catch (InvalidPathException ex) {
			throw new IOException("Invalid attachment path", ex);
		}
	}
}
