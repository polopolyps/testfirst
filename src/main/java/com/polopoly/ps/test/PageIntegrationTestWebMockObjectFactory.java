package com.polopoly.ps.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;

import com.mockrunner.mock.web.MockRequestDispatcher;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;

public class PageIntegrationTestWebMockObjectFactory extends WebMockObjectFactory {
    private static final Logger LOGGER = Logger.getLogger(PageIntegrationTestWebMockObjectFactory.class.getName());
    private File webappDir;

    public PageIntegrationTestWebMockObjectFactory(File webappDir) {
        this.webappDir = webappDir;
    }

    private class ConfigurableMockServletContext extends MockServletContext {
        RequestDispatcher dispatcher = null;

        @Override
        public synchronized void setRequestDispatcher(String path, RequestDispatcher dispatcher) {
            super.setRequestDispatcher(path, dispatcher);
            this.dispatcher = dispatcher;
        }

        @Override
        public synchronized RequestDispatcher getRequestDispatcher(String path) {
            if (dispatcher != null && dispatcher instanceof MockRequestDispatcher) {
                ((MockRequestDispatcher) dispatcher).setPath(path);
            }
            return dispatcher == null ? super.getRequestDispatcher(path) : dispatcher;
        }

        @Override
        public synchronized InputStream getResourceAsStream(String path) {
            File file = new File(webappDir, path);

            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "Requested " + path + " which did not exist in "
                        + webappDir.getAbsolutePath() + ".");

                return null;
            }
        }
        
        @Override
        public synchronized URL getResource(String path) throws MalformedURLException {
            return new URL("file://" + webappDir.getAbsolutePath() + path);
        }
    }

    @Override
    public MockServletContext createMockServletContext() {
        return new ConfigurableMockServletContext();
    }
}
