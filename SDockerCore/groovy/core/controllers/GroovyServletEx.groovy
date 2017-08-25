package core.controllers

import com.alibaba.fastjson.JSON;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import script.groovy.runtime.GroovyRuntime

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.io.IOUtils

import script.groovy.servlets.GroovyServlet
import core.utils.CleanLinkedHashMap

/**
 * Created by aplombchen on 3/7/16.
 */
class GroovyServletEx extends GroovyServlet {
    Object readJson(HttpServletRequest request) {
        String requestStr = IOUtils.toString(request.getInputStream(), "utf8");
        def slurper = new JsonSlurper()
        def json = slurper.parseText(requestStr);
        return json;
    }

    Map success() {
        return [code : 1] as CleanLinkedHashMap;
    }

    Map failed(int code, String description) {
        return [code : code, desp : description] as CleanLinkedHashMap;
    }

    void respond(HttpServletResponse response, Object map) {
//        JsonBuilder builder = new JsonBuilder(map);
//        String returnStr = builder.toString();
//        String returnStr = JsonOutput.toJson(map);
        String returnStr = JSON.toJSONString(map);

        response.setContentType("application/json");
//        LoggerEx.debug(this.getClass().getSimpleName(), "respond " + returnStr);
        response.getOutputStream().write(returnStr.getBytes("utf-8"));
    }
	
	void respondHtml(HttpServletResponse response, String text) {
		response.setContentType("text/html");
		response.getOutputStream().write(text.getBytes("utf-8"));
	}

}
