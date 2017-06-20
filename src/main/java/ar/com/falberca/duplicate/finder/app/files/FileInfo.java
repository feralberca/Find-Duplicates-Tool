package ar.com.falberca.duplicate.finder.app.files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Convenient base class for holding information about files and their attributes 
 * 
 * @author fernando
 */
public class FileInfo {

	private String name;
	private long size;
	private String path;
	private String realPath;
	private boolean link;
	
	public FileInfo(Path pathRef, BasicFileAttributes attributesRef) {
		name = pathRef.getFileName().toString();
		size = attributesRef.size();
		path = pathRef.toFile().getAbsolutePath();
		link = attributesRef.isSymbolicLink();
		try {
			realPath = pathRef.toRealPath().toFile().getAbsolutePath();
		}
		catch(IOException ex) {
			realPath = null;
		}
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public boolean isLink() {
		return link;
	}
	
	public String getPath() {
		return path;
	}	

	public String getRealPath() {
		return realPath;
	}

	public boolean isEmpty() {
		return getSize() == 0;
	}
}
