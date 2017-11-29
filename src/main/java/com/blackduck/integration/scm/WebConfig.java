package com.blackduck.integration.scm;

import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		super.addViewControllers(registry);
		registry.addViewController("/").setViewName("index");
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);

		// Always inject product version.
		registry.addWebRequestInterceptor(new WebRequestInterceptor() {

			@Override
			public void preHandle(WebRequest request) throws Exception {
			}

			@Override
			public void postHandle(WebRequest request, ModelMap model) throws Exception {
				if (model != null) {
					model.addAttribute("productVersion", WebConfig.class.getPackage().getImplementationVersion());
				}
			}

			@Override
			public void afterCompletion(WebRequest request, Exception ex) throws Exception {
			}
		});
	}
}
