package com.github.marcelomachadoxd.carteiraclientes.resources.documentation;

import com.github.marcelomachadoxd.carteiraclientes.dto.VisitaDTO;
import com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.StandardError;
import com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Visitas", description = "Registro e consulta de visitas a imóveis realizadas pelos corretores")
public interface VisitaResourceDocumentation {

    @Operation(
        summary = "Busca visita por ID",
        description = "Retorna os dados completos de uma visita a imóvel a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "200", description = "Visita encontrada",
        content = @Content(schema = @Schema(implementation = VisitaDTO.class)))
    @ApiResponse(responseCode = "404", description = "Visita não encontrada",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<VisitaDTO> findById(Long id);

    @Operation(
        summary = "Lista visitas por corretor",
        description = "Retorna uma lista paginada de visitas realizadas pelo corretor responsável informado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de visitas retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class)))
    ResponseEntity<Page<VisitaDTO>> findByResponsavelId(Long id, Pageable pageable);

    @Operation(
        summary = "Lista visitas por cliente",
        description = "Retorna uma lista paginada de visitas realizadas para o cliente informado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de visitas retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class)))
    ResponseEntity<Page<VisitaDTO>> findByClienteId(Long id, Pageable pageable);

    @Operation(
        summary = "Filtra visitas por cliente e corretor",
        description = "Retorna uma lista paginada de visitas filtradas simultaneamente por cliente e corretor responsável (AND lógico). Ambos os filtros são aplicados ao mesmo tempo."
    )
    @ApiResponse(responseCode = "200", description = "Lista de visitas filtrada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class)))
    ResponseEntity<Page<VisitaDTO>> findByClienteId(
        @Parameter(
            name = "cliId",
            description = "ID do cliente para filtrar as visitas. Aplicado simultaneamente com respId (AND lógico).",
            required = false,
            example = "1"
        ) Long cliId,
        @Parameter(
            name = "respId",
            description = "ID do corretor responsável para filtrar as visitas. Aplicado simultaneamente com cliId (AND lógico).",
            required = false,
            example = "1"
        ) Long respId,
        Pageable pageable
    );

    @Operation(
        summary = "Registra nova visita",
        description = "Registra uma nova visita a imóvel realizada por um corretor para um cliente da carteira."
    )
    @RequestBody(
        description = "Dados da visita a ser registrada",
        required = true,
        content = @Content(
            schema = @Schema(implementation = VisitaDTO.class),
            examples = @ExampleObject(
                name = "Exemplo de visita",
                value = "{\"dataVisita\": \"2025-03-15T14:30:00Z\", \"obs\": \"Cliente gostou do imóvel, aguardando proposta\", \"satisfacao\": true, \"responsavel\": {\"id\": 2}, \"cliente\": {\"id\": 1}}"
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Visita registrada com sucesso",
        content = @Content(schema = @Schema(implementation = VisitaDTO.class)))
    @ApiResponse(responseCode = "422", description = "Erro de validação nos campos da visita",
        content = @Content(schema = @Schema(implementation = ValidationError.class)))
    ResponseEntity<VisitaDTO> insert(VisitaDTO visitaDTO);

    @Operation(
        summary = "Remove visita",
        description = "Remove permanentemente o registro de uma visita a imóvel a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "204", description = "Visita removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Visita não encontrada",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<Void> delete(Long id);
}
