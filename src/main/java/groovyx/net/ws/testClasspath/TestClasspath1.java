

/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Jan 12, 2008
 * Time: 4:47:45 PM
 * To change this template use File | Settings | File Templates.
 */
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.io.File;

class TestClasspath1 {
    static void setupClasspath(Path classPath, ClassLoader classLoader) throws Exception {
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        ClassLoader tcl = classLoader;
        do {
            System.out.println("Entering do ...");
            if (tcl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader)tcl).getURLs();
                for (URL url:urls) {
                    System.out.println("> " + url);
                    if (url.getProtocol().startsWith("file")) {
                        try {
                            File file = new File(url.toURI().getPath());
                            if (file.isDirectory()) {
                                DirSet ds = new DirSet();
                                ds.setFile(file);
                                classPath.addDirset(ds);
                            } else {
                                FileSet fs = new FileSet();
                                fs.setFile(file);
                                classPath.addFileset(fs);
                            }
                        } catch (URISyntaxException e) {
                            throw new Exception(e);
                        }
                    }
                }
            }
            tcl = tcl.getParent();
            if (null == tcl) {
                break;
            }
        }while(!tcl.equals(scl));
    }
}
