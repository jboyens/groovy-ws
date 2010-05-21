package groovyx.net.ws

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
//import org.codehaus.xfire.XFire;
//import org.codehaus.xfire.XFireFactory;
//import org.codehaus.xfire.server.http.XFireHttpServer;
//import org.codehaus.xfire.service.Service;
//import org.codehaus.xfire.service.invoker.ObjectInvoker;
//import org.codehaus.xfire.service.binding.ObjectServiceFactory;
//import org.codehaus.xfire.xmlbeans.XmlBeansServiceFactory;
//import org.codehaus.xfire.xmlbeans.XmlBeansType;

import groovy.lang.GroovyClassLoader;

//import org.apache.log4j.Logger;
/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: May 16, 2009
 * Time: 8:51:37 AM
 * To change this template use File | Settings | File Templates.
 */

public class WSSimpleServer {
/*

    private XFire xfire = null;
    private XFireHttpServer server = null;

    static private Logger logger = Logger.getLogger(SoapServer.class);

    */
/**
     * <p>Creates a new instance of SoapServer.</p>
     * <p/>
     *
     */
/*
    public WSSimpleServer() {
        this("localhost", 8080);
    }

    */
/**
     * <p>Creates a new instance of SoapServer.</p>
     * <p/>
     *
     * @param host the host the web container is running on.
     * @param port the port used by the web container.
     */
/*
    public WSSimpleServer(String host, int port) {
        xfire = XFireFactory.newInstance().getXFire();
        server = new XFireHttpServer();
        server.setPort(port);
    }

    */
/**
     * <p>Instantiate the web service using a Plain Old
     * Groovy Object. Both, the interface and the implementation
     * classes are generated using introspection.
     * Then the implementation is delegating the call to the
     * Groovy class.</p>
     * <p/>
     *
     * @param text the prefix of the file containing the groovy class.
     * @param URL the URL hosting the serviceon the server.
     */
/*
    public void setNode(String text, String URL) throws ClassNotFoundException {

        GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader());
        // try {
        Class clazz = gcl.loadClass(text);
        //} catch (ClassNotFoundException ex){
        //    System.err.println("Groovy file "+ text + " not found");
        //    throw ex;
        //}
        Method[] methods = clazz.getDeclaredMethods();

        StringBuffer sInterface = new StringBuffer();
        StringBuffer sImpl = new StringBuffer();

        String className = text;
        String classPackage = "";
        if (text.indexOf(".") != -1) {
            // contains is a Java 5 method
            //if (text.contains(".")) {
            className = text.substring(text.lastIndexOf(".") + 1);
            classPackage = text.substring(0, text.lastIndexOf("."));
            sInterface.append("package ").append(classPackage).append(";\n");
            sImpl.append("package ").append(classPackage).append(";\n");
        }

        sInterface.append("interface ").append(className).append("Interface {\n");

        sImpl.append("class ").append(className).append("Impl implements ").append(className).append("Interface {\n  def service = new ").append(className).append("()\n");

        boolean hasMethod = false;
        for (int i = 0; i < methods.length; ++i) {
            if ("getMetaClass".equals(methods[i].getName()) ||
                    "getProperty".equals(methods[i].getName()) ||
                    "getClass".equals(methods[i].getName()) ||
                    "setMetaClass".equals(methods[i].getName()) ||
                    "setProperty".equals(methods[i].getName()) ||
                    "invokeMethod".equals(methods[i].getName()) ||
                    "hashCode".equals(methods[i].getName()) ||
                    "equals".equals(methods[i].getName()) ||
                    "notify".equals(methods[i].getName()) ||
                    "notifyAll".equals(methods[i].getName()) ||
                    "toString".equals(methods[i].getName()) ||
                    "run".equals(methods[i].getName()) ||
                    "main".equals(methods[i].getName()) ||
                    "class\$".equals(methods[i].getName()) ||
                    "wait".equals(methods[i].getName())) {
            } else if (Modifier.isPublic(methods[i].getModifiers())) {
                hasMethod = true;

                Class returnType = methods[i].getReturnType();
                String returnTypeString;

                if (returnType.isArray()) {
                    returnType = returnType.getComponentType();
                    returnTypeString = returnType.getName().concat("[]");
                } else {
                    returnTypeString = returnType.getName();
                }

                sInterface.append("  " + returnTypeString).append(" " + methods[i].getName() + "(");

                sImpl.append("\n  " + returnTypeString).append(" " + methods[i].getName() + "(");

                Class[] params = methods[i].getParameterTypes();

                int j = 0;
                Class argType;
                String argTypeString;
                while (j < params.length) {

                    if (params[j].isArray()) {
                        argType = params[j].getComponentType();
                        argTypeString = argType.getName().concat("[]");
                    } else {
                        argTypeString = params[j].getName();
                    }

                    sInterface.append(argTypeString + " arg" + j);
                    sImpl.append(argTypeString + " arg" + j);
                    if (++j < params.length) {
                        sInterface.append(", ");
                        sImpl.append(", ");
                    }
                }
                sInterface.append(");\n");
                sImpl.append(") {\n    ");

                if (methods[i].getReturnType() != void.class) {
                    sImpl.append("return ");
                }
                sImpl.append("service." + methods[i].getName() + "(");

                j = 0;
                while (j < params.length) {
                    sImpl.append("arg" + j);
                    if (++j < params.length) {
                        sImpl.append(", ");
                    }
                }
                sImpl.append(")\n  }\n");
            }
        }
        sImpl.append("}");
        sInterface.append("}");

        if (hasMethod == false) throw new PublicMethodNotFoundException("Groovy script should have public method");

        //System.out.println(sImpl.toString());

        if (logger.isDebugEnabled()) logger.debug(sImpl.toString());
        if (logger.isDebugEnabled()) logger.debug(sInterface.toString());

        Class interfaceClass = null;
        try {
            interfaceClass = gcl.parseClass(sInterface.toString());
        } catch (Exception ex) {
            System.err.println("Cannot parse Interface class:\n" + sInterface.toString());
            ex.printStackTrace();
        }

        Class implClass = null;
        try {
            implClass = gcl.parseClass(sImpl.toString());
        } catch (Exception ex) {
            System.err.println("Cannot parse Implementation class:\n" + sImpl.toString());
            ex.printStackTrace();
        }


        ObjectServiceFactory serviceFactory = new ObjectServiceFactory(xfire.getTransportManager());
        Service service = serviceFactory.create(interfaceClass);
        service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, implClass);
        */
/*
       XmlBeansServiceFactory serviceFactory = new XmlBeansServiceFactory(xfire.getTransportManager());
       Service service = serviceFactory.create(implClass);
       service.setProperty(XmlBeansType.XMLBEANS_NAMESPACE_HACK, "true");
        */
/*

        xfire.getServiceRegistry().register(service);
    }

    public void setNode(String text) throws ClassNotFoundException {
        this.setNode(text, "");
    }

    */
/**
     * <p>Start the SoapServer.</p>
     * <p/>
     *
     */
/*
    public void start() {
        try {
            server.start();
        } catch (Exception ex) {
            System.err.println("Cannot start server");
        }

    }

    */
/**
     * <p>Stop the SoapServer.</p>
     * <p/>
     *
     */
/*
    public void stop() {
        try {
            server.stop();
        } catch (Exception ex) {
            System.err.println("Cannot stop server");
        }
    }
*/

}