package rest

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.result.DeleteResult
import connectors.mongodb.annotations.DocumentField
import connectors.mongodb.codec.DataObject
import db.Account
import db.MediaResource
import org.bson.Document;

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.runtime.GroovyRuntime;
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.PathVariable
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlet.annotation.RequestParam
import script.groovy.servlets.GroovyServlet
import script.groovy.servlets.RequestHolder
import chat.errors.CoreException
import chat.logs.LoggerEx
import db.UserCollection
import db.UserData
import db.UserStatusCollection
import db.services.UserService

//@ControllerMapping(interceptClass = "core/intercepters/InternalServerIntercepter.groovy")
@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class AdminController extends GroovyServlet{
	private static final String TAG = "ADMIN";
	
	@Bean
	private GroovyObjectEx<UserCollection> userCollection;
	
	@Bean
	private GroovyObjectEx<UserStatusCollection> userStatusCollection;
	
	@Bean
	private GroovyObjectEx<UserService> userService;

	/**
	 * 获取Balancer概要信息的接口
	 * @param holder
	 * @throws CoreException
	 */
	@RequestMapping(uri = "info/{hello}", method = GroovyServlet.GET)
	public void getMemoryInfo(
		HttpServletRequest request, 
		HttpServletResponse response, 
		RequestHolder holder,
		@RequestHeader(key = "User-Agent") String ua,
		@PathVariable(key = "hello") Long hello, 
		@RequestParam(key="qq") int qq) throws CoreException{
		LoggerEx.info(TAG, "hello " + hello + " qq " + qq + " request " + request + " response " + response + " holder " + holder + " ua " + ua);
		StringBuilder buidler = new StringBuilder();

		MongoCollection<DataObject> theUserCollection = userCollection.getObject().getMongoCollection();
		DeleteResult result = theUserCollection.deleteMany(new Document());
		buidler.append(" ua " + ua).append(" hello " + hello).append(" qq " + qq);

		buidler.append(" delelted ").append(result.deletedCount);

		UserData user = new UserData();
		user.name = "aplomb";
		user.integerNum = 1;
		MediaResource mr1 = new MediaResource();
		mr1.resourceId = "1233";
		mr1.thumbnailResourceId = "1231233";
		user.resources = Arrays.asList(mr1, mr1);
		user.intNum = 2;
		user.data = [1, 2, 3];
		user.strs = Arrays.asList("a", "d");
		Account account = new Account();
		account.loginAccount = "1.1";
		account.resources = Arrays.asList(mr1, mr1);
		account.strs = Arrays.asList("aaa", "bbb");
		
		MediaResource mr = new MediaResource();
		mr.resourceId = "adf";
		mr.thumbnailResourceId = "as";
		account.mediaResource = mr;
		
		user.account = account;
		user.type = "user";
		theUserCollection.insertOne(user);
		
		FindIterable<DataObject> iterable = theUserCollection.find();
		MongoCursor<DataObject> cursor = iterable.iterator();
		buidler.append(" find " + user.getId() + ": ");
		while(cursor.hasNext()) {
			DataObject dataObject = cursor.next();
			buidler.append(dataObject);
		}

		UserService userService = userService.getObject();
		UserData user1 = userService.getUser(user.getId());
		buidler.append(" user " + user1);
		response.getOutputStream().write(buidler.toString().getBytes());
	}
	
	@RequestMapping(uri = "reload", method = GroovyServlet.GET)
	public void getMemoryInfo(HttpServletRequest request, 
		HttpServletResponse response) throws CoreException{
		LoggerEx.info(TAG, "hello1");
		GroovyRuntime.getInstance().redeploy();
		response.getOutputStream().write("Done".getBytes());
	}

	@RequestMapping(uri = "gc", method = GroovyServlet.GET)
	public void gc(HttpServletRequest request,
							  HttpServletResponse response) throws CoreException{
		System.gc();
		response.getOutputStream().write(("used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "M").getBytes());
	}

}
