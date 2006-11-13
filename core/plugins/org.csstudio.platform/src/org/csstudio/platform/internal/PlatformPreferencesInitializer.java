/* 
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.platform.internal;

import org.csstudio.platform.CSSPlatformPlugin;
import org.csstudio.platform.LocaleService;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.platform.security.AuthenticationService;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Preference initializer implemenation. This class initializes all core
 * preferences. New preference settings should be initialized in this class,
 * too.
 * 
 * @author awill, swende
 */
public final class PlatformPreferencesInitializer extends
		AbstractPreferenceInitializer {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = new DefaultScope()
				.getNode(CSSPlatformPlugin.ID);

		initializeLoggingPreferences(node);
		initializeAuthenticationPreferences(node);
		initializeLocalePreferences(node);
	}

	/**
	 * Initializes all preference settings for the Log4J console appender.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeConsoleAppenderPreferences(
			final IEclipsePreferences node) {
		node.put(CentralLogger.PROP_LOG4J_CONSOLE_APPENDER,
				"org.apache.log4j.ConsoleAppender"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_CONSOLE_LAYOUT,
				"org.apache.log4j.PatternLayout"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_CONSOLE_PATTERN,
				"%d{ISO8601} %-5p [%t] %c: %m%n"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_CONSOLE_THRESHOLD, "INFO"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_CONSOLE_FOLLOW, "true"); //$NON-NLS-1$
	}

	/**
	 * Initializes all preference settings for the Log4J file appender.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeFileAppenderPreferences(
			final IEclipsePreferences node) {
		node.put(CentralLogger.PROP_LOG4J_FILE_APPENDER,
				"org.apache.log4j.RollingFileAppender"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_LAYOUT,
				"org.apache.log4j.PatternLayout"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_PATTERN,
				"%d{ISO8601} %-5p [%t] %c: %m%n"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_THRESHOLD, "INFO"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_DESTINATION, "logs/event.log"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_APPEND, "true"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_MAX_SIZE, "500KB"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE_MAX_INDEX, "1"); //$NON-NLS-1$
	}

	/**
	 * Initializes all preference settings for the Log4J JMS appender.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeJmsAppenderPreferences(final IEclipsePreferences node) {
		node.put(CentralLogger.PROP_LOG4J_JMS_APPENDER,
				"org.apache.log4j.net.JMSAppender"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_THRESHOLD, "INFO"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_LAYOUT,
				"org.apache.log4j.PatternLayout"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_PATTERN,
				"%d{ISO8601} %-5p [%t] %c: %m%n"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_URL, "tcp://localhost:3035"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_ICFN,
				"org.exolab.jms.jndi.InitialContextFactory"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_TCFBN, "ConnectionFactory"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_TOPIC, "topic1"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_USER, ""); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS_PASSWORD, ""); //$NON-NLS-1$

		// important note: ConnectionFactory and topic1 are openjms defaults!
	}

	/**
	 * Initializes all preference settings for the logging mechanism.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeLoggingPreferences(final IEclipsePreferences node) {
		node.put(CentralLogger.PROP_LOG4J_CONSOLE, "true"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_FILE, "false"); //$NON-NLS-1$
		node.put(CentralLogger.PROP_LOG4J_JMS, "false"); //$NON-NLS-1$

		initializeConsoleAppenderPreferences(node);
		initializeFileAppenderPreferences(node);
		initializeJmsAppenderPreferences(node);
	}

	/**
	 * Initializes all preference settings for the authentication mechanism.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeAuthenticationPreferences(
			final IEclipsePreferences node) {
		node.put(AuthenticationService.PROP_AUTH_LOGIN, "false"); //$NON-NLS-1$
	}

	/**
	 * Initializes the localization settings.
	 * 
	 * @param node
	 *            the preferences node to use
	 */
	private void initializeLocalePreferences(final IEclipsePreferences node) {
		node.put(LocaleService.PROP_LOCALE, ""); //$NON-NLS-1$
	}

}
