package com.example.springws;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.Collections;
import java.util.List;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet dispatcherServlet = new MessageDispatcherServlet();
        dispatcherServlet.setApplicationContext(context);
        dispatcherServlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(dispatcherServlet, "/ws/*");
    }

    @Bean
    public XsdSchema countrySchema() {
        return new SimpleXsdSchema(new ClassPathResource("countries.xsd"));
    }

    @Bean(name = "country")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countrySchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setPortTypeName("CountryServicePort");
        wsdl11Definition.setTargetNamespace("http://spring.io/guides/gs-producing-web-service");
        wsdl11Definition.setSchema(countrySchema);
        return wsdl11Definition;
    }

    @Bean
    public PayloadLoggingInterceptor loggingInterceptor(){
        return new PayloadLoggingInterceptor();
    }

    @Bean
    public PayloadValidatingInterceptor validatingInterceptor(){
        PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("countries.xsd"));
        return interceptor;
    }

    @Bean
    public SimplePasswordValidationCallbackHandler callbackHandler(){
        SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
        callbackHandler.setUsersMap(Collections.singletonMap("admin", "pwd123"));
        return callbackHandler;
    }

    @Bean
    public XwsSecurityInterceptor securityInterceptor(){
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor();
        interceptor.setCallbackHandler(callbackHandler());
        interceptor.setPolicyConfiguration(new ClassPathResource("securityPolicy.xml"));
        return interceptor;
    }

    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(loggingInterceptor());
        interceptors.add(securityInterceptor());
    }
}
