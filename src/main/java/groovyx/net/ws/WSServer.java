package groovyx.net.ws;

import groovy.lang.GroovyClassLoader;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.type.TypeCreationOptions;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.cxf.common.logging.LogUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class WSServer {

	private ServerFactoryBean sf;
	private Boolean bmtom = false;
	private Boolean bssl = false;
	private Map<String, String> mssl = null;
	private Boolean bca = false;
	private String service;
	private String url;
	private TrustManagerFactory tmf = null;
	private KeyManagerFactory kmf = null;

    /**
     * @return The logger for the class
     */
    protected Logger getLogger() {
        return LogUtils.getL7dLogger(getClass());
    }

    /**
     * Default constructor
     */
	public WSServer(){
		this.sf = new ServerFactoryBean();
	}

	public WSServer(String service, String url){
        // TODO: fix service is not used
		this(url);
    }

	public WSServer(String url){
		this();
		try {
			this.service = new URL(url).getPath().replaceFirst("/", "");
		    if( new URL(url).getProtocol().equals("https")) {
                setSSL();
            }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.url = url;

	}

    /**
     * Attach a service class with its corresponding url
     *
     * @param service script implementing the business service  
     * @param url url that should be used to connect to the service
     */
	public void setNode(String service, String url){
		this.service = service;
		this.url = url;
	}

	public void setMtom(Boolean bmtom){
		this.bmtom = bmtom;
	}

	public void setSSL() {
		this.bssl = true;
		this.mssl = new HashMap<String, String>();

		String def_truststore = System.getProperty("java.home")+ "/lib/security/cacerts";
		String def_truststore_pass = "changeit";

		this.mssl.put("https.keystore", System.getProperty("https.keystore", ""));
		this.mssl.put("https.keystore.pass", System.getProperty("https.keystore.pass", ""));
		this.mssl.put("https.truststore", System.getProperty("https.truststore", def_truststore));
		this.mssl.put("https.truststore.pass", System.getProperty("https.truststore.pass", def_truststore_pass));
    }


	public void setSSL(Map<String, String> mssl){
        this.bssl = true;
		this.mssl = mssl;
	}

	public void setClientAuthentication(Boolean bca) {
		this.bca = bca;
	}

	@SuppressWarnings("unchecked")
    /**
     * Start the server
     */
	public void start(){

        if (this.mssl != null){
            getLogger().fine("> "+this.mssl.get("https.keystore")+" <");
            getLogger().fine("> "+this.mssl.get("https.keystore.pass")+" <");
	        getLogger().fine("> "+this.mssl.get("https.truststore")+" <");
            getLogger().fine("> "+this.mssl.get("https.truststore.pass")+" <");
        }

		AegisDatabinding aegisDb = new AegisDatabinding();

    	if (this.bmtom){
    		Map<String,Object> props = new HashMap<String, Object>();
      		props.put("mtom-enabled", Boolean.TRUE);
        	this.sf.setProperties(props);

    		aegisDb.setMtomEnabled(true);
    	}

    	this.sf.getServiceFactory().getServiceConfigurations().add(0, new GroovyConfiguration());

    	TypeCreationOptions conf = aegisDb.getAegisContext().getTypeCreationOptions();
    	conf.setDefaultMinOccurs(1);
    	conf.setDefaultNillable(false);

    	this.sf.getServiceFactory().setDataBinding(aegisDb);

    	if (this.bssl){
    		configureSSL();

    		TLSServerParameters tlsParams = new TLSServerParameters();

    		if (this.kmf != null)
            {
                tlsParams.setKeyManagers(this.kmf.getKeyManagers());
            }
    		if (this.tmf != null)
            {
                tlsParams.setTrustManagers(this.tmf.getTrustManagers());
            }

    		FiltersType filters = new FiltersType();
//1    		filters.getInclude().add(".*_EXPORT_.*");
//1    		filters.getInclude().add(".*_EXPORT1024_.*");
//1    		filters.getInclude().add(".*_WITH_DES_.*");
//1    		filters.getInclude().add(".*_WITH_NULL_.*");
//1            filters.getInclude().add(".*_WITH_3DES_.*");
//    		filters.getInclude().add(".*_DH_anon_.*");
            // May be we can use this instead
            filters.getInclude().add(".*");
            filters.getExclude().add(".*_DH_anon_.*");
    		tlsParams.setCipherSuitesFilter(filters);

    		if (this.bca){
    			ClientAuthentication ca = new ClientAuthentication();
    			ca.setRequired(true);
    			ca.setWant(true);
    			tlsParams.setClientAuthentication(ca);
    		}

    		JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
    		try {
				factory.setTLSServerParametersForPort(new URL(this.url).getPort(), tlsParams);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}


    	GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());
    	try {
			Class clazz = gcl.loadClass(this.service);
			this.sf.setServiceClass(clazz);
			this.sf.setAddress(this.url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	this.sf.create();
	}


	public void stop(){
		try{
			this.sf.getServer().stop();
			this.sf.getBus().shutdown(true);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void configureSSL() {

		String strKeystore = this.mssl.get("https.keystore");
		String strKsPass = this.mssl.get("https.keystore.pass");
		String strTruststore = this.mssl.get("https.truststore");
		String strTsPass = this.mssl.get("https.truststore.pass");

		KeyStore keyStore = null;

		try {
			keyStore = KeyStore.getInstance("JKS");
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
			if (strKeystore.compareTo("")>0) {
				File thekeystore = new File(strKeystore);
                assert keyStore != null;
                keyStore.load(new FileInputStream(thekeystore), strKsPass.toCharArray());
				this.kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				this.kmf.init(keyStore, strKsPass.toCharArray());
			}

			if (strTruststore.compareTo("")>0) {
				File thetruststore = new File(strTruststore);
                assert keyStore != null;
                keyStore.load(new FileInputStream(thetruststore), strTsPass.toCharArray());
				this.tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				this.tmf.init(keyStore);
			}

		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
