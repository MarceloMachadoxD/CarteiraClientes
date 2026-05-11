package com.github.marcelomachadoxd.carteiraclientes.resources.documentation;

import com.github.marcelomachadoxd.carteiraclientes.dto.RoleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Roles", description = "Gerenciamento dos perfis de acesso dos corretores")
public interface RoleResourceDocumentation {

    @Operation(
        summary = "Lista todos os perfis de acesso",
        description = "Retorna a lista completa de perfis de acesso cadastrados no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista de perfis retornada com sucesso",
        content = @Content(schema = @Schema(implementation = RoleDTO.class)))
    ResponseEntity<List<RoleDTO>> findAll();

    @Operation(
        summary = "Cadastra novo perfil de acesso",
        description = "Cria um novo perfil de acesso que pode ser atribuído aos corretores cadastrados no sistema."
    )
    @RequestBody(
        description = "Dados do perfil de acesso a ser cadastrado",
        required = true,
        content = @Content(
            schema = @Schema(implementation = RoleDTO.class),
            examples = @ExampleObject(
                name = "Exemplo de perfil",
                value = "{\"nome\": \"ROLE_CORRETOR\"}"
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "Perfil de acesso cadastrado com sucesso",
        content = @Content(schema = @Schema(implementation = RoleDTO.class)))
    ResponseEntity<RoleDTO> insert(RoleDTO roleDTO);
}
