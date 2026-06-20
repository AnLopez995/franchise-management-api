package com.franchise.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchisesOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Franchises API")
                .version("v1")
                .description("API para gestionar franquicias, sus sucursales y los productos de cada sucursal."));
    }
}
