package ar.com.falberca.duplicate.finder.app.files;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileInfoTest {
	
	private File regularFile;
	private File symlinkFile;
	private File emptyFile;
	private File brokenLink;

	@Before
	public void setUp() throws Exception {
		emptyFile = File.createTempFile("empty-file-info-test", ".tmp");
		regularFile = File.createTempFile("regular-file-info-test", ".tmp");
		symlinkFile = Files.createSymbolicLink(
				Paths.get(System.getProperty("java.io.tmpdir"), "symlink-file-test"), regularFile.toPath()).toFile();
		
		try (FileWriter fileWriter = new FileWriter(regularFile)) {
			fileWriter.write("Testing regular files");
		}
		
		Path tempFile = Files.createTempFile("broken-file-info-test", ".tmp");
		brokenLink = Files.createSymbolicLink(
				Paths.get(System.getProperty("java.io.tmpdir"), "broken-symlink-file-test"), tempFile).toFile();
		tempFile.toFile().delete();
	}

	@After
	public void tearDown() throws Exception {
		emptyFile.delete();
		symlinkFile.delete();
		regularFile.delete();
		brokenLink.delete();
	}

	@Test
	public void testRegularFile() throws Exception {
		BasicFileAttributes attr = Files.readAttributes(regularFile.toPath(), BasicFileAttributes.class);
		FileInfo fileInfo = new FileInfo(regularFile.toPath(), attr);
		
		assertTrue(fileInfo.getSize() > 0);
		assertFalse(fileInfo.isLink());
		assertFalse(fileInfo.isEmpty());
		assertEquals(regularFile.getAbsolutePath(), fileInfo.getPath());
		assertEquals(fileInfo.getPath(), fileInfo.getRealPath());
		assertEquals(regularFile.getName(), fileInfo.getName());
	}
	
	@Test
	public void testSymlinkFile() throws Exception {
		BasicFileAttributes attr = Files.readAttributes(symlinkFile.toPath(), 
														BasicFileAttributes.class, 
														LinkOption.NOFOLLOW_LINKS);
		
		String realPath = symlinkFile.toPath().toRealPath().toString();
		
		FileInfo fileInfo = new FileInfo(symlinkFile.toPath(), attr);
		
		assertTrue(fileInfo.getSize() > 0);
		assertTrue(fileInfo.isLink());
		assertFalse(fileInfo.isEmpty());
		assertEquals(realPath, fileInfo.getRealPath());
		assertEquals(symlinkFile.getName(), fileInfo.getName());
	}
	
	@Test
	public void testEmptyFile() throws Exception {
		BasicFileAttributes attr = Files.readAttributes(emptyFile.toPath(), BasicFileAttributes.class);
		
		FileInfo fileInfo = new FileInfo(emptyFile.toPath(), attr);
		
		assertEquals(0, fileInfo.getSize());
		assertFalse(fileInfo.isLink());
		assertTrue(fileInfo.isEmpty());
		assertEquals(emptyFile.getName(), fileInfo.getName());
	}

	@Test
	public void testBrokenLinkFile() throws Exception {
		BasicFileAttributes attr = Files.readAttributes(brokenLink.toPath(), 
														BasicFileAttributes.class, 
														LinkOption.NOFOLLOW_LINKS);
		
		FileInfo fileInfo = new FileInfo(brokenLink.toPath(), attr);
		
		assertNotEquals(0, fileInfo.getSize());
		assertTrue(fileInfo.isLink());
		assertTrue(fileInfo.isEmpty());
		assertEquals(brokenLink.getName(), fileInfo.getName());
		assertNull(fileInfo.getRealPath());
	}
}
