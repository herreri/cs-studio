/*
 * Copyright (c) 2009 Stiftung Deutsches Elektronen-Synchrotron,
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

package org.csstudio.diag.icsiocmonitor.ui.internal.model;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csstudio.diag.icsiocmonitor.service.IocConnectionReport;
import org.csstudio.diag.icsiocmonitor.service.IIocConnectionReporter;
import org.csstudio.diag.icsiocmonitor.service.IocConnectionState;
import org.csstudio.diag.icsiocmonitor.ui.internal.model.IocMonitor;
import org.csstudio.diag.icsiocmonitor.ui.internal.model.IocState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Joerg Rathlev
 *
 */
public class IocMonitorTest {
	
	private IocMonitor _im;
	private IIocConnectionReporter _r1;
	private IIocConnectionReporter _r2;

	@Before
	public void setUp() throws Exception {
		_im = new IocMonitor();

		_r1 = Mockito.mock(IIocConnectionReporter.class);
		Map<String, IocConnectionState> s1 = new HashMap<String, IocConnectionState>();
		s1.put("ioc1", IocConnectionState.CONNECTED);
		s1.put("ioc2", IocConnectionState.DISCONNECTED);
		Mockito.when(_r1.getReport()).thenReturn(new IocConnectionReport("r1", s1));

		_r2 = Mockito.mock(IIocConnectionReporter.class);
		Map<String, IocConnectionState> s2 = new HashMap<String, IocConnectionState>();
		s2.put("ioc1", IocConnectionState.CONNECTED);
		s2.put("ioc2", IocConnectionState.CONNECTED);
		s2.put("ioc3", IocConnectionState.DISCONNECTED);
		Mockito.when(_r2.getReport()).thenReturn(new IocConnectionReport("r2", s2));
	}
	
	/**
	 * Helper method which checks that the given list contains an IOC state for
	 * the given IOC and that the state for that IOC and the given server is the
	 * given state.
	 */
	private boolean containsState(List<IocState> states, String ioc,
			String server, IocConnectionState state) {
		for (IocState iocState : states) {
			if (iocState.getIocName().equals(ioc)) {
				return iocState.getIcsConnectionState(server) == state;
			}
		}
		return false;
	}
	
	@Test
	public void testNoReportersAdded() throws Exception {
		// No reporters configured, so lists should be empty
		assertTrue(_im.getInterconnectionServers().isEmpty());
		assertTrue(_im.getIocStates().isEmpty());
	}
	
	@Test
	public void testSingleReporter() throws Exception {
		_im.addReporterService(_r1);
		assertEquals(1, _im.getInterconnectionServers().size());
		assertTrue(_im.getInterconnectionServers().contains("r1"));
		List<IocState> is = _im.getIocStates();
		assertEquals(2, is.size());
		assertTrue(containsState(is, "ioc1", "r1", IocConnectionState.CONNECTED));
		assertTrue(containsState(is, "ioc2", "r1", IocConnectionState.DISCONNECTED));
	}
	
	@Test
	public void testMultipleReporters() throws Exception {
		/*
		 * This test checks that reports from multiple reporters are aggregated
		 * correctly. There are two reporters that report:
		 *   r1: ioc1 = C, ioc2 = D
		 *   r2: ioc1 = C, ioc2 = C, ioc3 = D
		 *  
		 * The expected result is that there should be three IocState objects
		 * with the correct states. ioc3/r1 should be reported as DISCONNECTED.
		 */
		_im.addReporterService(_r1);
		_im.addReporterService(_r2);
		assertEquals(2, _im.getInterconnectionServers().size());
		assertTrue(_im.getInterconnectionServers().contains("r1"));
		assertTrue(_im.getInterconnectionServers().contains("r2"));
		List<IocState> is = _im.getIocStates();
		assertEquals(3, is.size());
		assertTrue(containsState(is, "ioc1", "r1", IocConnectionState.CONNECTED));
		assertTrue(containsState(is, "ioc1", "r2", IocConnectionState.CONNECTED));
		assertTrue(containsState(is, "ioc2", "r1", IocConnectionState.DISCONNECTED));
		assertTrue(containsState(is, "ioc2", "r2", IocConnectionState.CONNECTED));
		assertTrue(containsState(is, "ioc3", "r1", IocConnectionState.DISCONNECTED));
		assertTrue(containsState(is, "ioc3", "r2", IocConnectionState.DISCONNECTED));
	}
	
	@Test
	public void testRemoveReporter() throws Exception {
		_im.addReporterService(_r1);
		_im.addReporterService(_r2);
		_im.removeReporterService(_r2);
		assertEquals(1, _im.getInterconnectionServers().size());
		assertTrue(_im.getInterconnectionServers().contains("r1"));
		List<IocState> is = _im.getIocStates();
		assertEquals(2, is.size());
		assertTrue(containsState(is, "ioc1", "r1", IocConnectionState.CONNECTED));
		assertTrue(containsState(is, "ioc2", "r1", IocConnectionState.DISCONNECTED));
	}

}
