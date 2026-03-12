package com.organiza.application.usecase;

import com.organiza.application.dto.OrderDTO;
import com.organiza.domain.entity.ItemPedido;
import com.organiza.domain.entity.Mesa;
import com.organiza.domain.entity.Pedido;
import com.organiza.domain.enums.StatusMesa;
import com.organiza.domain.enums.StatusPedido;
import com.organiza.domain.repository.MesaRepositoryPort;
import com.organiza.domain.repository.PedidoRepositoryPort;
import com.organiza.infrastructure.exception.BusinessException;
import com.organiza.infrastructure.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private MesaRepositoryPort mesaRepository;

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    private CreateOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrderUseCase(mesaRepository, pedidoRepository);
    }

    @Test
    void deveCriarPedidoComDadosValidos() {
        Mesa mesa = new Mesa(1, 1, StatusMesa.LIVRE);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(10);
            return p;
        });

        List<ItemPedido> itens = List.of(new ItemPedido("Café", 1, 3.50));
        OrderDTO result = useCase.execute(1, "João", itens);

        assertNotNull(result);
        assertEquals("João", result.nomeCliente());
        assertEquals("PENDENTE", result.status());
        assertEquals(1, result.itens().size());
        assertEquals(3.50, result.total(), 0.001);

        verify(mesaRepository).updateStatus(1, StatusMesa.OCUPADA);
    }

    @Test
    void deveLancarExcecaoParaMesaInexistente() {
        when(mesaRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> useCase.execute(999, "João", List.of(new ItemPedido("Café", 1, 0.0))));
    }

    @Test
    void deveLancarExcecaoParaNomeVazio() {
        Mesa mesa = new Mesa(1, 1, StatusMesa.LIVRE);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        assertThrows(ValidationException.class,
                () -> useCase.execute(1, "  ", List.of(new ItemPedido("Café", 1, 0.0))));
    }

    @Test
    void deveLancarExcecaoParaNomeNulo() {
        Mesa mesa = new Mesa(1, 1, StatusMesa.LIVRE);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        assertThrows(ValidationException.class,
                () -> useCase.execute(1, null, List.of(new ItemPedido("Café", 1, 0.0))));
    }

    @Test
    void deveLancarExcecaoParaPedidoSemItens() {
        Mesa mesa = new Mesa(1, 1, StatusMesa.LIVRE);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        assertThrows(ValidationException.class,
                () -> useCase.execute(1, "João", List.of()));
    }

    @Test
    void deveLancarExcecaoParaItensNulos() {
        Mesa mesa = new Mesa(1, 1, StatusMesa.LIVRE);
        when(mesaRepository.findById(1)).thenReturn(Optional.of(mesa));

        assertThrows(ValidationException.class,
                () -> useCase.execute(1, "João", null));
    }
}
