/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.attachments;

/**
 * Single source of truth for the storage-backend credentials and endpoint used by the attachments
 * module.
 * <p>
 * Values are read from the runtime environment (see the {@code ATTACHMENTS_STORAGE_*} variables in
 * {@link AttachmentsConstants}) and never hardcoded. Secrets are intentionally NOT read from
 * OpenMRS global properties, because those are persisted in the database; the environment keeps
 * them out of both source control and the application database.
 */
public class StorageConfig {
	
	private final EnvironmentSource environment;
	
	/**
	 * Production constructor: resolves values from the process environment.
	 */
	public StorageConfig() {
		this(new EnvironmentSource() {
			
			@Override
			public String get(String name) {
				return System.getenv(name);
			}
		});
	}
	
	/**
	 * Testing seam: resolves values from the supplied source.
	 *
	 * @param environment the source to read configuration values from
	 */
	StorageConfig(EnvironmentSource environment) {
		this.environment = environment;
	}
	
	public String getStorageAccessKey() {
		return require(AttachmentsConstants.ENV_STORAGE_ACCESS_KEY);
	}
	
	public String getStorageSecretKey() {
		return require(AttachmentsConstants.ENV_STORAGE_SECRET_KEY);
	}
	
	public String getStorageBucket() {
		return require(AttachmentsConstants.ENV_STORAGE_BUCKET);
	}
	
	/**
	 * Resolves a required value, failing fast when it is missing so the module never silently falls
	 * back to a hardcoded credential.
	 */
	private String require(String name) {
		String value = environment.get(name);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Missing required storage configuration environment variable: " + name);
		}
		return value;
	}
}
