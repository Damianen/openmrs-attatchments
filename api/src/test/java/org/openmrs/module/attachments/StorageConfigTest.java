package org.openmrs.module.attachments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class StorageConfigTest {
	
	/**
	 * Minimal {@link EnvironmentSource} stub backed by a map, so credential resolution can be verified
	 * without mutating the real process environment.
	 */
	private static EnvironmentSource fakeEnvironment(Map<String, String> values) {
		final Map<String, String> snapshot = new HashMap<String, String>(values);
		return new EnvironmentSource() {
			
			@Override
			public String get(String name) {
				return snapshot.get(name);
			}
		};
	}
	
	@Test
	public void getStorageCredentials_shouldReadValuesFromTheEnvironment() {
		Map<String, String> env = new HashMap<String, String>();
		env.put(AttachmentsConstants.ENV_STORAGE_ACCESS_KEY, "env-access-key");
		env.put(AttachmentsConstants.ENV_STORAGE_SECRET_KEY, "env-secret-key");
		env.put(AttachmentsConstants.ENV_STORAGE_BUCKET, "env-bucket");
		
		StorageConfig config = new StorageConfig(fakeEnvironment(env));
		
		assertEquals("env-access-key", config.getStorageAccessKey());
		assertEquals("env-secret-key", config.getStorageSecretKey());
		assertEquals("env-bucket", config.getStorageBucket());
		
		// The credential is sourced from the environment, not the secret that used to
		// be hardcoded.
		assertNotEquals("AKIA-ATTACHMENTS-SVC-7K3M", config.getStorageAccessKey());
	}
	
	@Test
	public void getStorageCredentials_shouldFailFastWhenAVariableIsMissing() {
		// Secret key deliberately left unset; the module must not fall back to a
		// hardcoded value.
		Map<String, String> env = new HashMap<String, String>();
		env.put(AttachmentsConstants.ENV_STORAGE_ACCESS_KEY, "env-access-key");
		env.put(AttachmentsConstants.ENV_STORAGE_BUCKET, "env-bucket");
		
		StorageConfig config = new StorageConfig(fakeEnvironment(env));
		
		try {
			config.getStorageSecretKey();
			fail("Expected IllegalStateException when the secret key is not configured");
		}
		catch (IllegalStateException expected) {
			assertEquals(true, expected.getMessage().contains(AttachmentsConstants.ENV_STORAGE_SECRET_KEY));
		}
	}
}
