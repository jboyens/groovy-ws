package groovyx.net.ws;

import java.util.HashMap;
import java.util.Map;

import groovy.lang.GroovyClassLoader;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;


public class WSServer {
    private ServerFactoryBean sf;

    public WSServer(AuthorizationPolicy ap) {
        sf = new ServerFactoryBean();

        Map<String,Object> props = new HashMap<String, Object>();
        props.put("mtom-enabled", Boolean.TRUE);      
        sf.setProperties(props);
        
        sf.getServiceFactory().getServiceConfigurations().add(0, new GroovyConfiguration());
        sf.getServiceFactory().setDataBinding(new AegisDatabinding());
    }

    public void setNode(String text, String url) throws ClassNotFoundException {
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());

        Class clazz = gcl.loadClass(text);
        sf.setServiceClass(clazz);
        sf.setAddress(url);
        sf.create();
    }
}
