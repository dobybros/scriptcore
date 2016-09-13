package script.memodb.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;

import chat.logs.LoggerEx;

/**
 * @author aplombchen
 *
 */
public class MDataFileGroup<T extends MDataFile<?>> {
	private static final String TAG = MDataFileGroup.class.getSimpleName();
	private List<T> files;
	
	public static final int AVAILABLE_FILESIZE = 10 * 1024 * 1024;

	private String fileName;
	private String basePath;
	
	public MDataFileGroup(String basePath, String fileName, Class<T> tClass) {
		this.fileName = fileName;
		this.basePath = basePath;
		this.fileClass = tClass;
		files = new ArrayList<T>();
		Collection<File> keyFiles = FileUtils.listFiles(new File(basePath),
				FileFilterUtils.prefixFileFilter(fileName),
				FileFilterUtils.directoryFileFilter());
		for(File file : keyFiles) {
			String theFileName = file.getName();
			theFileName = theFileName.replace(fileName, "");
			if(StringUtils.isBlank(theFileName)) {
				T dataFile = createFile(file);
				if(dataFile != null) {
					addDataFile(0, dataFile);
				}
			} else {
				theFileName = theFileName.substring(1);
				int number = -1;
				try {
					number = Integer.parseInt(theFileName);
				} catch (Exception e) {
				}
				if(number > 0) {
					T dataFile = createFile(file);
					if(dataFile != null) {
						addDataFile(number, dataFile);
					}
				}
			}
		}
	}
	
	private void addDataFile(int index, T t) {
		synchronized (files) {
			if(files.size() <= index) {
				for(int i = files.size(); i < index; i++) {
					files.add(null);
				}
				files.add(t);
			} else {
				files.add(index, t);
			}
		}
	}
	
	public T findCurrent() {
		return findCurrent(0);
	}
	public T findCurrent(int expectSize) {
		int size;
		if(AVAILABLE_FILESIZE < expectSize) 
			size = expectSize;
		else
			size = AVAILABLE_FILESIZE;
		
		for(int i = 0; i < files.size(); i++) {
			T t = files.get(i);
			if(t == null) {
				T dataFile = createFile(getFile(i));
				if(dataFile != null) {
					addDataFile(i, dataFile);
					t = dataFile;
				}
			}
			if(t != null) {
				int availableSize = t.length - t.offset;
				if(availableSize > size) {
					return t;
				}
			}
		}
		
		int index = files.size();
		T dataFile = createFile(getFile(index));
		if(dataFile != null) {
			addDataFile(index, dataFile);
		}
		int availableSize = dataFile.length - dataFile.offset;
		if(availableSize > size) {
			return dataFile;
		}
		return null;
	}

	private File getFile(int index) {
		if(index <= 0) {
			return new File(this.basePath + "/" + this.fileName);	
		}
		return new File(this.basePath + "/" + this.fileName + "." + index);
	}
	
	private Class<? extends MDataFile<?>> fileClass;
	private T createFile(File file) {
		if(fileClass != null) { 
			try {
				Constructor<?> constructor = fileClass.getConstructor(String.class);
				T t = (T) constructor.newInstance(file.getAbsolutePath());
				t.open();
				return t;
			} catch (Throwable t) {
				t.printStackTrace();
				LoggerEx.error(TAG, "Create MDataFile " + file + " failed, " + t.getMessage());
			}
		} else {
			LoggerEx.error(TAG, "Create MDataFile " + file + " failed, because fileClass not found, " + this.getClass());
		}
		return null;
	}
	
	public static void main(String[] args) {
		MDataFileGroup<KeysMDataFile> group = new MDataFileGroup<KeysMDataFile>("/Users/aplombchen/Desktop/data", "MemoTableFactory.index", KeysMDataFile.class);
//		System.out.println('5' + 2);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
