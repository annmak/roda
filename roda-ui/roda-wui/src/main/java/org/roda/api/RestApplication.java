/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

public class RestApplication extends ResourceConfig {
  public RestApplication() {
    super();
    packages("io.swagger.jaxrs.listing", "org.roda.api");
    register(JacksonFeature.class);
    register(MoxyXmlFeature.class);
    register(MultiPartFeature.class);

    // https://github.com/swagger-api/swagger-core/wiki/Swagger-Core-Jersey-2.X-Project-Setup-1.5
    register(ApiListingResource.class);
    register(SwaggerSerializers.class);
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1");
    beanConfig.setBasePath("/api");
    beanConfig.setResourcePackage("org.roda.api");
    beanConfig.setScan(true);
  }
}
