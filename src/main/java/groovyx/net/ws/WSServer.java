package groovyx.net.ws;

import groovy.lang.GroovyClassLoader;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ServerFactoryBean;

import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Map;


public class WSServer {
    private ServerFactoryBean sf;

    public WSServer() throws KeyStoreException {
        sf = new ServerFactoryBean();

        Map<String,Object> props = new HashMap<String, Object>();
//      props.put("mtom-enabled", Boolean.TRUE);      
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
