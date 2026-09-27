package com.example.todolist;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@AllArgsConstructor
public class Config {

    private Environment environment;

    @Bean
    public OpenAPI defineOpenAPI () {
        Server server = new Server();
        String serverUrl = environment.getProperty("api.server.url");
        server.setUrl(serverUrl);
        server.setDescription("Development");

        Contact myContact = new Contact();
        myContact.setName("Имя Фамилия");
        myContact.setEmail("my.email@example.com");

        Info info = new Info()
                .title("Системное API для управления задачами")
                .version("1.0")
                .description("Это API предоставляет эндпоинты для управления задачами.")
                .contact(myContact);
        return new OpenAPI().info(info).servers(List.of(server));
    }
}