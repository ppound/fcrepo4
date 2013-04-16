package org.fcrepo.integration.util;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class ContainerWrapper {
	private static final Logger logger = LoggerFactory.getLogger(ContainerWrapper.class);

	private List<String> resources;
	private int port;
	private ConfigurableApplicationContext context;
	private SelectorThread server;

	public void setResources(List<String> resources) {
		this.resources = resources;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void start() throws Exception {
		URI uri = URI.create("http://localhost:" + port);
		ServletAdapter adapter = new ServletAdapter();
		adapter.addInitParameter("com.sun.jersey.config.property.packages", "org.fcrepo.api.legacy");
		adapter.addInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
		adapter.addContextParameter("contextConfigLocation", "classpath:spring-test/master.xml");
		adapter.addServletListener("org.springframework.web.context.ContextLoaderListener");
		adapter.setServletInstance(new SpringServlet());
		adapter.setContextPath(uri.getPath());
		adapter.setProperty("load-on-startup", 1);
		this.server = GrizzlyServerFactory.create(uri, adapter);
	}

	public void stop() throws Exception {
		server.stopEndpoint();
	}

}
