package groovyx.net.ws;

import groovy.lang.GroovyObjectSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.namespace.QName;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.codehaus.groovy.runtime.InvokerHelper;


public class WSClient extends GroovyObjectSupport {

    private Client client = null;
    private String loc = null;
    private Map<String, String> mproxy = null;
    private Map<String, String> mssl = null;
    private Map<String, String> mba = null;
    private Boolean bssl = false;
    private Boolean bmtom = false;
    private Boolean bproxy = false;
    private Boolean bba = false;
    private ClassLoader cl = null;
    private String wsdl = null;
    private TrustManagerFactory tmf = null;
    private KeyManagerFactory kmf = null;


    public Object invokeMethod(String name, Object args) {

        Object[] args_a = InvokerHelper.asArray(args);
        Object[] response;

        try {
            QName qname = new QName(client.getEndpoint().getService().getName().getNamespaceURI(), name);
            BindingOperationInfo op = client.getEndpoint().getEndpointInfo().getBinding().getOperation(qname);

            if (op == null)
                return null;

            if (op.isUnwrappedCapable())
                response = client.invoke(op.getUnwrappedOperation(), args_a);
            else
                response = client.invoke(name, args_a);

            if (response == null)
                return null;
            else
                return response[0];

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object create(String name) {
        Object o = null;

        try {
            o = Thread.currentThread().getContextClassLoader().loadClass(name).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return o;
    }

    private void initializeBa() {
        mba.put("http.user", System.getProperty("http.user"));
        mba.put("http.password", System.getProperty("http.password"));
    }

    private void initializeProxy() {
        mproxy.put("http.proxyHost", System.getProperty("http.proxyHost"));
        mproxy.put("http.proxyPort", System.getProperty("http.proxyPort"));
        mproxy.put("http.proxy.user", System.getProperty("http.proxy.user"));
        mproxy.put("http.proxy.password", System.getProperty("http.proxy.password"));
    }

    public WSClient(String loc, ClassLoader cl) {

        this.cl = cl;
        this.loc = loc;

        URL url;

        try {
            url = new URL(loc);
            if (url.getProtocol().equals("https"))
                setSSL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (bba)
            initializeBa();
        if (bproxy)
            initializeProxy();
    }

    public void getWsdl() throws IllegalArgumentException {
        URL url;

        try {
            url = new URL(loc);

            if (url.getQuery().compareTo("wsdl") > 0)
                throw new IllegalArgumentException(
                        "Bad query. Expected 'wsdl', not '" + url.getQuery()
                                + "'");

            if (url.getProtocol().equals("https")) {
                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");// Here we
                    // choose
                    // the TLS
                    // protocol
                    // ... for
                    // now
                    if (kmf != null && tmf != null)
                        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                    else if (kmf == null)
                        ctx.init(null, tmf.getTrustManagers(), null);
                    else if (tmf == null)
                        ctx.init(kmf.getKeyManagers(), null, null);

                    SSLSocket socket = (SSLSocket) ctx.getSocketFactory()
                            .createSocket(url.getHost(), url.getPort());
                    try {
                        socket.startHandshake();
                    } catch (SSLHandshakeException e) {
                        System.out.println("Error during SSL handshake between client and server. If you enabled client authentication for the server, then you must pass keystore parameters to the client");
                        e.printStackTrace();
                    }

                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println("GET " + url.getFile() + " HTTP/1.0");
                    out.println();
                    out.flush();

                    if (out.checkError())
                        System.out.println("SSLSocketClient: " + out.getClass().getName() + " error");

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    File myWsdl = File.createTempFile("wsdl", null);
                    wsdl = myWsdl.getAbsolutePath();
                    BufferedWriter wout = new BufferedWriter(new FileWriter(
                            myWsdl));
                    String inputLine;
                    int i = 0;
                    while ((inputLine = in.readLine()) != null)
                        if (i++ > 3)
                            wout.write(inputLine + "\n");

                    in.close();
                    wout.close();
                    out.close();
                    socket.close();

                } catch (KeyManagementException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if (url.getProtocol().equals("http")) {
                wsdl = url.toString();
            } else {
                System.out.println("Unknown protocol " + url.getProtocol());
            }

        } catch (MalformedURLException e1) {
            wsdl = loc;
        }
    }


    public void create() {

        if (bssl)
            configureSSL();

        getWsdl();

        try {
            client = DynamicClientFactory.newInstance().createClient(wsdl, cl);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy httpClientPolicy = conduit.getClient();
        httpClientPolicy.setAllowChunking(false);
        conduit.setClient(httpClientPolicy);

        if (bssl)
            enableSSL();
        if (bmtom)
            enableMtom();
        if (bba)
            enableBasicAuthentication();
        if (bproxy)
            enableProxy();

    }

    public void setMtom(Boolean bmtom) {
        this.bmtom = bmtom;

    }

    private void enableMtom() {
        client.getRequestContext().put("mtom-enabled", Boolean.TRUE);
    }

    public void setBasicAuthentication(Map<String, String> mba) {
        bba = true;
        this.mba = mba;
    }

    public void enableBasicAuthentication() {

        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        String username = mba.get("http.user");
        String password = mba.get("http.password");

        if ((username != null) && (password != null)) {
            AuthorizationPolicy auth = conduit.getAuthorization();
            auth.setUserName(username);
            auth.setPassword(password);
        }
    }

    public void setProxy(Map<String, String> mproxy) {
        this.mproxy = mproxy;
        bproxy = true;
    }

    public void enableProxy() {

        bproxy = true;

        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        String host = mproxy.get("http.proxyHost");
        String port = mproxy.get("http.proxyPort");
        String proxyUsername = mproxy.get("http.proxy.user");
        String proxyPassword = mproxy.get("http.proxy.password");

        if (host != null) {
            conduit.getClient().setProxyServer(host);
            if (port != null) {
                conduit.getClient().setProxyServerPort(Integer.parseInt(port));
            }
            if ((proxyUsername != null) && (proxyPassword != null)) {
                conduit.getProxyAuthorization().setUserName(proxyUsername);
                conduit.getProxyAuthorization().setPassword(proxyPassword);
            }
        }
    }

    public void setSSL(Map<String, String> mssl) {
        this.mssl = mssl;
        this.bssl = true;
    }

    public void setSSL() {

        this.mssl = new HashMap<String, String>();
        this.bssl = true;

        String def_truststore = System.getProperty("java.home")
                + "/lib/security/cacerts";
        String def_truststore_pass = "changeit";

        mssl.put("https.keystore", System.getProperty("https.keystore", ""));
        mssl.put("https.keystore.pass", System.getProperty(
                "https.keystore.pass", ""));
        mssl.put("https.truststore", System.getProperty("https.truststore",
                def_truststore));
        mssl.put("https.truststore.pass", System.getProperty(
                "https.truststore.pass", def_truststore_pass));
    }

    private void configureSSL() {

        KeyStore keyStore = null;

        String strKeystore = mssl.get("https.keystore");
        String strKsPass = mssl.get("https.keystore.pass");
        String strTruststore = mssl.get("https.truststore");
        String strTsPass = mssl.get("https.truststore.pass");

        try {
            keyStore = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            if (strKeystore.compareTo("") > 0) {

                File thekeystore = new File(strKeystore);
                assert keyStore != null;
                keyStore.load(new FileInputStream(thekeystore), strKsPass.toCharArray());
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, strKsPass.toCharArray());
            }

            if (strTruststore.compareTo("") > 0) {

                File thetruststore = new File(strTruststore);
                assert keyStore != null;
                keyStore.load(new FileInputStream(thetruststore), strTsPass.toCharArray());
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

    private void enableSSL() {

        TLSClientParameters tlsParams = new TLSClientParameters();

        tlsParams.setDisableCNCheck(true); // At least for development
        if (kmf != null)
            tlsParams.setKeyManagers(kmf.getKeyManagers());
        if (tmf != null)
            tlsParams.setTrustManagers(tmf.getTrustManagers());

        FiltersType filters = new FiltersType();
        filters.getInclude().add(".*_EXPORT_.*");
        filters.getInclude().add(".*_EXPORT1024_.*");
        filters.getInclude().add(".*_WITH_DES_.*");
        filters.getInclude().add(".*_WITH_NULL_.*");
        filters.getInclude().add(".*_DH_anon_.*");

        tlsParams.setCipherSuitesFilter(filters);

        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        conduit.setTlsClientParameters(tlsParams);
    }

}
