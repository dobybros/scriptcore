

import groovy.transform.CompileStatic

import java.util.concurrent.Callable

import a.Account
import a.Aplomb
import a.CUser
import a.User

import com.balancer.main.Test

@CompileStatic
class test implements Callable{
	public static void main(String[] args) {
//		Aplomb aplomb = new Aplomb();
		User user = new User();
		user.strs = new ArrayList<String>();
		for(int i = 0; i < 100000; i++) {
			user.strs.add("asaax");
		}  
		System.gc();
//		println "bbbb " + new Account().a() + " " + Test.A + " aplomb " + aplomb.a1() + " user " + user.a1() + " CUser " + new CUser().a1();
		println "hello " + user.strs.size() + "|||||" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "k";
	}
	
	String hello123() {
		return "阿斯蒂芬,kk45";
	}
	
	public onStart() {
		
	}
	
	public void onRelease() {
//		users.clear();
	}
	
	@Override
	public Object call() throws Exception {
		
		def hello = hello123();
		println hello + " size ";
//		users.clear();
		return hello;
	}
	
}


