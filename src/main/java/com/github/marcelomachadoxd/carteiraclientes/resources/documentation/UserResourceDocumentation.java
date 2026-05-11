package com.github.marcelomachadoxd.carteiraclientes.resources.documentation;

import com.github.marcelomachadoxd.carteiraclientes.dto.UserDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.UserInsertDTO;
import com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.StandardError;
import com.github.marcelomachadoxd.carteiraclientes.resources.exceptions.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Usuarios", description = "Gerenciamento dos corretores cadastrados no sistema")
public interface UserResourceDocumentation {

    @Operation(
        summary = "Lista todos os corretores",
        description = "Retorna uma lista paginada com todos os corretores cadastrados no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Lista de corretores retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class)))
    ResponseEntity<Page<UserDTO>> findAllPageable(Pageable pageable);

    @Operation(
        summary = "Busca corretor por ID",
        description = "Retorna os dados completos de um corretor a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "200", description = "Corretor encontrado",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "404", description = "Corretor não encontrado",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<UserDTO> findById(Long id);

    @Operation(
        summary = "Cadastra novo corretor",
        description = "Cria um novo corretor no sistema com seus dados de acesso e perfil."
    )
    @RequestBody(
        description = "Dados do corretor a ser cadastrado",
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserInsertDTO.class),
            examples = @ExampleObject(
                name = "Exemplo de corretor",
                value = "{\"nome\": \"João Corretor\", \"email\": \"joao.corretor@imobiliaria.com.br\", \"acessoId\": 1, \"password\": \"senha123\"}"
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Corretor cadastrado com sucesso",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "422", description = "Erro de validação nos campos do corretor",
        content = @Content(schema = @Schema(implementation = ValidationError.class)))
    ResponseEntity<UserDTO> insert(UserInsertDTO userInsertDTO);

    @Operation(
        summary = "Remove corretor",
        description = "Remove permanentemente um corretor do sistema a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "204", description = "Corretor removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Corretor não encontrado",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<Void> delete(Long id);
}
