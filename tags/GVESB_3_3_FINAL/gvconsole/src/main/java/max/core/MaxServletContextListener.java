package max.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * E' necessario un application server che supporti web-app_2_3.dtd.
 */
public class MaxServletContextListener implements ServletContextListener
{
    public void contextInitialized(ServletContextEvent evt)
    {
        ServletContext context = evt.getServletContext();
        int maj = context.getMajorVersion();
        int min = context.getMinorVersion();
        String server = context.getServerInfo();
        String cntxName = context.getServletContextName();

        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| MAXIME INFORMATICA INTRANET FRAMEWORK V1.0 on context " + cntxName);
        System.out.println("| SERVER.............: " + server);
        System.out.println("| SERVLET API VERSION: " + maj + "." + min);
        System.out.println("+--------------------------------------------------------------------------");
    }

    public void contextDestroyed(ServletContextEvent evt)
    {
        System.out.println("+--------------------------------------------------------------------------");
        System.out.println("| SHUTDOWN MAXIME INFORMATICA INTRANET FRAMEWORK V1.0 on context "
                + evt.getServletContext().getServletContextName());
        System.out.println("+--------------------------------------------------------------------------");
    }
}
