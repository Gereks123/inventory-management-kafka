package com.energia.inventory_management.controller;

import com.energia.inventory_management.exception.InsufficientStockException;
import com.energia.inventory_management.repository.entity.Item;
import com.energia.inventory_management.service.ItemService;
import com.energia.inventory_management.service.data.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    private Item testItem;
    private ItemDto testItemDto;
    private PageResponseDto<Item> testPageResponse;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(99.99);
        testItem.setQuantity(10);

        testItemDto = new ItemDto();
        testItemDto.setName("Test Item");
        testItemDto.setPrice(99.99);
        testItemDto.setQuantity(10);

        testPageResponse = PageResponseDto.<Item>builder()
                .content(List.of(testItem))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    @Nested
    class GetEndpoints {
        @Test
        void getAllItems_Success() throws Exception {
            given(itemService.getAllItems(anyInt(), anyInt(), anyString(), anyString()))
                    .willReturn(testPageResponse);

            mockMvc.perform(get("/items")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value(testItem.getName()))
                    .andExpect(jsonPath("$.pageNumber").value(0));
        }

        @Test
        void getItemById_Success() throws Exception {
            given(itemService.getItemById(1L)).willReturn(testItem);

            mockMvc.perform(get("/items/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(testItem.getName()));
        }

        @Test
        void getItemById_NotFound() throws Exception {
            given(itemService.getItemById(99L))
                    .willThrow(new EntityNotFoundException("Item not found with id: 99"));

            mockMvc.perform(get("/items/99")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Resource Not Found"))
                    .andExpect(jsonPath("$.details").value("Item not found with id: 99"));
        }
    }

    @Nested
    class CreateEndpoints {
        @Test
        void createNewItem_EmptyBody() throws Exception {
            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name").value("Name is required"))
                    .andExpect(jsonPath("$.price").value("Price is required"))
                    .andExpect(jsonPath("$.quantity").value("Quantity is required"));
        }

        @Test
        void createNewItem_InvalidPrice() throws Exception {
            testItemDto.setPrice(-10.0);

            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.price").value("Price must be greater than or equal to 0"));
        }
        @Test
        void createNewItem_InvalidName() throws Exception {
            char[] chars = new char[256];
            String invalid_name = new String(chars);
            testItemDto.setName(invalid_name);

            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.name").value("Name must not exceed 255 characters"));
        }
        @Test
        void createNewItem_InvalidQuantity() throws Exception {
            testItemDto.setQuantity(-1);

            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testItemDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.quantity").value("Quantity must be greater than or equal to 0"));
        }

    }

    @Nested
    class UpdateEndpoints {
        @Test
        void modifyItem_Success() throws Exception {
            ModifiedItemDto modifiedItemDto = new ModifiedItemDto();
            modifiedItemDto.setName("Updated Item");
            modifiedItemDto.setPrice(199.99);

            given(itemService.updateItem(eq(1L), any(ModifiedItemDto.class))).willReturn(testItem);

            mockMvc.perform(put("/items/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(modifiedItemDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(testItem.getName()));
        }

        @Test
        void sellItem_Success() throws Exception {
            SaleDto saleDto = new SaleDto();
            saleDto.setQuantity(5);

            Item updatedItem = new Item();
            updatedItem.setId(1L);
            updatedItem.setQuantity(5);

            given(itemService.sellItem(eq(1L), any(SaleDto.class))).willReturn(updatedItem);

            mockMvc.perform(patch("/items/1/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saleDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(5));
        }

        @Test
        void sellItem_InsufficientStock() throws Exception {
            SaleDto saleDto = new SaleDto();
            saleDto.setQuantity(15);

            given(itemService.sellItem(eq(1L), any(SaleDto.class)))
                    .willThrow(new InsufficientStockException(10, 15, 1L));

            mockMvc.perform(patch("/items/1/sell")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saleDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient Stock"))
                    .andExpect(jsonPath("$.details").exists());
        }
    }

    @Nested
    class DeleteEndpoints {
        @Test
        void removeItem_Success() throws Exception {
            doNothing().when(itemService).deleteItem(1L);

            mockMvc.perform(delete("/items/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        void removeItem_NotFound() throws Exception {
            doThrow(new EntityNotFoundException("Item not found with id: 99"))
                    .when(itemService).deleteItem(99L);

            mockMvc.perform(delete("/items/99")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Resource Not Found"));
        }
    }

    @Nested
    class SalesHistoryEndpoints {
        @Test
        void getSalesHistory_Success() throws Exception {
            SaleHistoryResponseDto saleHistory = SaleHistoryResponseDto.builder()
                    .itemId(1L)
                    .quantitySold(5)
                    .priceAtSale(99.99)
                    .build();

            PageResponseDto<SaleHistoryResponseDto> salesResponse = PageResponseDto.<SaleHistoryResponseDto>builder()
                    .content(List.of(saleHistory))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            given(itemService.getSalesHistory(any(), anyInt(), anyInt(), anyString(), anyString()))
                    .willReturn(salesResponse);

            mockMvc.perform(get("/items/sales")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].itemId").value(1))
                    .andExpect(jsonPath("$.content[0].quantitySold").value(5));
        }

        @Test
        void getSalesHistory_WithItemId() throws Exception {
            given(itemService.getSalesHistory(eq(1L), anyInt(), anyInt(), anyString(), anyString()))
                    .willReturn(PageResponseDto.<SaleHistoryResponseDto>builder().build());

            mockMvc.perform(get("/items/sales")
                            .param("itemId", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}