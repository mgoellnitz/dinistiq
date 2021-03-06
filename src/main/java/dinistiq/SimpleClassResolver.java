/**
 *
 * Copyright 2013-2019 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package dinistiq;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Resolve classes or classnames from a given set of packages.
 *
 * One design goal in finding the classes was to hit the JAR files just once but apart from that this is a
 * far too simple implementation.
 *
 */
public class SimpleClassResolver implements ClassResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleClassResolver.class);

    private final static Set<String> CLASSES_TO_IGNORE = new HashSet<>();

    private final Set<String> packageNames;

    private final Set<String> properties;

    private final Set<String> classNames;


    /**
     * These classes must be ignored to be able to use Dinistiq without web integration
     * and corresponding useless error entries in the log
     */
    static {
        CLASSES_TO_IGNORE.add("dinistiq.web.DinistiqContextLoaderListener");
        CLASSES_TO_IGNORE.add("dinistiq.web.RegisterableServlet");
    }


    /**
     * Adds all relevant JAR/.class URLs for a given package to an already present set of URLs.
     *
     * @param urls set of URLs to add the newly resolved ones to.
     * @param packageName name of the package
     */
    private void addUrlsForPackage(Set<URL> urls, String packageName) {
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (urlEnumeration.hasMoreElements()) {
                String url = urlEnumeration.nextElement().toString();
                int idx = url.indexOf('!');
                url = idx>0 ? url.substring(0, idx) : url;
                url = url.startsWith("jar:") ? url.substring(4) : url;
                url = url.startsWith("vfs:/") ? "file"+url.substring(3, url.length()-packagePath.length()-2) : url;
                url = url.endsWith(".jar") ? url : url.substring(0, url.length()-packagePath.length());
                LOG.info("addUrlsForPackage() resulting URL {}", url);
                urls.add(new URL(url));
            } // while
        } catch (IOException e) {
            LOG.error("addUrlsForPackage()", e);
        } // try/catch
    } // addUrlsForPackage()


    /**
     * Checks a file name if it needs to be considered for properties files or derive a class name from.
     *
     * @param name name of a file to be scanned
     */
    protected final void checkClassAndAdd(String name) {
        LOG.debug("checkClassAndAdd() name={}", name);
        if (name.endsWith(".class")&&(name.indexOf('$')<0)) {
            String n = name.replace(File.separatorChar, '/').replace('/', '.');
            String className = n.substring(0, n.length()-6);
            LOG.debug("checkClassAndAdd() class name {}", className);
            boolean add = false;
            if (!CLASSES_TO_IGNORE.contains(className)) {
                for (String packageName : packageNames) {
                    add = add||className.startsWith(packageName);
                } // if
            }
            if (add) {
                LOG.debug("checkClassAndAdd(): {}", className);
                classNames.add(className);
            } // if
        } // if
        if (name.endsWith(".properties")) {
            LOG.info("checkClassAndAdd() properties {}", name);
            properties.add(name.replace(File.separatorChar, '/'));
        } // if
    } // checkClassAndAdd()


    /**
     * Recurses a given directory to scan for properties and class files.
     *
     * @param dir base directory to scann recursively
     * @param basePathLength length of the basePath string
     */
    protected final void recurseSubDir(File dir, int basePathLength) {
        LOG.debug("recurseSubDir() scanning {}", dir.getAbsolutePath());
        for (File f : (dir.isDirectory() ? dir.listFiles() : new File[0])) {
            String fileName = f.getAbsolutePath().substring(basePathLength);
            LOG.debug("recurseSubDir() fileName={}", fileName);
            if (fileName.endsWith(".class")||fileName.endsWith(".properties")) {
                checkClassAndAdd(fileName);
            } else {
                recurseSubDir(f, basePathLength);
            } // if
        } // for
    } // recurseSubDir()


    /**
     * Initialize class resolver with a given set of package names to scan.
     *
     * @param packageNames Set of string names for pakckges to scan
     */
    public SimpleClassResolver(Set<String> packageNames) {
        this.packageNames = new HashSet<>(packageNames);
        // to have the properties files in the path which we intend to use for configuration
        this.packageNames.add(this.getClass().getPackage().getName());

        properties = new HashSet<>();
        classNames = new HashSet<>();
        Set<URL> urls = new HashSet<>();
        for (String packageName : this.packageNames) {
            addUrlsForPackage(urls, packageName);
        } // if
        LOG.debug("() url # {}", urls.size());
        for (URL u : urls) {
            try {
                String path = URLDecoder.decode(u.getPath(), "UTF-8");
                LOG.info("(): path {}", path);
                if (path.endsWith(".jar")) {
                    LOG.info("(): scanning jar {}", path);
                    try ( JarInputStream is = new JarInputStream(u.openStream())) {
                        for (JarEntry entry = is.getNextJarEntry(); entry!=null; entry = is.getNextJarEntry()) {
                            LOG.info("(): entry {}", entry.getName());
                            checkClassAndAdd(entry.getName());
                        } // for
                    }
                } else {
                    File dir = new File(path);
                    int basePathLength = dir.getAbsolutePath().length()+1;
                    recurseSubDir(dir, basePathLength);
                } // if
            } catch (IOException e) {
                LOG.error("()", e);
            } // try/catch
        } // for
    } // SimpleClassResolver()


    /**
     * Helper method to keep areas with suppressed warnings small.
     *
     * @param <T>
     * @param className
     * @return simply returns Class.forName(classname)
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private <T extends Object> Class<T> loadClass(String className) throws ClassNotFoundException {
        return (Class<T>) Class.forName(className);
    } // loadClass()


    /**
     * Helper method for all API methods which have to scan all detected classes.
     *
     * @return collection of classes for the collected class names.
     */
    private <T extends Object> Collection<Class<T>> getClasses() {
        Set<Class<T>> result = new HashSet<>();
        for (String className : classNames) {
            try {
                LOG.debug("getClasses() className={}", className);
                Class<T> cls = loadClass(className);
                result.add(cls);
            } catch (ClassNotFoundException|Error e) {
                LOG.error("getClasses() file format error for class name "+className, e);
            } // try/catch
        } // for
        return result;
    } // getClasses()


    /**
     * Get classes from underlying packages satisfying the given superclass which are no interfaces and not abstract.
     *
     * @see ClassResolver#getSubclasses(java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getSubclasses(Class<T> c) {
        Set<Class<T>> result = new HashSet<>();
        LOG.debug("getSubclasses() checking {} classes", classNames.size());
        Collection<Class<T>> classes = getClasses();
        for (Class<T> cls : classes) {
            LOG.debug("getSubclasses() className={}", cls.getName());
            if ((!cls.isInterface())&&c.isAssignableFrom(cls)&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                result.add(cls);
            } // if
        } // for
        return result;
    } // getSubclasses()


    /**
     * Get classes from underlying packages satisfying the given annotation which are no interfaces and not abstract.
     *
     * @see ClassResolver#getAnnotated(java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getAnnotated(Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<>();
        LOG.debug("getAnnotated() checking {} classes", classNames.size());
        Collection<Class<T>> classes = getClasses();
        for (Class<T> cls : classes) {
            LOG.debug("getAnnotated() className={}", cls.getName());
            if ((!cls.isInterface())&&(cls.getAnnotation(annotation)!=null)&&((cls.getModifiers()&Modifier.ABSTRACT)==0)) {
                result.add(cls);
            } // if
        } // if
        return result;
    } // getAnnotated()


    /**
     * Get classes from underlying packages satisfying the given annotation.
     *
     * @see ClassResolver#getAnnotatedItems(java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getAnnotatedItems(Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<>();
        LOG.debug("getAnnotatedItems() checking {} classes", classNames.size());
        Collection<Class<T>> classes = getClasses();
        for (Class<T> cls : classes) {
            LOG.debug("getAnnotatedItems() className={}", cls.getName());
            if (cls.getAnnotation(annotation)!=null) {
                result.add(cls);
            } // if
        } // if
        return result;
    } // getAnnotated()


    /**
     * Get classes from underlying packages satisfying the given annotation and superclass which are no interfaces.
     *
     * @see ClassResolver#getAnnotatedSubclasses(java.lang.Class, java.lang.Class)
     */
    @Override
    public <T extends Object> Set<Class<T>> getAnnotatedSubclasses(Class<T> c, Class<? extends Annotation> annotation) {
        Set<Class<T>> result = new HashSet<>();
        LOG.debug("getAnnotatedSubclasses() checking {} classes", classNames.size());
        Collection<Class<T>> classes = getClasses();
        for (Class<T> cls : classes) {
            LOG.debug("getAnnotatedSubclasses() className={}", cls.getName());
            if ((cls.getAnnotation(annotation)!=null)&&c.isAssignableFrom(cls)&&(!cls.isInterface())) {
                result.add(cls);
            } // if
        } // if
        return result;
    } // getAnnotatedSubclasses()


    /**
     * @see ClassResolver#getProperties(java.lang.String)
     */
    @Override
    public SortedSet<String> getProperties(String path) {
        SortedSet<String> result = new TreeSet<>();
        for (String property : properties) {
            LOG.info("getProperties({}) checking {}", path, property);
            if (property.startsWith(path)) {
                result.add(property);
            } // if
        } // for
        return result;
    } // getProperties()

} // SimpleClassResolver
