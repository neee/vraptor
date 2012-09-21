package br.com.caelum.vraptor.scan;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import br.com.caelum.vraptor.core.BaseComponents;
import br.com.caelum.vraptor.ioc.Stereotype;

import com.google.common.collect.Sets;

/**
 * An implementation for {@link ComponentScanner} using Reflections framework.
 * 
 * @author Otávio Scherer Garcia
 * @author Sérgio Lopes
 */
public class ReflectionsComponentScanner implements ComponentScanner {
	
	public ReflectionsComponentScanner() {
	}

	public Collection<String> scan(ClasspathResolver resolver) {
    	Reflections webInfClassesIndex = createReflectionsForWebinf(resolver);
    	Reflections basePackagesIndex = createReflectionsForBasePackages(resolver);

    	HashSet<String> results = Sets.newHashSet();
    	HashSet<Class<?>> stereotypes = Sets.newHashSet();

    	//add vraptor stereotypes
    	Collections.addAll(stereotypes, BaseComponents.getStereotypes());

    	//add other stereotypes
    	stereotypes.addAll(webInfClassesIndex.getTypesAnnotatedWith(Stereotype.class));
    	stereotypes.addAll(basePackagesIndex.getTypesAnnotatedWith(Stereotype.class));

    	//add components
    	for(Class<?> stereotype: stereotypes) {
        	for(Class<?> clazz : webInfClassesIndex.getTypesAnnotatedWith((Class<? extends Annotation>) stereotype)) {
        		results.add(clazz.getName());
        	}
    	}
    	
    	//add components
    	for(Class<?> stereotype: stereotypes) {
        	for(Class<?> clazz : basePackagesIndex.getTypesAnnotatedWith((Class<? extends Annotation>) stereotype)) {
        		results.add(clazz.getName());
        	}
    	}
    	
		return results;
	}

	private Reflections createReflectionsForBasePackages(ClasspathResolver resolver) {
		FilterBuilder packageFilters = new FilterBuilder();
		for (String p : resolver.findBasePackages()) {
    		packageFilters.include(FilterBuilder.prefix(p));
    	}
	
    	return new Reflections(new ConfigurationBuilder()
    		.filterInputsBy(packageFilters)
            .setUrls(resolver.findWebInfLibLocations())
            .setScanners(new TypeAnnotationsScanner()));
	}

	private Reflections createReflectionsForWebinf(ClasspathResolver resolver) {
		return new Reflections(new ConfigurationBuilder()
            .setUrls(resolver.findWebInfClassesLocation())
            .setScanners(new TypeAnnotationsScanner()));
	}
}
