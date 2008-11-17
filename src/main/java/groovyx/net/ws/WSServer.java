package groovyx.net.ws;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.type.TypeCreationOptions;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;

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
	
	
	public WSServer(){
		sf = new ServerFactoryBean();
	}
	
	public WSServer(String service, String url){
		this(url);
    }
	
	public WSServer(String url){
		this();
		try {
			this.service = new URL(url).getPath().replaceFirst("/", "");
			if( new URL(url).getProtocol().equals("https"))
				setSSL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.url = url;
		
	}
	
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

		mssl.put("https.keystore", System.getProperty("https.keystore", ""));
		mssl.put("https.keystore.pass", System.getProperty("https.keystore.pass", ""));
		mssl.put("https.truststore", System.getProperty("https.truststore", def_truststore));
		mssl.put("https.truststore.pass", System.getProperty("https.truststore.pass", def_truststore_pass));
	}
	
	public void setSSL(Map<String, String> mssl){
		this.bssl = true;
		this.mssl = mssl;
	}

	public void setClientAuthentication(Boolean bca) {
		this.bca = bca;
	}
	
	@SuppressWarnings("unchecked")
	public void start(){
		
		AegisDatabinding aegisDb = new AegisDatabinding();
    	
    	if (bmtom){
    		Map<String,Object> props = new HashMap<String, Object>();
      		props.put("mtom-enabled", Boolean.TRUE);
        	sf.setProperties(props);
        	
    		aegisDb.setMtomEnabled(true);
    	}
    	
    	sf.getServiceFactory().getServiceConfigurations().add(0, new GroovyConfiguration());		

    	TypeCreationOptions conf = aegisDb.getAegisContext().getTypeCreationOptions();
    	conf.setDefaultMinOccurs(1);
    	conf.setDefaultNillable(false);
    	
    	sf.getServiceFactory().setDataBinding(aegisDb);

    	if(bssl){
    		configureSSL();
    		
    		TLSServerParameters tlsParams = new TLSServerParameters();
    		
    		if(kmf!=null)
    			tlsParams.setKeyManagers(kmf.getKeyManagers());
    		if(tmf!=null)
    			tlsParams.setTrustManagers(tmf.getTrustManagers());

    		FiltersType filters = new FiltersType();
    		filters.getInclude().add(".*_EXPORT_.*");
    		filters.getInclude().add(".*_EXPORT1024_.*");
    		filters.getInclude().add(".*_WITH_DES_.*");
    		filters.getInclude().add(".*_WITH_NULL_.*");
    		filters.getInclude().add(".*_DH_anon_.*");
    		tlsParams.setCipherSuitesFilter(filters);
    		
    		if(bca){
    			ClientAuthentication ca = new ClientAuthentication();
    			ca.setRequired(true);
    			ca.setWant(true);
    			tlsParams.setClientAuthentication(ca);
    		}
    		
    		JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
    		try {
				factory.setTLSServerParametersForPort(new URL(url).getPort(), tlsParams);
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
			Class clazz = gcl.loadClass(service);
			sf.setServiceClass(clazz);
			sf.setAddress(url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	sf.create();
	}
	
	
	public void stop(){
		try{
			sf.getServer().stop();
			sf.getBus().shutdown(true);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void configureSSL() {

		String strKeystore = mssl.get("https.keystore");
		String strKsPass = mssl.get("https.keystore.pass");
		String strTruststore = mssl.get("https.truststore");
		String strTsPass = mssl.get("https.truststore.pass");

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
				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, strKsPass.toCharArray());
			}

			if (strTruststore.compareTo("")>0) {
				
				File thetruststore = new File(strTruststore);
				FileInputStream myin = new FileInputStream(thetruststore);
                assert keyStore != null;
                keyStore.load(myin, strTsPass.toCharArray());
				tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(keyStore);
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