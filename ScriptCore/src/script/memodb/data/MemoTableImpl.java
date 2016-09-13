package script.memodb.data;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import script.memodb.MemoData;
import script.memodb.MemoTable;

public class MemoTableImpl extends MemoTable {
	private static final String EXTENSION_INDEXFILE = ".index";
	private static final String EXTENSION_KEYSFILE = ".keys"; 
	private static final String EXTENSION_CHUNKFILE = ".chunk"; 
	private MDataFileGroup<IndexMDataFile> indexFileGroup;
	private MDataFileGroup<KeysMDataFile> keysFileGroup;
	private MDataFileGroup<ChunkMDataFile> chunkFileGroup;
	
	public static final int STATUS_STANDBY = 0;
	public static final int STATUS_OPENNING = 1;
	public static final int STATUS_OPENNED = 10;
	public static final int STATUS_CLOSED = 20;
	private AtomicInteger status = new AtomicInteger(STATUS_STANDBY);
	
	private static final String TAG = MemoTableImpl.class.getSimpleName();
	
	@Override
	public void addMemoData(MemoData data) {

	}

	@Override
	public MemoCursor find(MemoIndexer byIndexer) {
		return null;
	}

	@Override
	public void open() {
		if(status.compareAndSet(STATUS_STANDBY, STATUS_OPENNING)) {
//			keysFile = new KeysMDataFile(path);
			String tablePath = basePath + "/" + name;
			
		} else {
			throw new IllegalStateException("MemoTable " + getName() + " open failed because of illegal status " + status.get() + " expected " + STATUS_STANDBY);
		}
	}

	@Override
	public void close() {
	}

}
