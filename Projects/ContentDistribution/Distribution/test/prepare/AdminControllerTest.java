package prepare;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.Test;
import base.ChatTestCase;

import java.io.InputStream;
import java.util.Arrays;

public class AdminControllerTest extends ChatTestCase {

	@Test
	public void testRegister() throws Exception {
		String account = "admin";
		String password = "123456";

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/admin/register");
		Document doc = new Document();
		doc.put("account", account);
		doc.put("pwd", Base64.encodeBase64String(password.getBytes("utf8")));
		doc.put("name", "Aplomb");
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Register response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);
	}

	@Test
	public void testRegisterEmployee() throws Exception {
		String account = "aplomb";
		String password = "123456";

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/register");
		Document doc = new Document();
		doc.put("account", account);
		doc.put("pwd", Base64.encodeBase64String(password.getBytes("utf8")));
		doc.put("name", "Aplomb");
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Register response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);
	}

	@Test
	public void testRegisterUser() throws Exception {
		String account = "lily";
		String password = "123456";

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/register");
		Document doc = new Document();
		doc.put("account", account);
		doc.put("pwd", Base64.encodeBase64String(password.getBytes("utf8")));
		doc.put("name", "Lily");
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Register response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);
	}

	@Test
	public void testNewCompany() throws Exception {
		Document user = login("aplomb", "123456");
		Document login = (Document)user.get("login");
		String aplombId = (String)login.get("id");

		user = login("admin", "123456");

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/company");
		Document doc = new Document();
		doc.put("employeeIds", Arrays.asList(aplombId));
		doc.put("name", "MyCompany");
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		method.setRequestHeader(new Header("Cookie", cookieString));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Register response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		assert responseDoc.get("code").equals(1);

	}
}
