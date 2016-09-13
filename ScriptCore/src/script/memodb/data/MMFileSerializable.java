package script.memodb.data;

import java.io.IOException;

interface MMFileSerializable {
	void resurrect(MemoryMappedFile memoFile, int offset) throws IOException;
	void persistent(MemoryMappedFile memoFile, int offset) throws IOException;
}
