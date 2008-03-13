package groovyx.net.ws;

import groovy.lang.GroovyClassLoader;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.mortbay.jetty.security.SslSocketConnector;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;


public class WSServer {
    private ServerFactoryBean sf;

    private KeyStore ks = null;

    public WSServer(AuthorizationPolicy ap) {
    }

    public WSServer() throws KeyStoreException {
        sf = new ServerFactoryBean();

        Map<String,Object> props = new HashMap<String, Object>();
//      props.put("mtom-enabled", Boolean.TRUE);
        sf.setProperties(props);

        sf.getServiceFactory().getServiceConfigurations().add(0, new GroovyConfiguration());
        sf.getServiceFactory().setDataBinding(new AegisDatabinding());
/*
        TLSServerParameters tlsServerParameters = new TLSServerParameters();

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream("keyStoreName");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        char[] password = null;
        try {
            ks.load(fis, password);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (CertificateException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        TrustManagerFactory tmf = null;
        try {
            tmf = TrustManagerFactory.getInstance( "PKIX", "SunJSSE" );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchProviderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        tmf.init( ks );

        Server server = sf.getServer();
        sf.


        tlsServerParameters.setTrustManagers();*/

    }

    public void setNode(String text, String url) throws ClassNotFoundException {
        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());

        Class clazz = gcl.loadClass(text);
        sf.setServiceClass(clazz);
        sf.setAddress(url);
        sf.create();
    }
}
