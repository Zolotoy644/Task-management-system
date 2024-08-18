package com.zolotarev.tms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info=@Info(
                contact = @Contact(
                        name = "Oleg Z/",
                        email = "Zolotoy644@yandex.ru"
                ),
                description = "Documentation for Task-Management-System",
                title = "Task-management-system",
                version = "0.0.1"
        )
)
@SecurityScheme(
        name = "JWT Bearer",
        description = "JWT token in Header",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
