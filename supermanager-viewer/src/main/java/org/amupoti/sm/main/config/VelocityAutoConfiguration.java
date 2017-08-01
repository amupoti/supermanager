package org.amupoti.sm.main.config;

import org.apache.velocity.app.event.implement.IncludeRelativePath;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.velocity.VelocityConfig;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityToolboxView;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

import javax.servlet.Servlet;
import java.util.Properties;

/**
 * Minimal Spring-Boot Auto-Configuration for the Velocity Template Engine
 *
 * @author Christian Gebauer
 */
@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class VelocityAutoConfiguration {

  @Configuration
  @ConditionalOnClass({ Servlet.class })
  public static class DefaultTemplateResolverConfiguration {

    @Autowired
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Bean
    public VelocityConfig velocityConfig() {
      VelocityConfigurer cfg = new VelocityConfigurer();
      cfg.setResourceLoader(resourceLoader);
      Properties props = new Properties();
      props.setProperty( "input.encoding", "UTF-8" );
      props.setProperty( "output.encoding", "UTF-8" );
      props.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, IncludeRelativePath.class.getName());
      cfg.setVelocityProperties(props);
      return cfg;
    }

    @Bean
    public ViewResolver viewResolver() {
      VelocityViewResolver resolver = new VelocityViewResolver();
      resolver.setViewClass(VelocityToolboxView.class);
      resolver.setPrefix("/templates/");
      resolver.setSuffix(".vm");
      resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 20);
      return resolver;
    }
  }
}