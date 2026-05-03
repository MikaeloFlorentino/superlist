package com.superlist.config;

import com.superlist.model.*;
import com.superlist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataLoader implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final FamiliaRepository familiaRepository;
    private final MiembroFamiliaRepository miembroRepository;
    private final AreaSuperRepository areaSuperRepository;
    private final AreaCasaRepository areaCasaRepository;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("[SeedDataLoader] No se requieren datos iniciales. Las áreas por defecto se crean al crear una familia.");
    }
}
