package controllers;

import base.ChatTestCase;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class CompanyControllerTest extends ChatTestCase {
	@Test
	public void testNewCompanyEmployeeFailed() throws Exception {
		//aplomb user don't have admin permission to create Company
		Document user = login("aplomb", "123456");
		Document login = (Document)user.get("login");
		String aplombId = (String)login.get("id");

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/company");
		Document doc = new Document();
		doc.put("employeeIds", Arrays.asList(aplombId));
		doc.put("name", "AplombCompany");
		assert cookieString != null;
		method.setRequestHeader(new Header("Cookie", cookieString));
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code != 200;
	}

	@Test
	public void testNewCompanyUserFailed() throws Exception {
		//aplomb user don't have admin permission to create Company
		Document user = login("lily", "123456");
		Document login = (Document)user.get("login");
		String lilyId = (String)login.get("id");

		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(HOST + "/company");
		Document doc = new Document();
		doc.put("employeeIds", Arrays.asList(lilyId));
		doc.put("name", "LilyCompany");
		assert cookieString != null;
		method.setRequestHeader(new Header("Cookie", cookieString));
		method.setRequestEntity(new StringRequestEntity(doc.toJson()));
		int code = httpClient.executeMethod(method);
		assert code != 200;
	}

	@Test
	public void testListCompaniesUserFailed() throws Exception {
		//aplomb user don't have admin permission to create Company
		Document user = login("lily", "123456");

		HttpClient httpClient = new HttpClient();
		GetMethod method = new GetMethod(HOST + "/companies");
		assert cookieString != null;
		method.setRequestHeader(new Header("Cookie", cookieString));
		int code = httpClient.executeMethod(method);
		assert code != 200;
	}

	@Test
	public void testListCompanies() throws Exception {
		//aplomb user don't have admin permission to create Company
		Document user = login("admin", "123456");

		HttpClient httpClient = new HttpClient();
		GetMethod method = new GetMethod(HOST + "/companies");
		assert cookieString != null;
		method.setRequestHeader(new Header("Cookie", cookieString));
		int code = httpClient.executeMethod(method);
		assert code == 200;
		InputStream is = method.getResponseBodyAsStream();
		String responseStr = IOUtils.toString(is, "utf8");
		System.out.println("Response " + responseStr);
		Document responseDoc = Document.parse(responseStr);
		List<Document> companies = (List<Document>)responseDoc.get("companies");
		assert companies.size() == 1;
		assert companies.get(0).get("name").equals("MyCompany");
	}
}
