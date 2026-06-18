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
 * Abstraction over a source of runtime configuration values, looked up by name.
 * <p>
 * It exists so that {@link StorageConfig} depends on an abstraction rather than directly on
 * {@link System#getenv(String)} (dependency inversion). The production implementation reads the
 * process environment; tests inject a stub so credential resolution can be verified without
 * mutating the real environment.
 */
public interface EnvironmentSource {
	
	/**
	 * @param name the name of the configuration value (e.g. an environment variable name)
	 * @return the resolved value, or {@code null} when it is not set
	 */
	String get(String name);
}
