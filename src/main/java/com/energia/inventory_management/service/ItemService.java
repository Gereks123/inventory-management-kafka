package com.energia.inventory_management.service;

import com.energia.inventory_management.config.KafkaProperties;
import com.energia.inventory_management.exception.InsufficientStockException;
import com.energia.inventory_management.repository.ItemRepository;
import com.energia.inventory_management.repository.SalesHistoryRepository;
import com.energia.inventory_management.repository.entity.Item;
import com.energia.inventory_management.repository.entity.SalesHistory;
import com.energia.inventory_management.service.data.dto.*;
import com.energia.inventory_management.service.data.event.EventType;
import com.energia.inventory_management.service.data.event.QueryEvent;
import com.energia.inventory_management.service.kafka.EventPublisherService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final SalesHistoryRepository salesHistoryRepository;

    @Autowired
    private final KafkaProperties kafkaTopics;
    private final EventPublisherService eventPublisher;

    public Item addItem(ItemDto item) {
        Item newItem = mapItemDtoToItem(item, new Item());
        Item savedItem = itemRepository.save(newItem);

        // Publish a message with the Producer, mapped to ItemEvent object
        publishItemEvent(newItem, "Item Created", EventType.ITEM_CREATED, kafkaTopics.getTopic().getCreated(), savedItem);

        return savedItem;
    }

    public Item updateItem(Long id, ModifiedItemDto changedItem) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        boolean hasChanges = hasItemChanged(changedItem, item);
        if (hasChanges) {
            Item updatedItem = itemRepository.save(item);

            // Publish a message with the Producer, mapped to ItemEvent object
            publishItemEvent(updatedItem,
                    "Item Updated",
                    EventType.ITEM_UPDATED,
                    kafkaTopics.getTopic().getModified(),
                    changedItem
            );

            return updatedItem;
        }
        return item;
    }

    private static boolean hasItemChanged(ModifiedItemDto updatedItem, Item item) {
        boolean changed = false;

        if (updatedItem.hasName() && !updatedItem.getName().equals(item.getName())) {
            item.setName(updatedItem.getName());
            changed = true;
        }

        if (updatedItem.hasPrice() && !updatedItem.getPrice().equals(item.getPrice())) {
            item.setPrice(updatedItem.getPrice());
            changed = true;
        }

        if (updatedItem.hasQuantity() && !updatedItem.getQuantity().equals(item.getQuantity())) {
            item.setQuantity(updatedItem.getQuantity());
            changed = true;
        }

        return changed;
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // Publish a message with the Producer, mapped to ItemEvent object
        publishItemEvent(item,
                "Item Deleted",
                EventType.ITEM_DELETED,
                kafkaTopics.getTopic().getDeleted(),
                item
        );

        itemRepository.deleteById(id);
    }

    public Item sellItem(Long id, SaleDto soldItem) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        if (item.getQuantity() < soldItem.getQuantity()) {
            throw new InsufficientStockException(item.getQuantity(), soldItem.getQuantity(), id);
        }
        item.setQuantity(item.getQuantity() - soldItem.getQuantity());

        // Generate a Sale History report
        generateSalesHistoryEntry(id, soldItem, item);
        publishItemEvent(item,
                "Item Sold",
                EventType.ITEM_SOLD,
                kafkaTopics.getTopic().getSales(),
                soldItem
        );

        return itemRepository.save(item);
    }

    private void generateSalesHistoryEntry(Long id, SaleDto soldItem, Item item) {
        SalesHistory salesHistory = new SalesHistory();
        salesHistory.setItemId(id);
        salesHistory.setQuantitySold(soldItem.getQuantity());
        salesHistory.setPriceAtSale(item.getPrice());
        salesHistory.setSaleDate(LocalDateTime.now());
        salesHistoryRepository.save(salesHistory);
    }

    private void publishItemEvent(Item item, String eventAction, EventType eventType, String kafkaTopic, Object data) {
        eventPublisher.publishEvent(kafkaTopic,
                eventPublisher.createEvent(
                        eventType,
                        item.getId(),
                        item.getName(),
                        eventAction,
                        data
                )
        );
    }

    public PageResponseDto<SaleHistoryResponseDto> getSalesHistory(
            Long itemId,
            int page,
            int size,
            String sortBy,
            String direction) {

        // Set the direction how to return the data - in ascending or descending order
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        // Determine if endpoint was queried by ID or not
        Page<SalesHistory> salePage = itemId != null ?
                salesHistoryRepository.findByItemId(itemId, pageRequest) :
                salesHistoryRepository.findAll(pageRequest);

        Map<String, Object> paginationParams = createPaginationParams(page, size, sortBy, direction);
        if (itemId != null) {
            paginationParams.put("itemId", itemId);
        }
        publishQueryEventWithParams(EventType.SALES_HISTORY_QUERIED,
                paginationParams,
                kafkaTopics.getTopic().getSalesHistory()
        );

        List<SaleHistoryResponseDto> content = salePage.getContent().stream()
                .map(sale -> SaleHistoryResponseDto.builder()
                        .itemId(sale.getItemId())
                        .quantitySold(sale.getQuantitySold())
                        .priceAtSale(sale.getPriceAtSale())
                        .saleDate(sale.getSaleDate())
                        .build())
                .toList();

        return PageResponseDto.<SaleHistoryResponseDto>builder()
                .content(content)
                .pageNumber(salePage.getNumber())
                .pageSize(salePage.getSize())
                .totalElements(salePage.getTotalElements())
                .totalPages(salePage.getTotalPages())
                .last(salePage.isLast())
                .first(salePage.isFirst())
                .build();
    }

    public Item getItemById(Long id) {
        Item item =  itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("itemId", item.getId());

        publishQueryEventWithParams(EventType.ITEM_FETCHED, queryParams, kafkaTopics.getTopic().getFetch());

        return item;
    }

    public PageResponseDto<Item> getAllItems(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Item> itemPage = itemRepository.findAll(pageRequest);

        Map<String, Object> paginationParams = createPaginationParams(page, size, sortBy, direction);

        publishQueryEventWithParams(EventType.ITEM_FETCHED, paginationParams, kafkaTopics.getTopic().getFetch());

        return PageResponseDto.<Item>builder()
                .content(itemPage.getContent())
                .pageNumber(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .first(itemPage.isFirst())
                .build();
    }

    private Map<String, Object> createPaginationParams(int page, int size, String sortBy, String direction) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", page);
        queryParams.put("size", size);
        queryParams.put("sortBy", sortBy);
        queryParams.put("direction", direction);
        return queryParams;
    }

    private void publishQueryEventWithParams(EventType eventType, Map<String, Object> queryParams, String kafkaTopic) {
        QueryEvent queryEvent = eventPublisher.createQueryEvent(
                eventType,
                queryParams
        );
        eventPublisher.publishQueryEvent(kafkaTopic, queryEvent);
    }

    // Utility, mappers. Leave them for the last
    private Item mapItemDtoToItem(ItemDto itemDto, Item item) {
        item.setName(itemDto.getName());
        item.setPrice(itemDto.getPrice());
        item.setQuantity(itemDto.getQuantity());
        return item;
    }
}
