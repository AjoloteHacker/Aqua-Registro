package com.aquachile.aquacheck.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Servidor Local (Desarrollo y Testing)"),
                        new Server().url("http://10.0.2.2:8080/api").description("Acceso desde Emulador Android Studio")
                ))
                .info(new Info()
                        .title("AquaCheck API - AquaChile")
                        .version("1.0.0")
                        .description("Microservicios REST para gestión de seguridad en faenas de buceo comercial, semáforo de pre-chequeo industrial, línea base fotográfica, bitácora post-inmersión y sincronización offline (DSY1105 EA4/EA5).")
                        .contact(new Contact()
                                .name("Smiling Friends - Prevención de Riesgos AquaChile")
                                .email("soporte@aquachile.com"))
                        .license(new License().name("Uso Académico Duoc UC").url("https://www.duoc.cl")));
    }
}
