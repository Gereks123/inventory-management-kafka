# Inventory Management API Documentation

## Table of Contents
- [Requirements](#requirements)
- [Running application](#running-application)
- [API Overview](#api-overview)
  - [Base URL](#base-url)
  - [Endpoints](#endpoints)
      - [Items](#items)
        - [Get All Items](#get-all-items)
        - [Get Item by ID](#get-item-by-id)
        - [Create New Item](#create-new-item)
        - [Update Item](#update-item)
        - [Sell Item](#sell-item)
        - [Delete Item](#delete-item)
      - [Sales](#sales)
        - [Get Sales History](#get-sales)
- [Error Handling](#error-handling)
- [Event Logging](#event-logging)

## Requirements
- Java 17+, using IntelliJ IDE, you can adjust the SDK through the project settings.
- Have Docker desktop or just docker-compose installed.
- Bring your best self to test the application!! :D 

## Running application
- Firstly run `docker-compose -d`, set up of PostgresSql with a database and launches Kafka with Zookeeper. 
- You can run the application from you IDE (preferably IntelliJ) or using `./gradlew bootRun`. Automatically Populates the database with the needed tables!
- To stop the services the following commands: `docker-compose down` and for the application `./gradlew --stop` OR within the terminal use **CTRL+C**

## Application overview
- This application imitates a very small-scale application for inventory management. 
- This application could be scaled to something bigger with further development.
- REST endpoints

Technologies used are:
- Spring Boot (3.4.1) - Spring MVC architectural pattern
- PostgresSql for data storage/persistence
- Kafka acting as a log retention tool. (Pub/Sub) logic implementation. 
  - Each action has its separate topic. The idea was to "imitate" a real microservice where Kafka could potentially talk to 10-20 applications.
- Zookeeper for Kafka
- Docker - docker-compose to bundle everything together and allow the ease of deployment/testing of the whole flow.

## API Overview
This API provides endpoints to manage inventory items and track sales history. It supports CRUD operations for items and includes pagination for large datasets.

**NB!!!** You can use the api_endpoints.postman_collection.json to get a template with defined endpoints to query the application!  

## Base URL
```
http://localhost:8080/items
```

## Endpoints

### Items

#### Get All Items
Retrieves a paginated list of all items in the inventory.

```http
GET /items
```

**Query Parameters:**

| Parameter  | Type    | Default | Description                              |
|------------|---------|---------|------------------------------------------|
| page       | Integer | 0       | Page number (zero-based)                 |
| size       | Integer | 10      | Number of items per page                 |
| sortBy     | String  | "id"    | Field to sort by                         |
| direction  | String  | "asc"   | Sort direction ("asc" or "desc")         |

**Example Request:**
```http
GET /items?page=0&size=10&sortBy=name&direction=desc
```

**Example Response:**
```json
{
    "content": [
        {
            "id": 1,
            "name": "Laptop",
            "price": 999.99,
            "quantity": 50
        },
        {
            "id": 2,
            "name": "Mouse",
            "price": 29.99,
            "quantity": 100
        }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2,
    "totalPages": 1,
    "first": true,
    "last": true
}
```

#### Get Item by ID
Retrieves a specific item by its ID.

```http
GET /items/{id}
```

**Example Request:**
```http
GET /items/1
```

**Example Response:**
```json
{
    "id": 1,
    "name": "Laptop",
    "price": 999.99,
    "quantity": 50
}
```

#### Create New Item
Creates a new item in the inventory.

```http
POST /items
```

**Request Body:**
```json
{
    "name": "Keyboard",
    "price": 59.99,
    "quantity": 30
}
```

**Validation Rules:**
- `name`: Required, maximum 255 characters
- `price`: Required, must be positive
- `quantity`: Required, must be non-negative

#### Update Item
Updates an existing item. Supports partial updates.

```http
PUT /items/{id}
```

**Request Body:**
```json
{
    "name": "Wireless Keyboard",
    "price": 79.99
}
```

**Validation Rules:**
- All fields are optional
- When provided:
    - `name`: Maximum 255 characters
    - `price`: Must be positive
    - `quantity`: Must be non-negative

#### Sell Item
Records a sale for an item and updates its quantity.

```http
PATCH /items/{id}/sell
```

**Request Body:**
```json
{
    "quantity": 5
}
```

**Validation Rules:**
- `quantity`: Required, must be positive and not exceed current stock

#### Delete Item
Removes an item from the inventory.

```http
DELETE /items/{id}
```

### Sales

#### Get Sales
Retrieves the entireity of the sales history or use the itemId field to query specific items with pagination support.

```http
GET /items/sales
```

**Query Parameters:**

| Parameter  | Type    | Default | Description                      |
|------------|---------|---------|----------------------------------|
| itemId     | Long    | null    | Filter sales by specific item    |
| page       | Integer | 0       | Page number                      |
| size       | Integer | 10      | Items per page                   |
| sortBy     | String  | "id"    | Field to sort by                 |
| direction  | String  | "asc"   | Sort direction ("asc" or "desc") |


**Example Response:**
```json
{
    "content": [
        {
            "itemId": 1,
            "quantitySold": 5,
            "priceAtSale": 999.99,
            "saleDate": "2024-01-12T14:30:00"
        }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
}
```

## Error Handling

The API uses conventional HTTP response codes:
- `200 OK`: Successful request
- `201 Created`: Resource successfully created
- `204 No Content`: Successful deletion
- `400 Bad Request`: Invalid input
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

**Example Error Response:**
```json
{
    "timestamp": "2024-01-12T15:30:45",
    "message": "Validation Error",
    "details": "Price must be greater than 0"
}
```

### Common Error Cases

#### Validation Error
```json
{
    "timestamp": "2024-01-12T15:30:45",
    "message": "Validation Error",
    "details": "Name must not exceed 255 characters"
}
```

#### Resource Not Found
```json
{
    "timestamp": "2024-01-12T15:30:45",
    "message": "Resource Not Found",
    "details": "Item not found with id: 1"
}
```

#### Insufficient Stock
```json
{
    "timestamp": "2024-01-12T15:30:45",
    "message": "Insufficient Stock",
    "details": "Available: 5, Requested: 10"
}
```

## Event Logging
All operations are logged using Kafka events for audit purposes. The following events are tracked:
- Item Created
- Item Updated
- Item Deleted
- Item Sold
- Sales History Queried