/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package net.stratplus.backdemo;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import net.stratplus.model.User;


@SpringBootApplication
@ImportResource({ "classpath:spring/camel-context.xml" })
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ServletRegistrationBean servletRegistrationBean() {
		ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
				"/camel-rest-sql/*");
		servlet.setName("CamelServlet");
		return servlet;
	}

	@Component
	class RestApi extends RouteBuilder {

		@Override
		public void configure() {
			restConfiguration().contextPath("/camel-rest-sql").apiContextPath("/api-doc")
					.apiProperty("api.title", "Camel REST API").apiProperty("api.version", "1.0")
					.apiProperty("cors", "true").apiContextRouteId("doc-api").component("servlet")
					.bindingMode(RestBindingMode.json);

			rest("/login").description("Login REST service").post("/findUser")
			.description("Log The User").type(User.class).route().id("find")
			.to("direct:findUser");


			rest("/register").description("Register REST service").post("/newUser")
			.description("REgister The User").type(User.class).route().id("register")
			.to("direct:registerUser");

			
		}
	}

	@Component
	class Backend extends RouteBuilder {

		@SuppressWarnings("deprecation")
		@Override
		public void configure() throws Exception {
			from("direct:findUser").log(LoggingLevel.INFO, "Solicitud de Logeo ").beanRef("loginRegister", "findByUser");

			from("direct:registerUser").log(LoggingLevel.INFO, "Solicitud de Registro").beanRef("loginRegister", "registerUser");
		
		}
	}
}



