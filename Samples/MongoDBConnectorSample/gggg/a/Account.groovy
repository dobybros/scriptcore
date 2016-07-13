package a

import script.groovy.annotation.Bean;
import groovy.transform.CompileStatic;

@CompileStatic
@Bean(name = "adkf")
class Account {
	private String name;
	private String description;
	
	public String a() {
		return "zzzz";
	}
}


