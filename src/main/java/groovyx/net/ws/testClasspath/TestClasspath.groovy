
/**
 * Created by IntelliJ IDEA.
 * User: alleon
 * Date: Jan 12, 2008
 * Time: 4:50:10 PM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

def tmpdir = System.getProperty("java.io.tmpdir")
def project = new Project();
project.setBaseDir(new File(tmpdir));
def cp = new Path(project)
TestClasspath1.setupClasspath(cp, this.class.classLoader)

println cp