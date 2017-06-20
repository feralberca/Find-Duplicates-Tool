package ar.com.falberca.duplicate.finder.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FinderApp.class )
public class FinderAppTest {

	@Test
	public void testOk() throws Exception {
		FinderApp app = PowerMock.createPartialMock(FinderApp.class, "processFileSystem");
		app.processFileSystem("/home", 3, true, true);
		EasyMock.expectLastCall().atLeastOnce();
		PowerMock.replayAll();
		app.execute(new String[] {"-rootDir", "/home", "-parallel", "3", "-skipLinks", "-skipEmpty"});
		PowerMock.verifyAll(); 
	}
	
	@Test
	public void testOptionalParams() throws Exception {
		FinderApp app = PowerMock.createPartialMock(FinderApp.class, "processFileSystem");
		app.processFileSystem("/home", 3, false, false);
		EasyMock.expectLastCall().atLeastOnce();
		PowerMock.replayAll();
		app.execute(new String[] {"-rootDir", "/home", "-parallel", "3"});
		PowerMock.verifyAll(); 
	}
	
	@Test
	public void testOkSkipLinks() throws Exception {
		FinderApp app = PowerMock.createPartialMock(FinderApp.class, "processFileSystem");
		app.processFileSystem("/home", 3, true, false);
		EasyMock.expectLastCall().atLeastOnce();
		PowerMock.replayAll();
		app.execute(new String[] {"-rootDir", "/home", "-parallel", "3", "-skipLinks"});
		PowerMock.verifyAll(); 
	}
	
	@Test
	public void testOkSkipEmpty() throws Exception {
		FinderApp app = PowerMock.createPartialMock(FinderApp.class, "processFileSystem");
		app.processFileSystem("/home", 3, false, true);
		EasyMock.expectLastCall().atLeastOnce();
		PowerMock.replayAll();
		app.execute(new String[] {"-rootDir", "/home", "-parallel", "3", "-skipEmpty"});
		PowerMock.verifyAll(); 
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgumentException() throws Exception {
		FinderApp app = PowerMock.createPartialMock(FinderApp.class, "processFileSystem");
		app.processFileSystem(EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyBoolean(), EasyMock.anyBoolean());
		EasyMock.expectLastCall().times(0);
		PowerMock.replayAll();
		app.execute(new String[] {"-rootDir2", "/home", "-parallel", "3", "-skipLinks", "-skipEmpty"});
		PowerMock.verifyAll(); 
	}

}
