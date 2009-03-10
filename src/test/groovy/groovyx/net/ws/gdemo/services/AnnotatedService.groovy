package groovyx.net.ws.gdemo.services

import java.util.UUID
import javax.annotation.*;
import javax.xml.ws.WebServiceContext;

class AnnotatedService {

    @Resource
    public WebServiceContext wsc

    public String getSession() {
        if( wsc == null) {
             //println "WSC NULL !"
             //return UUID.randomUUID()
             return nul
         } else {
             wsc.getMessageContext().get( "sessionid")
         }
    }
}
