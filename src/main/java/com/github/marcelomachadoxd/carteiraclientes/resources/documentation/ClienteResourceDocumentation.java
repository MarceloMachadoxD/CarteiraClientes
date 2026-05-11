package com.github.marcelomachadoxd.carteiraclientes.resources.documentation;

import com.github.marcelomachadoxd.carteiraclientes.dto.ClienteDTO;
import com.github.marcelomachadoxd.carteiraclientes.dto.PageResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Clientes", description = "Gerenciamento da carteira de clientes dos corretores de imóveis")
public interface ClienteResourceDocumentation {

    @Operation(
        summary = "Busca cliente por ID",
        description = "Retorna os dados completos de um cliente a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "200", description = "Cliente encontrado",
        content = @Content(schema = @Schema(implementation = ClienteDTO.class)))
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<ClienteDTO> findById(Long id);

    @Operation(
        summary = "Busca clientes por nome",
        description = "Retorna uma lista paginada de clientes cujo nome começa com o prefixo informado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso",
        content = @Content(schema = @Schema(implementation = PageResponse.class)))
    ResponseEntity<PageResponse<ClienteDTO>> findByNome(String nome, Pageable pageable);

    @Operation(
        summary = "Filtra clientes por perfil de interesse",
        description = "Retorna uma lista paginada de clientes cujo perfil de interesse em imóveis corresponde aos critérios informados, aplicando uma margem de tolerância percentual sobre valorMaximo e metragem."
    )
    @ApiResponse(responseCode = "200", description = "Lista de clientes filtrada com sucesso",
        content = @Content(schema = @Schema(implementation = PageResponse.class)))
    ResponseEntity<PageResponse<ClienteDTO>> findByInteresses(
        @Parameter(
            name = "margem",
            description = "Percentual de margem de tolerância aplicado sobre valorMaximo e metragem. O valor 0 desativa o filtro para esse parâmetro.",
            example = "5"
        ) Integer margem,
        @Parameter(
            name = "qtdQuartos",
            description = "Número mínimo de quartos desejados. O valor 0 desativa o filtro para esse parâmetro.",
            example = "2"
        ) Integer qtdQuartos,
        @Parameter(
            name = "qtdBanheiros",
            description = "Número mínimo de banheiros desejados. O valor 0 desativa o filtro para esse parâmetro.",
            example = "1"
        ) Integer qtdBanheiros,
        @Parameter(
            name = "qtdVagas",
            description = "Número mínimo de vagas de garagem desejadas. O valor 0 desativa o filtro para esse parâmetro.",
            example = "1"
        ) Integer qtdVagas,
        @Parameter(
            name = "metragem",
            description = "Área máxima aceitável em m². A margem percentual é aplicada sobre este valor. O valor 0 desativa o filtro para esse parâmetro.",
            example = "65"
        ) Integer metragem,
        @Parameter(
            name = "valorMaximo",
            description = "Orçamento máximo em reais. A margem percentual é aplicada sobre este valor. O valor 0 desativa o filtro para esse parâmetro.",
            example = "350000"
        ) Integer valorMaximo,
        Pageable pageable
    );

    @Operation(
        summary = "Cadastra novo cliente",
        description = "Cria um novo cliente na carteira com seus dados pessoais e perfil de interesse em imóveis."
    )
    @RequestBody(
        description = "Dados do cliente a ser cadastrado",
        required = true,
        content = @Content(
            schema = @Schema(implementation = ClienteDTO.class),
            examples = @ExampleObject(
                name = "Exemplo de cliente",
                value = "{\"nome\": \"Maria Silva\", \"email\": \"maria.silva@email.com\", \"qtdQuartos\": 2, \"qtdBanheiros\": 1, \"qtdVagas\": 1, \"metragem\": 65, \"valorMaximo\": 350000, \"obs\": \"Prefere apartamento em andar alto, aceita condomínio até R$ 800\"}"
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso",
        content = @Content(schema = @Schema(implementation = ClienteDTO.class)))
    @ApiResponse(responseCode = "422", description = "Erro de validação nos campos do cliente",
        content = @Content(schema = @Schema(implementation = ValidationError.class)))
    ResponseEntity<ClienteDTO> insert(ClienteDTO clienteDTO);

    @Operation(
        summary = "Remove cliente",
        description = "Remove permanentemente um cliente da carteira a partir do seu identificador único."
    )
    @ApiResponse(responseCode = "204", description = "Cliente removido com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<Void> delete(Long id);

    @Operation(
        summary = "Atualiza dados do cliente",
        description = "Atualiza os dados pessoais e o perfil de interesse de um cliente existente."
    )
    @RequestBody(
        description = "Dados atualizados do cliente",
        required = true,
        content = @Content(
            schema = @Schema(implementation = ClienteDTO.class),
            examples = @ExampleObject(
                name = "Exemplo de atualização",
                value = "{\"nome\": \"Maria Silva\", \"email\": \"maria.silva@email.com\", \"qtdQuartos\": 3, \"qtdBanheiros\": 2, \"qtdVagas\": 1, \"metragem\": 80, \"valorMaximo\": 450000, \"obs\": \"Prefere apartamento em andar alto, aceita condomínio até R$ 1000\"}"
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
    @ApiResponse(responseCode = "422", description = "Erro de validação nos campos do cliente",
        content = @Content(schema = @Schema(implementation = ValidationError.class)))
    ResponseEntity<Void> update(Long id, ClienteDTO clienteDTO);
}
