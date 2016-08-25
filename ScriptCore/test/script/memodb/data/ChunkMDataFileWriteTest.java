package script.memodb.data;

import java.io.IOException;

public class ChunkMDataFileWriteTest {
	ChunkMDataFileWriteTest(String path) {
	}
	
	public static void main(String[] args) throws IOException {
//		ChunkMDataFile file = new ChunkMDataFile("C:\\Dev\\tmp\\1.chunk");
		
		for(int i = 0; i < 1; i++) {
			final int count = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ChunkMDataFile file = new ChunkMDataFile("/Users/aplombchen/Desktop/data/" + count + ".chunk");
//					ChunkMDataFile file = new ChunkMDataFile("/tmp/test/1.chunk");
						file.open();
						long time = System.currentTimeMillis();
						int count = 25000000;
						for(int i = 0; i < count;i++) {
							Chunk chunk = new Chunk();
							chunk.setChunkNum(0);
							chunk.setDataBytes(("hello world " + i).getBytes("utf8"));;
//						chunk.chunkLength = chunk.dataBytes.length;
							chunk.setNextChunkNum(-1);
							chunk.setNextChunkOffset(-1);
							
							file.add(chunk);
						}
						long takes = (System.currentTimeMillis() - time);
//						System.out.println("takes " + takes);
//						System.out.println("takes seconds " + ((float)takes / 1000));
						System.out.println(this + " takes seconds " + ((float)takes / 1000) + "; byte per second " + count * 38 / ((float)takes / 1000) / 1024 / 1024 + "m");					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
	}
}
