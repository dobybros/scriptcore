package load;

import base.ChatTestCase;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.mongodb.MongoClientHelper;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class MongoTest extends ChatTestCase {

	@Test
	public void testWrite() throws Exception {
		MongoClientHelper mongoHelper = new MongoClientHelper();
		mongoHelper.connect("mongodb://localhost:8900,localhost:8901,localhost:8902");

		MongoDatabase database = mongoHelper.getMongoDatabase("test");
		MongoCollection<Document> writeCollection = database.getCollection("write");
		MongoCollection<Document> readCollection = database.getCollection("read");

		writeCollection.drop();
		writeCollection = database.getCollection("write");

		long time = System.currentTimeMillis();
		int count = 10000000;
		for(int i = 0; i < count; i++) {
			Document doc = new Document();
			doc.put("hello", "abcd" + i);
			doc.put("a", i);
			writeCollection.insertOne(doc);
		}
		long takes = (System.currentTimeMillis() - time);
		int length = 50;
		System.out.println("takes " + takes);
		System.out.println("takes seconds " + ((float)takes / 1000));
		System.out.println("byte per second " + count * length / ((float)takes / 1000) / 1024 / 1024 + "m");
	}

}
