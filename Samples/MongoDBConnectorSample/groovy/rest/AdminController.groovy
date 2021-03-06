package rest

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bson.Document

import script.groovy.annotation.Bean
import script.groovy.runtime.GroovyRuntime
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.PathVariable
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlet.annotation.RequestParam
import script.groovy.servlets.GroovyServlet
import script.groovy.servlets.RequestHolder
import chat.errors.CoreException
import chat.logs.LoggerEx

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.result.DeleteResult

import connectors.mongodb.codec.DataObject
import db.Account
import db.MediaResource
import db.QuestStatus
import db.UserCollection
import db.UserData
import db.UserStatusCollection
import db.services.QuestStatusService
import db.services.UserService

//@ControllerMapping(interceptClass = "core/intercepters/InternalServerIntercepter.groovy")
@ControllerMapping(interceptClass = "intercepters/CommonIntercepter.groovy")
public class AdminController extends GroovyServlet{
	private static final String TAG = "ADMIN";
	
	@Bean
	private UserCollection userCollection;
	
	@Bean
	private UserStatusCollection userStatusCollection;
	
	@Bean
	private UserService userService;
	
	@Bean
	private QuestStatusService questStatusService;

	@RequestMapping(uri = "test/aaa", method = GroovyServlet.GET)
	public void getMemoryInfo1(){
		QuestStatus qs = questStatusService.getQuestStatus("5577bc4284ae5740892a5fe2");
		println qs;
	}
	
	@RequestMapping(uri = "test/bbb", method = GroovyServlet.GET)
	public void getMemoryInfo2(){
		QuestStatus qs = questStatusService.getQuestStatus("5577bc4284ae5740892a5fe2");
		qs.setId("asdf");
		questStatusService.addQuestStatus(qs);
	}
	
	/**
	 * 获取Balancer概要信息的接口
	 * @param holder
	 * @throws CoreException
	 */
	@RequestMapping(uri = "info/{hello}", method = GroovyServlet.GET)
	public void getMemoryInfo1(
		HttpServletRequest request, 
		HttpServletResponse response, 
		RequestHolder holder,
		@RequestHeader(key = "User-Agent") String ua,
		@PathVariable(key = "hello") Long hello, 
		@RequestParam(key="qq") int qq) throws CoreException{
		LoggerEx.info(TAG, "hello " + hello + " qq " + qq + " request " + request + " response " + response + " holder " + holder + " ua " + ua);
		StringBuilder buidler = new StringBuilder();

		MongoCollection<DataObject> theUserCollection = userCollection.getMongoCollection();
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

		UserData user1 = userService.getUser(user.getId());
		buidler.append(" user " + user1);
		response.getOutputStream().write(buidler.toString().getBytes());
	}
	
	@RequestMapping(uri = "reload", method = GroovyServlet.GET)
	public void getMemoryInfo(HttpServletRequest request, 
		HttpServletResponse response) throws CoreException{
		LoggerEx.info(TAG, "hello1");
//		GroovyRuntime.getInstance().redeploy();
		response.getOutputStream().write("Done".getBytes());
	}

	@RequestMapping(uri = "gc", method = GroovyServlet.GET)
	public void gc(HttpServletRequest request,
							  HttpServletResponse response) throws CoreException{
		System.gc();
		response.getOutputStream().write(("used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "M").getBytes());
	}
	  @RequestMapping(uri = "hello", method = GroovyServlet.GET)
	  public void hello(HttpServletRequest request,
								HttpServletResponse response) throws CoreException{
		  println userService.hello();
		  response.getOutputStream().write(("used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "M").getBytes());
	  }
}
