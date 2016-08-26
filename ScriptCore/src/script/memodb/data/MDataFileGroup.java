package script.memodb.data;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
	
	public static final int AVAILABLE_FILESIZE = 5 * 1024 * 1024;

	public MDataFileGroup(String basePath, String fileName) {
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
					files.add(0, dataFile);
				}
			} else {
				int number = -1;
				try {
					number = Integer.parseInt(theFileName);
				} catch (Exception e) {
				}
				if(number > 0) {
					T dataFile = createFile(file);
					if(dataFile != null) {
						files.add(number, dataFile);
					}
				}
			}
		}
	}
	
	private T createFile(File file) {
		Class<? extends MDataFile<?>> fileClass = null;
		Type[] types = this.getClass().getGenericInterfaces();
		for (Type type : types) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType().equals(MDataFile.class)) {
					Type[] params = pType.getActualTypeArguments();
					if (params != null && params.length == 1) {
						fileClass = (Class<? extends MDataFile<?>>) params[0];
					}
				}
			}
		}
		if(fileClass != null) { 
			try {
				Constructor<?> constructor = fileClass.getConstructor(String.class);
				return (T) constructor.newInstance(file.getAbsolutePath());
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
		MDataFileGroup<KeysMDataFile> group = new MDataFileGroup<KeysMDataFile>("/Users/aplombchen/Desktop/data", "MemoTableFactory.index");
//		System.out.println('5' + 2);
	}
}
