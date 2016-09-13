package load;

import base.ChatTestCase;
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

public class ReloadMemoryTest extends ChatTestCase {

	@Test
	public void testNewArticle() throws Exception {
		login("aplomb", "123456");

		HttpClient httpClient = new HttpClient();
		for(int i = 0; i < 1000; i++) {
			PostMethod method = new PostMethod(HOST + "/articles");
			assert cookieString != null;
			method.setRequestHeader(new Header("Cookie", cookieString));
			Part[] parts = new Part[4];
			parts[0] = new StringPart("article", "{\"title\" : \"hello_" + i + "\", \"summary\" : \"This is summary\"}", "utf8");
//		parts[1] = new StringPart("html", "<html><body><img src=\"{unique125}\" />Hello world, this is very good!<img src=\"{unique123}\" /></body></html>", "utf8");
			parts[1] = new StringPart("content", "{\"text\" : \"hello world\", \"image\" : \"icon40@2x.png\", \"image\" : \"icon40@2x.png\"}", "utf8");
			parts[2] = new FilePart("1", "icon40@2x.png", new File("data/icon40@2x.png"));
			parts[3] = new FilePart("2", "icon40@2x.png", new File("data/icon40@2x.png"));
			method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
			int code = httpClient.executeMethod(method);

			if(code != 200)
				System.out.println(method.getURI() + " i " + i + " code " + code + " phrase " + method.getStatusText() + " i = " + i);
//			assert code == 200;
			InputStream is = method.getResponseBodyAsStream();
			String responseStr = IOUtils.toString(is, "utf8");
//			System.out.println("Response " + responseStr);
//			Document responseDoc = Document.parse(responseStr);
//			assert responseDoc.get("code").equals(1);
		}

	}

	@Test
	public void testHtmlResource() throws Exception {
		//Create new article
		testNewArticle();

		//List all articles and find one article id.
		String id = null;
		HttpClient httpClient = new HttpClient();
		GetMethod method = new GetMethod(HOST + "/articles");
		assert cookieString != null;
		method.setRequestHeader(new Header("Cookie", cookieString));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);
		List<Document> articles = (List<Document>)responseDoc.get("articles");
		assert !articles.isEmpty();
		id = (String)articles.get(0).get("id");
		assert  id != null;

		httpClient = new HttpClient();
		method = new GetMethod(HOST + "/resource/" + id + "/index.html");
//		assert cookieString != null;
//		method.setRequestHeader(new Header("Cookie", cookieString));
		code = httpClient.executeMethod(method);
		assert code == 200;
		is = method.getResponseBodyAsStream();
		responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Response " + responseStr);
		assert responseStr.contains("html");
	}

//	@Test
//	public void testNewArticleUserFailed() throws Exception {
//		login("lily", "123456");
//
//		HttpClient httpClient = new HttpClient();
//		PostMethod method = new PostMethod(HOST + "/articles");
//		assert cookieString != null;
//		method.setRequestHeader(new Header("Cookie", cookieString));
//		Part[] parts = new Part[3];
//		parts[0] = new StringPart("article", "{\"title\" : \"hello\", \"summary\" : \"This is summary\"}", "utf8");
//		parts[1] = new StringPart("html", "<html><body>Hello<img src=\"{unique123}\" /></body></html>", "utf8");
//		parts[2] = new FilePart("unique123", "icon40@2x.png", new File("data/icon40@2x.png"));
//		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
//		int code = httpClient.executeMethod(method);
//		assert code != 200;
//	}

	@Test
	public void testLoginAdmin() throws Exception {
		login("admin", "123456");
	}

	@Test
	public void testGetArticles() throws Exception {
//		testNewArticle();

		login("lily", "123456");

		HttpClient httpClient = new HttpClient();
		for(int i = 0; i < 10000; i++) {
			GetMethod method = new GetMethod(HOST + "/articles");
			assert cookieString != null;
			method.setRequestHeader(new Header("Cookie", cookieString));
			int code = httpClient.executeMethod(method);
			assert code == 200;
			InputStream is = method.getResponseBodyAsStream();
			String responseStr = IOUtils.toString(is, "utf8");
			System.out.println("Response " + responseStr);
			Document responseDoc = Document.parse(responseStr);
			assert responseDoc.get("code").equals(1);
			List<Document> articles = (List<Document>)responseDoc.get("articles");
			System.out.println("articles size " + articles.size());
//			assert !articles.isEmpty();
		}

	}

	@Test
	public void testDeleteArticles() throws Exception {
		HttpClient httpClient = new HttpClient();
		for(int i = 0; i < 300; i++) {
			testNewArticle();

			login("aplomb", "123456");

			GetMethod method = new GetMethod(HOST + "/articles");
			assert cookieString != null;
			method.setRequestHeader(new Header("Cookie", cookieString));
			int code = httpClient.executeMethod(method);
//			assert code == 200;
			if(code != 200)
				System.out.println(method.getURI() + " i " + i + " code " + code + " phrase " + method.getStatusText() + " i = " + i);
			InputStream is = method.getResponseBodyAsStream();
			String responseStr = IOUtils.toString(is, "utf8");
			System.out.println("Response " + responseStr);
			Document responseDoc = Document.parse(responseStr);
//			assert responseDoc.get("code").equals(1);
			List<Document> articles = (List<Document>)responseDoc.get("articles");
//			System.out.println("article size " + articles.size());
//			assert !articles.isEmpty();

			for(Document article : articles) {
				String id = (String)article.get("id");
				DeleteMethod deleteMethod = new DeleteMethod(HOST + "/article/" + id);
				assert cookieString != null;
				deleteMethod.setRequestHeader(new Header("Cookie", cookieString));
				code = httpClient.executeMethod(deleteMethod);
//				assert code == 200;
				if(code != 200)
					System.out.println(deleteMethod.getURI() + " i " + i + " code " + code + " phrase " + deleteMethod.getStatusText() + " i = " + i);
				is = deleteMethod.getResponseBodyAsStream();
				responseStr = IOUtils.toString(is, "utf8");
//			System.out.println("Response " + responseStr);
				responseDoc = Document.parse(responseStr);
//				assert responseDoc.get("code").equals(1);
			}
		}
	}

	@Test
	public void testReload() throws Exception {
		HttpClient httpClient = new HttpClient();
		for(int i = 0; i < 1000; i++) {
			GetMethod method = new GetMethod(HOST + "/reload");
			int code = httpClient.executeMethod(method);
			assert code == 200;
		}
	}
}
