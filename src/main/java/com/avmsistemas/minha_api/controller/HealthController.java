package com.avmsistemas.minha_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController // Indica que esta classe é um controlador REST
@Tag(name = "Health", description = "Valida se o serviço está a funcionar")
public class HealthController {

    // Define um endpoint GET em /hello
    @GetMapping("/health")
    public Boolean health() {
        return true;
    }
}
