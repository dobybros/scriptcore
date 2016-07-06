package common.controllers

import chat.logs.LoggerEx
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import script.groovy.servlets.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
        return [code : 1];
    }

    Map failed(int code, String description) {
        return [code : code, desp : description];
    }

    void respond(HttpServletResponse response, Object map) {
        JsonBuilder builder = new JsonBuilder(map);
        String returnStr = builder.toString();

        LoggerEx.debug(this.getClass().getSimpleName(), "respond " + returnStr);
        response.getOutputStream().write(returnStr.getBytes("utf-8"));
    }
}
