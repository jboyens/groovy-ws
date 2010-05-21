package groovyx.net.ws.cxf;

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
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.endpoint.Client;

/**
 * Helper class to configure a ssl connection.
 *
 * @see SettingConstants#HTTPS_KEYSTORE
 * @see SettingConstants#HTTPS_KEYSTORE_PASS
 * @see SettingConstants#HTTPS_TRUSTSTORE
 * @see SettingConstants#HTTPS_TRUSTSTORE_PASS
 * 
 * @author <a href="mailto:guillaume.alleon@gmail.com">Tog</a>
 * 
 * @since 0.5
 */
public class SSLHelper extends AbstractSettingHelper {
    /**
     * Default constructor
     *
     */
    public SSLHelper() {
    }

    /**
     * The trust manager-factory
     */
    private TrustManagerFactory trustStoreManager;

    /**
     * The key manager-factory
     */
    private KeyManagerFactory keyStoreManager;

    /**
     * Reads the system properties and puts the value of
     * {@link SettingConstants#HTTPS_KEYSTORE}, {@link SettingConstants#HTTPS_KEYSTORE_PASS},
     * {@link SettingConstants#HTTPS_TRUSTSTORE} and {@link SettingConstants#HTTPS_TRUSTSTORE_PASS}
     * into the property-map.
     */
    @Override
    protected void setDefaultProperties() {
        this.properties = new HashMap<String, String>();

        String def_truststore = System.getProperty("java.home") + "/lib/security/cacerts";
        String def_truststore_pass = null;

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JKS");
        }
        catch (KeyStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        assert keyStore != null;

        String[] candidatePassword = new String[]{"changeit", "changeme"};

        for (String it:candidatePassword){

            if (def_truststore_pass == null) {
                try {
                    File thetruststore = new File(def_truststore);
                    keyStore.load(new FileInputStream(thetruststore), it.toCharArray());
                    def_truststore_pass = it;
                } catch (CertificateException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    // try next one
                }
            }
        }

        assert def_truststore_pass != null;

        this.properties.put(SettingConstants.HTTPS_KEYSTORE,
                System.getProperty(SettingConstants.HTTPS_KEYSTORE, ""));
        this.properties.put(SettingConstants.HTTPS_KEYSTORE_PASS,
                System.getProperty(SettingConstants.HTTPS_KEYSTORE_PASS, ""));
        this.properties.put(SettingConstants.HTTPS_TRUSTSTORE,
                System.getProperty(SettingConstants.HTTPS_TRUSTSTORE, def_truststore));
        this.properties.put(SettingConstants.HTTPS_TRUSTSTORE_PASS,
                System.getProperty(SettingConstants.HTTPS_TRUSTSTORE_PASS, def_truststore_pass));
    }

    /**
     * Configures the store managers by reading the set properties
     * and sets the settings for a ssl connection.
     */
    @Override
    public void initialize() {
        if (!isConfigured()) {
            setDefaultProperties();
        }
        configureStoreManager();

        //isConfiguredProperly(this.client.localWSDL);
    }

    /**
     * Configures the store managers by reading the set properties.
     */
    private void configureStoreManager() {

        String strKeystore = this.properties.get(SettingConstants.HTTPS_KEYSTORE);
        String strKsPass = this.properties.get(SettingConstants.HTTPS_KEYSTORE_PASS);
        String strTruststore = this.properties.get(SettingConstants.HTTPS_TRUSTSTORE);
        String strTsPass = this.properties.get(SettingConstants.HTTPS_TRUSTSTORE_PASS);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JKS");
        }
        catch (KeyStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // TODO keyStore == null -> return or throw exception?! (and remove assert)
        assert keyStore != null;

        try {
            if ("".compareTo(strKeystore) < 0) {
                File thekeystore = new File(strKeystore);
                keyStore.load(new FileInputStream(thekeystore), strKsPass.toCharArray());
                this.keyStoreManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                this.keyStoreManager.init(keyStore, strKsPass.toCharArray());
            }

            if ("".compareTo(strTruststore) < 0) {
                File thetruststore = new File(strTruststore);
                keyStore.load(new FileInputStream(thetruststore), strTsPass.toCharArray());
                this.trustStoreManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                this.trustStoreManager.init(keyStore);
            }
        }
        catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Sets the settings for a ssl connection.
     */
    @Override
    public void configureClientParameters(Client client) {
        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setDisableCNCheck(true); // At least for development

        if (this.keyStoreManager != null) {
            tlsParams.setKeyManagers(this.keyStoreManager.getKeyManagers());
        }
        if (this.trustStoreManager != null) {
            tlsParams.setTrustManagers(this.trustStoreManager.getTrustManagers());
        }

        FiltersType filters = new FiltersType();
        filters.getInclude().add(".*_EXPORT_.*");
        filters.getInclude().add(".*_EXPORT1024_.*");
        filters.getInclude().add(".*_WITH_DES_.*");
        filters.getInclude().add(".*_WITH_AES_.*");
        filters.getInclude().add(".*_WITH_RC4_.*");
        filters.getInclude().add(".*_WITH_3DES_.*");
        filters.getInclude().add(".*_WITH_NULL_.*");
        filters.getInclude().add(".*_DH_anon_.*");

        tlsParams.setCipherSuitesFilter(filters);

        HTTPConduit conduit = (HTTPConduit)client.getConduit();        

        conduit.setTlsClientParameters(tlsParams);
    }

    /**
     * Copy the WSDL locally and returns the local url
     *
     * @param  url The actual wsdl url
     * @return The local wsdl file url.
     */
    public URL getLocalWsdlUrl(URL url) {
        SSLContext ctx;
        try {
            // Here we choose the TLS protocol for now
            ctx = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            System.out.println("TLS-Algorithm could not be found in the ssl context");
            e.printStackTrace();

            return null;
        }

        try {
            if (this.keyStoreManager != null && this.trustStoreManager != null) {
                ctx.init(this.keyStoreManager.getKeyManagers(), this.trustStoreManager.getTrustManagers(), null);
            } else if (this.keyStoreManager == null) {
                ctx.init(null, this.trustStoreManager.getTrustManagers(), null);
            } else if (this.trustStoreManager == null) {
                ctx.init(this.keyStoreManager.getKeyManagers(), null, null);
            }
        } catch (KeyManagementException e) {
            getLogger().finest("SSL context could not be initialized");
            e.printStackTrace();

            return null;
        }

        SSLSocket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedWriter wout = null;
        try {

            int port = url.getPort();
            if (port == -1) port = url.getDefaultPort();

            socket = (SSLSocket) ctx.getSocketFactory().createSocket(url.getHost(), port);
            socket.startHandshake();

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            out.println("GET " + url.getFile() + " HTTP/1.0");
            out.println("Host: " + url.getHost());
            out.println();
            out.flush();

            if (out.checkError()) {
                System.out.println("SSLSocketClient: " + out.getClass().getName() + " error");
            }

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            File myWsdl = File.createTempFile("wsdl", null);
            //            this.wsdl = myWsdl.getAbsolutePath();
            wout = new BufferedWriter(new FileWriter(myWsdl));
            String inputLine;

            boolean readLine = false;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("<?xml")) {
                    readLine = true;
                }
                if (readLine) {
                    wout.write(inputLine + "\n");
                }
            }

            return myWsdl.toURI().toURL();

        } catch (UnknownHostException e) {
            getLogger().finest("The host: " + url.getHost()
                    + " is unknown or could not be determined at the moment");
            // TODO Auto-generated catch block
            e.printStackTrace();

            return null;
        } catch (SSLHandshakeException e) {
            getLogger().finest("Error during SSL handshake between client and server. If you enabled client "
                    + "authentication for the server, then you must pass keystore parameters to the client");
            // TODO Auto-generated catch block
            e.printStackTrace();

            return null;
        } catch (IOException e) {
            getLogger().finest("An error occured during creation of the ssl socket.");
            // TODO Auto-generated catch block
            e.printStackTrace();

            return null;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                }
                catch (IOException e) {
                    //Ignore
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }

            if (wout != null) {
                try {
                    wout.close();
                } catch (IOException e) {
                    // Ignore
                }
            }

            if (out != null) {
                out.close();
            }
        }
    }
}
