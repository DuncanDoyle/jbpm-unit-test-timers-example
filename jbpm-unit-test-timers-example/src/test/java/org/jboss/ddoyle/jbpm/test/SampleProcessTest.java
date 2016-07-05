package org.jboss.ddoyle.jbpm.test;

import java.util.concurrent.TimeUnit;

import org.drools.core.time.impl.PseudoClockScheduler;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;

/**
 * <code>jBPM</code> <code>JUnit</code> testcase that uses the <code>Drools</code> {@link PseudoClockScheduler} to programatically advance
 * time, which allows for unit-testing of timers in processes.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class SampleProcessTest extends JbpmJUnitBaseTestCase {

	private RuntimeManager runtimeManager;

	private RuntimeEngine runtimeEngine;

	private KieSession kieSession;

	public SampleProcessTest() {
		// Setup the datasource and enable persistence.
		super(true, true);
	}

	@Before
	public void init() {
		//Enable the PseudoClock using the following system property.
		System.setProperty("drools.clockType", "pseudo");
		runtimeManager = createRuntimeManager("sample-process.bpmn");
		runtimeEngine = getRuntimeEngine(null);
		kieSession = runtimeEngine.getKieSession();
	}

	@After
	public void destroy() {
		runtimeManager.disposeRuntimeEngine(runtimeEngine);
		runtimeManager.close();
	}

	@Test
	public void testTimerActivated() {
		ProcessInstance pInstance = kieSession.startProcess("sample-process");
		long pInstanceId = pInstance.getId();

		PseudoClockScheduler sessionClock = kieSession.getSessionClock();

		// Timer is set to 60 seconds, so advancing with 70.
		sessionClock.advanceTime(70, TimeUnit.SECONDS);

		// Test that the timer has triggered.
		assertNodeTriggered(pInstanceId, "Goodbye Process");
		assertProcessInstanceCompleted(pInstanceId);
		

	}
	
	@Test
	public void testTimerNotActivated() {
		ProcessInstance pInstance = kieSession.startProcess("sample-process");
		long pInstanceId = pInstance.getId();

		PseudoClockScheduler sessionClock = kieSession.getSessionClock();

		// Only advancing time by 10 seconds, so process should still be waiting.
		sessionClock.advanceTime(10, TimeUnit.SECONDS);

		// Test that the process is still active.
		assertProcessInstanceActive(pInstanceId);
	}

}
