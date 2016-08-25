package script.memodb.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author aplombchen
 *
 */
public class MDataFileGroup<T extends MDataFile<?>> {
	private List<T> files;

	public MDataFileGroup(String basePath, String fileName) {
		files = new ArrayList<T>();
		Collection<File> keyFiles = FileUtils.listFiles(new File(basePath),
				FileFilterUtils.prefixFileFilter(fileName),
				FileFilterUtils.directoryFileFilter());
		for(File file : keyFiles) {
			String theFileName = file.getName();
			theFileName = theFileName.replace(fileName, "");
			if(StringUtils.isBlank(theFileName)) {
				
				files.add(0, element);
			}
			System.out.println(file);
		}
	}
	
	private MDataFile<?> createFile(File file) {
		return null;
	}
	
	public static void main(String[] args) {
		MDataFileGroup<KeysMDataFile> group = new MDataFileGroup<KeysMDataFile>("/Users/aplombchen/Desktop/data", "MemoTableFactory.index");
//		System.out.println('5' + 2);
	}
}
