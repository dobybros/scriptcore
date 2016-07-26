package base;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.bson.Document;


public class ChatTestCase {
	private static final String TAG = ChatTestCase.class.getSimpleName();
	
	final String logPath = "/Users/twk/Desktop/logs";
	
	protected Header[] cookies;
	protected String userName;
	protected String tcpHost;
	protected String cookieString="";
	
	protected static final String HOST = "http://localhost:6066/rest";
	
	protected Document login(String account, String password) throws Exception {
		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/login");
		Document doc = new Document();
		doc.put("account", account);
		doc.put("pwd", Base64.encodeBase64String(password.getBytes("utf8")));
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		Header cookieHeader = method.getResponseHeader("Set-Cookie");
		if(cookieHeader != null) {
			cookieString = cookieHeader.getValue();
		}
		assert cookieString != null;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Login response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);
		return responseDoc;
	}
	
	
}
