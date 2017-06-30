/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.ibm.javametrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests for com.ibm.javametrics.Javametrics
 *
 */
public class JavametricsTest {

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#getTopic(java.lang.String)}.
	 */
	@Test
	public void testGetTopic() {
		try {
			Javametrics.getTopic(null);
			fail("Exception expected for Javametrics.getTopic(null)");
		} catch (JavametricsException jme) {
			// expected, continue test
		}
		try {
			Javametrics.getTopic("");
			fail("Exception expected for Javametrics.getTopic(\"\")");
		} catch (JavametricsException jme2) {
			// expected, continue test
		}
		Topic topic = Javametrics.getTopic("myTopic");
		assertNotNull(topic);
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#isEnabled(java.lang.String)}.
	 */
	@Test
	public void testIsEnabled() {
		try {
			Javametrics.isEnabled(null);
			fail("Exception expected for Javametrics.isEnabled(null)");
		} catch (JavametricsException jme2) {
			// expected, continue test
		}
		try {
			Javametrics.isEnabled("");
			fail("Exception expected for Javametrics.isEnabled(\"\")");
		} catch (JavametricsException jme2) {
			// expected, continue test
		}
		Topic topic = Javametrics.getTopic("testTopic");
		assertTrue(Javametrics.isEnabled("testTopic"));
		topic.disable();
		assertFalse(Javametrics.isEnabled("testTopic"));
		topic.enable();
		assertTrue(Javametrics.isEnabled("testTopic"));
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#addListener(com.ibm.javametrics.JavametricsListener)}.
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#sendJSON(String, String)}.
	 */
	@Test
	public void testAddListenerAndSendJSON() {
		final List<String> received = new ArrayList<String>();
		Javametrics.addListener(new JavametricsListener() {

			@Override
			public void receive(String pluginName, String data) {
				if (pluginName.equals("api")) {
					List<String> events = TestUtils.splitIntoJSONObjects(data);
					for (Iterator<String> iterator = events.iterator(); iterator.hasNext();) {
						String oneEvent = iterator.next();
						// Only store data we sent from this test case
						if (oneEvent.startsWith("{\"topic\": \"myTopic\",")) {
							received.add(oneEvent);
						}
					}
				}
			}
		});
		Javametrics.sendJSON("myTopic", "{\"message\": \"hello\"}");
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeout < startTime && received.size() == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		assertTrue("Listener should have received a message", received.size() > 0);
		for (Iterator<String> iterator = received.iterator(); iterator.hasNext();) {
			String data = iterator.next();
			boolean foundMyData = false;
			if (data.startsWith("{\"topic\": \"myTopic\",")) {
				assertEquals(data, "{\"topic\": \"myTopic\", \"payload\":{\"message\": \"hello\"}}");
				foundMyData = true;
			}
			assertTrue("Should have received the data we sent", foundMyData);
		}
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#sendJSON(String, String)}.
	 */
	@Test
	public void testSendJSON() {
		try {
			Javametrics.sendJSON(null, "hello");
			fail("Javametrics.sendJSON(null, ..) should throw an exception");
		} catch (JavametricsException e) {
			// expected, continue test
		}
		try {
			Javametrics.sendJSON("", "hello");
			fail("Javametrics.sendJSON(\"\", ..) should throw an exception");
		} catch (JavametricsException je) {
			// expected, continue test
		}

		try {
			Javametrics.sendJSON("hello", null);
			fail("Javametrics.sendJSON(.., null) should throw an exception");
		} catch (JavametricsException je) {
			// expected, continue test
		}
		try {
			Javametrics.sendJSON("hello", "");
			fail("Javametrics.sendJSON(..,\"\") should throw an exception");
		} catch (JavametricsException je) {
			// expected
		}
	}

	/**
	 * Test method for
	 * {@link com.ibm.javametrics.Javametrics#removeListener(com.ibm.javametrics.JavametricsListener)}.
	 */
	@Test
	public void testRemoveListener() {
		final List<String> received = new ArrayList<String>();
		JavametricsListener jml = new JavametricsListener() {

			@Override
			public void receive(String pluginName, String data) {
				if (pluginName.equals("api")) {
					received.add(data);
				}
			}
		};
		Javametrics.addListener(jml);
		Javametrics.removeListener(jml);
		// clear any data that may have been sent by built in data providers
		received.clear();
		Javametrics.sendJSON("myTopic", "{\"message\": \"hello\"}");
		int timeout = 3000;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - timeout < startTime && received.size() == 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		assertTrue("Listener should not have received a message", received.size() == 0);
	}

}
