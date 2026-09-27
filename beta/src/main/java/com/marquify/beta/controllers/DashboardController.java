package com.marquify.beta.controllers;

import com.marquify.beta.request.DashboardFiltro;
import com.marquify.beta.response.DashboardConsultaResponse.*;
import com.marquify.beta.service.DashboardConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/me/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardConsultaService service;

    @GetMapping("/agendamentos")
    public Pagina agendamentos(@ModelAttribute DashboardFiltro filtro) { return service.agendamentos(filtro); }

    @GetMapping("/indicadores")
    public Indicadores indicadores(@ModelAttribute DashboardFiltro filtro) { return service.indicadores(filtro); }
}
