package script.memodb.data;

import java.io.IOException;

interface MMFileSerializable {
	void resurrect(MemoryMappedFile memoFile, long address) throws IOException;
	void persistent(MemoryMappedFile memoFile, long address) throws IOException;
}
