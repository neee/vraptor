package br.com.caelum.vraptor.scan;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Abstract classpath resolver that provides common methods to all {@link ClasspathResolver} implementations.
 * @author Otávio Scherer Garcia
 * @since 3.5.0-SNAPSHOT
 */
abstract class AbstractClasspathResolver implements ClasspathResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(ReflectionsComponentScanner.class);

	/**
	 * Append the list of packages that vraptor should scan to find custom components.
	 * @param result pre existing list of packages
	 */
	protected void getPackagesFromPluginsJARs(List<String> result) {
		try {
			Enumeration<URL> urls = getClassLoader().getResources("META-INF/br.com.caelum.vraptor.packages");

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				List<String> lines = Resources.readLines(url, Charsets.UTF_8);
				if (lines.isEmpty()) {
					logger.warn("Plugin packages file was empty: {}", url.getPath());
				} else {
					result.addAll(lines);
				}
			}
		} catch (IOException e) {
			logger.error("Exception while searching for packages file inside JARs", e);
		}
	}
}
