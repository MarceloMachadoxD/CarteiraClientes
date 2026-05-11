package com.github.marcelomachadoxd.carteiraclientes.dto;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link PageResponse}.
 *
 * Grupo 1: construtor com Page<T> mockado via Mockito — verifica os 7 campos.
 * Grupo 2: página vazia via PageImpl real — verifica content vazio e metadados.
 * Grupo 3: página intermediária via PageImpl real — verifica first=false, last=false.
 *
 * Requirements: 2.3, 3.3, 3.4, 3.5, 3.8
 */
class PageResponseTest {

    // =========================================================================
    // Grupo 1 — Construtor com Page<T> mockado (Mockito)
    // =========================================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ComPageMockado {

        @Mock
        private Page<String> mockedPage;

        /**
         * Teste 1: construtor com Page<T> mockado — todos os 7 campos mapeados corretamente.
         */
        @Test
        void constructor_withMockedPage_shouldMapAllSevenFields() {
            // Arrange
            List<String> content = List.of("item1", "item2", "item3");
            when(mockedPage.getContent()).thenReturn(content);
            when(mockedPage.getTotalElements()).thenReturn(100L);
            when(mockedPage.getTotalPages()).thenReturn(10);
            when(mockedPage.getNumber()).thenReturn(2);
            when(mockedPage.getSize()).thenReturn(10);
            when(mockedPage.isFirst()).thenReturn(false);
            when(mockedPage.isLast()).thenReturn(false);

            // Act
            PageResponse<String> response = new PageResponse<>(mockedPage);

            // Assert — todos os 7 campos
            assertThat(response.getContent()).containsExactly("item1", "item2", "item3");
            assertThat(response.getTotalElements()).isEqualTo(100L);
            assertThat(response.getTotalPages()).isEqualTo(10);
            assertThat(response.getNumber()).isEqualTo(2);
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.isFirst()).isFalse();
            assertThat(response.isLast()).isFalse();
        }
    }

    // =========================================================================
    // Grupo 2 — Página vazia (PageImpl real)
    // =========================================================================

    /**
     * Teste 2: PageResponse com página vazia — content vazio, totalElements=0,
     * totalPages=0, first=true, last=true.
     */
    @Test
    void constructor_withEmptyPage_shouldReturnEmptyPageResponse() {
        // Arrange
        PageImpl<String> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0L
        );

        // Act
        PageResponse<String> response = new PageResponse<>(emptyPage);

        // Assert
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    // =========================================================================
    // Grupo 3 — Página intermediária (PageImpl real)
    // =========================================================================

    /**
     * Teste 3: PageResponse com página intermediária (não primeira, não última) —
     * first=false, last=false.
     * 30 elementos totais, página 1 (índice 1) de tamanho 10:
     * página 0 é a primeira, página 2 é a última → página 1 é intermediária.
     */
    @Test
    void constructor_withMiddlePage_shouldHaveFirstFalseAndLastFalse() {
        // Arrange
        List<String> content = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        PageImpl<String> middlePage = new PageImpl<>(
                content,
                PageRequest.of(1, 10),
                30L
        );

        // Act
        PageResponse<String> response = new PageResponse<>(middlePage);

        // Assert
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getContent()).hasSize(10);
    }
}
