package com.foodservice.frontend.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FrontendController {

    private final String BASE_URL = "http://localhost:8082";
    private RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Object safeGet(String url, Model model) {
	    try {
	        return restTemplate.getForObject(url, Object.class);
	    } catch (Exception e) {
	        model.addAttribute("error", e.getMessage());
	        return null;
	    }
	}

	private Object safePost(String url, Object body, Model model) {
	    try {
	        return restTemplate.postForObject(url, body, Object.class);
	    } catch (Exception e) {
	        model.addAttribute("error", e.getMessage());
	        return null;
	    }
	}
    // ================= HOME =================
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/{name}")
    public String directPage(@PathVariable String name) {
        return switch (name.toLowerCase()) {
            case "nikhil" -> "nikhil";
            case "tushar" -> "tushar";
            case "madhurima" -> "madhurima";
            case "diptyanil" -> "diptyanil";
            case "aditya" -> "aditya";
            default -> "index";
        };
    }

    // ================= MEMBER ROUTING =================
    @GetMapping("/member/{name}")
    public String member(@PathVariable String name) {
        return switch (name.toLowerCase()) {
            case "nikhil" -> "nikhil";
            case "tushar" -> "tushar";
            case "madhurima" -> "madhurima";
            case "diptyanil" -> "diptyanil";
            case "aditya" -> "aditya";
            default -> "index";
        };
    }

    // ================= MENU ITEMS =================


    private String menuValidationError(Model model, String message) {
        model.addAttribute("menuValidationError", message);
        return "nikhil";
    }

    private String menuStatusErrorView(int statusCode) {
        if (statusCode == 404) return "menuitems-error-404";
        if (statusCode == 409) return "menuitems-error-409";
        return "menuitems-error-500";
    }

    private String handleMenuHttpError(HttpStatusCodeException e, Model model, String action) {
        int statusCode = e.getStatusCode().value();
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", e.getResponseBodyAsString());
        return menuStatusErrorView(statusCode);
    }

    private String handleMenuUnexpectedError(Exception e, Model model, String action) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", "Unexpected error: " + e.getMessage());
        return "menuitems-error-500";
    }

    private boolean invalidPositive(Integer value) {
        return value == null || value <= 0;
    }

    private boolean invalidPrice(Double value) {
        return value == null || value < 0;
    }

    private Map<String, Object> normalizeMenuRow(Object raw) {
        Map<String, Object> map = objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("itemId", map.get("itemId"));
        normalized.put("itemName", map.get("itemName"));
        normalized.put("itemDescription", map.get("itemDescription"));
        normalized.put("itemPrice", map.get("itemPrice"));
        normalized.put("restaurantId", map.get("restaurantId"));
        normalized.put("rating", map.get("rating"));
        return normalized;
    }

    private List<Map<String, Object>> normalizeMenuList(List<?> rawList) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object row : rawList) {
            normalized.add(normalizeMenuRow(row));
        }
        return normalized;
    }

    @PostMapping("/menuitems/getAll")
    public String getAllMenuItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems", List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "All Menu Items");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get All Menu Items");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get All Menu Items");
        }
    }

    @PostMapping("/menuitems/getById")
    public String getMenuItemById(@RequestParam Integer id, Model model) {
        if (invalidPositive(id)) return menuValidationError(model, "Item ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(BASE_URL + "/menuitems/" + id, Object.class);
            model.addAttribute("menuItem", normalizeMenuRow(data));
            model.addAttribute("title", "Menu Item Details");
            return "menuitems-single";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Item By ID");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Item By ID");
        }
    }

    @PostMapping("/menuitems/create")
    public String createMenuItem(
            @RequestParam("itemId") Integer itemId,
            @RequestParam("itemName") String itemName,
            @RequestParam("itemDescription") String itemDescription,
            @RequestParam("itemPrice") Double itemPrice,
            @RequestParam("restaurantId") Integer restaurantId,
            Model model) {
        if (invalidPositive(itemId)) return menuValidationError(model, "Item ID must be greater than 0.");
        if (invalidPrice(itemPrice)) return menuValidationError(model, "Item price must be 0 or greater.");
        if (invalidPositive(restaurantId)) return menuValidationError(model, "Restaurant ID must be greater than 0.");
        if (itemName == null || itemName.trim().isEmpty()) return menuValidationError(model, "Item name is required.");
        if (itemDescription == null || itemDescription.trim().isEmpty()) {
            return menuValidationError(model, "Item description is required.");
        }
        try {
            String url = BASE_URL + "/menuitems";
            Map<String, Object> request = new HashMap<>();
            request.put("itemId", itemId);
            request.put("itemName", itemName.trim());
            request.put("itemDescription", itemDescription.trim());
            request.put("itemPrice", itemPrice);
            request.put("restaurantId", restaurantId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Create Menu Item");
            return "menuitems-single";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Create Menu Item");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Create Menu Item");
        }
    }

    @PostMapping("/menuitems/update")
    public String updateMenuItem(
            @RequestParam Integer itemId,
            @RequestParam String itemName,
            @RequestParam String itemDescription,
            @RequestParam Double itemPrice,
            @RequestParam Integer restaurantId,
            Model model) {
        if (invalidPositive(itemId)) return menuValidationError(model, "Item ID must be greater than 0.");
        if (invalidPrice(itemPrice)) return menuValidationError(model, "Item price must be 0 or greater.");
        if (invalidPositive(restaurantId)) return menuValidationError(model, "Restaurant ID must be greater than 0.");
        if (itemName == null || itemName.trim().isEmpty()) return menuValidationError(model, "Item name is required.");
        if (itemDescription == null || itemDescription.trim().isEmpty()) {
            return menuValidationError(model, "Item description is required.");
        }
        try {
            String url = BASE_URL + "/menuitems/" + itemId;
            Map<String, Object> request = new HashMap<>();
            request.put("itemName", itemName.trim());
            request.put("itemPrice", itemPrice);
            request.put("itemDescription", itemDescription.trim());
            request.put("restaurantId", restaurantId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Update Menu Item");
            return "menuitems-single";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Update Menu Item");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Update Menu Item");
        }
    }

    @PostMapping("/menuitems/delete")
    public String deleteMenuItem(@RequestParam Integer id, Model model) {
        if (invalidPositive(id)) return menuValidationError(model, "Item ID must be greater than 0.");
        try {
            String url = BASE_URL + "/menuitems/" + id;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Delete Menu Item");
            return "menuitems-single";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Delete Menu Item");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Delete Menu Item");
        }
    }

    @PostMapping("/menuitems/getByRestaurant")
    public String getMenuItemsByRestaurant(@RequestParam Integer restaurantId, Model model) {
        if (invalidPositive(restaurantId)) return menuValidationError(model, "Restaurant ID must be greater than 0.");
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems/restaurant/" + restaurantId, List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Menu Items By Restaurant");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Restaurant");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Restaurant");
        }
    }

    @PostMapping("/menuitems/getByName")
    public String getMenuItemsByName(@RequestParam String name, Model model) {
        if (name == null || name.trim().isEmpty()) return menuValidationError(model, "Name cannot be blank.");
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems/name/" + name.trim(), List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Menu Items By Name");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Name");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Name");
        }
    }

    @PostMapping("/menuitems/getByPriceRange")
    public String getMenuItemsByPriceRange(@RequestParam Double min, @RequestParam Double max, Model model) {
        if (invalidPrice(min) || invalidPrice(max) || min > max) {
            return menuValidationError(model, "Price range is invalid. Use min <= max and non-negative values.");
        }
        try {
            String url = BASE_URL + "/menuitems/price-range?min=" + min + "&max=" + max;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Menu Items By Price Range");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Price Range");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Price Range");
        }
    }

    @PostMapping("/menuitems/getByRestaurantAndPrice")
    public String getMenuItemsByRestaurantAndPrice(
            @RequestParam Integer restaurantId,
            @RequestParam Double min,
            @RequestParam Double max,
            Model model) {
        if (invalidPositive(restaurantId)) return menuValidationError(model, "Restaurant ID must be greater than 0.");
        if (invalidPrice(min) || invalidPrice(max) || min > max) {
            return menuValidationError(model, "Price range is invalid. Use min <= max and non-negative values.");
        }
        try {
            String url = BASE_URL + "/menuitems/restaurant/" + restaurantId + "/price-range?min=" + min + "&max=" + max;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Menu Items By Restaurant And Price");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Restaurant And Price");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Restaurant And Price");
        }
    }

    @PostMapping("/menuitems/topSelling")
    public String getTopSellingMenuItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems/top-selling", List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Top Selling Menu Items");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Top Selling Menu Items");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Top Selling Menu Items");
        }
    }

    @PostMapping("/menuitems/getByRating")
    public String getMenuItemsByRating(@RequestParam("rating") Integer rating, Model model) {
        if (rating == null || rating < 0 || rating > 5) {
            return menuValidationError(model, "Rating must be between 0 and 5.");
        }
        try {
            String url = BASE_URL + "/menuitems/rating?rating=" + rating;
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            List<?> data = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
            model.addAttribute("menuItems", normalizeMenuList(data != null ? data : List.of()));
            model.addAttribute("title", "Menu Items By Rating: " + rating);
            return "menuitems-by-rating";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Rating");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Rating");
        }
    }

    @PostMapping("/menuitems/getByRestaurantAndRating")
    public String getMenuItemsByRestaurantAndRating(
            @RequestParam Integer restaurantId,
            @RequestParam Integer min,
            Model model) {
        if (invalidPositive(restaurantId)) return menuValidationError(model, "Restaurant ID must be greater than 0.");
        if (min == null || min < 0 || min > 5) return menuValidationError(model, "Minimum rating must be between 0 and 5.");
        try {
            String url = BASE_URL + "/menuitems/restaurant/" + restaurantId + "/rating?min=" + min;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Menu Items By Restaurant And Rating");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Menu Items By Restaurant And Rating");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Menu Items By Restaurant And Rating");
        }
    }

    @PostMapping("/menuitems/sort")
    public String sortMenuItems(
            @RequestParam(defaultValue = "itemPrice") String field,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        List<String> allowedFields = Arrays.asList("itemId", "itemName", "itemPrice", "itemDescription");
        if (field == null || !allowedFields.contains(field)) {
            return menuValidationError(model, "Sort field must be one of: itemId, itemName, itemPrice, itemDescription.");
        }
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            return menuValidationError(model, "Sort direction must be asc or desc.");
        }
        try {
            String url = BASE_URL + "/menuitems/sort?field=" + field + "&direction=" + direction;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("menuItems", normalizeMenuList(data));
            model.addAttribute("title", "Sorted Menu Items");
            return "menuitems-table";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Sort Menu Items");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Sort Menu Items");
        }
    }

    @PostMapping("/menuitems/page")
    public String getPagedMenuItems(@RequestParam Integer page, @RequestParam Integer size, Model model) {
        if (page == null || page < 0) return menuValidationError(model, "Page must be 0 or greater.");
        if (size == null || size <= 0) return menuValidationError(model, "Size must be greater than 0.");
        try {
            String url = BASE_URL + "/menuitems/page?page=" + page + "&size=" + size;
            Object raw = restTemplate.getForObject(url, Object.class);
            Map<String, Object> paged = objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
            Object itemsRaw = paged.get("items");
            List<?> items = itemsRaw instanceof List<?> list ? list : List.of();
            model.addAttribute("menuItems", normalizeMenuList(items));
            model.addAttribute("currentPage", paged.get("currentPage"));
            model.addAttribute("totalPages", paged.get("totalPages"));
            model.addAttribute("totalElements", paged.get("totalElements"));
            model.addAttribute("elementsInCurrentPage", paged.get("elementsInCurrentPage"));
            model.addAttribute("title", "Paged Menu Items");
            return "menuitems-paged";
        } catch (HttpStatusCodeException e) {
            return handleMenuHttpError(e, model, "Get Paged Menu Items");
        } catch (Exception e) {
            return handleMenuUnexpectedError(e, model, "Get Paged Menu Items");
        }
    }

    @GetMapping("/menuitems")
    public String menuItemsPage() {
        return "nikhil";
    }

    // ================= ORDER ITEMS =================

    private String orderValidationError(Model model, String message) {
        model.addAttribute("orderValidationError", message);
        return "nikhil";
    }

    private String orderStatusErrorView(int statusCode) {
        if (statusCode == 404) return "orderitems-error-404";
        if (statusCode == 409) return "orderitems-error-409";
        return "orderitems-error-500";
    }

    private String handleOrderHttpError(HttpStatusCodeException e, Model model, String action) {
        int statusCode = e.getStatusCode().value();
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", e.getResponseBodyAsString());
        return orderStatusErrorView(statusCode);
    }

    private String handleOrderUnexpectedError(Exception e, Model model, String action) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", "Unexpected error: " + e.getMessage());
        return "orderitems-error-500";
    }

    private Map<String, Object> normalizeOrderItemRow(Object raw) {
        Map<String, Object> map = objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("orderItemId", map.get("orderItemId"));
        normalized.put("orderId", map.get("orderId"));
        normalized.put("itemId", map.get("itemId"));
        normalized.put("quantity", map.get("quantity"));
        normalized.put("itemName", map.get("itemName"));
        normalized.put("itemPrice", map.get("itemPrice"));
        normalized.put("subtotal", map.get("subtotal"));
        normalized.put("itemDescription", map.get("itemDescription"));
        normalized.put("restaurantid", map.get("restaurantid"));
        return normalized;
    }

    private List<Map<String, Object>> normalizeOrderItemList(List<?> rawList) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object row : rawList) {
            normalized.add(normalizeOrderItemRow(row));
        }
        return normalized;
    }

    // 1. GET ALL
    @PostMapping("/orderitems/getAll")
    public String getAllOrderItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/orderitems", List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "All Order Items");
            return "orderitems-table";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get All Order Items");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get All Order Items");
        }
    }

    // 2. GET BY ID
    @PostMapping("/orderitems/getById")
    public String getOrderItemById(@RequestParam Integer id, Model model) {
        if (invalidPositive(id)) return orderValidationError(model, "Order Item ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(BASE_URL + "/orderitems/" + id, Object.class);
            model.addAttribute("orderItem", normalizeOrderItemRow(data));
            model.addAttribute("title", "Order Item Details");
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Order Item By ID");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Order Item By ID");
        }
    }

    // 3. CREATE (Auto ID)
    @PostMapping("/orderitems/create")
    public String createOrderItem(
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            @RequestParam Integer quantity,
            Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        if (invalidPositive(itemId)) return orderValidationError(model, "Item ID must be greater than 0.");
        if (invalidPositive(quantity)) return orderValidationError(model, "Quantity must be greater than 0.");
        try {
            String url = BASE_URL + "/orderitems";
            Map<String, Object> request = new HashMap<>();
            request.put("orderId", orderId);
            request.put("itemId", itemId);
            request.put("quantity", quantity);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Create Order Item");
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Create Order Item");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Create Order Item");
        }
    }

    // 4. FULL UPDATE
    @PostMapping("/orderitems/update")
    public String updateOrderItem(
            @RequestParam Integer id,
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            @RequestParam Integer quantity,
            Model model) {
        if (invalidPositive(id)) return orderValidationError(model, "Order Item ID must be greater than 0.");
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        if (invalidPositive(itemId)) return orderValidationError(model, "Item ID must be greater than 0.");
        if (invalidPositive(quantity)) return orderValidationError(model, "Quantity must be greater than 0.");
        try {
            String url = BASE_URL + "/orderitems/" + id;
            Map<String, Object> request = new HashMap<>();
            request.put("orderId", orderId);
            request.put("itemId", itemId);
            request.put("quantity", quantity);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Update Order Item");
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Update Order Item");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Update Order Item");
        }
    }

    // 5. PATCH QUANTITY
    @PostMapping("/orderitems/updateQuantity")
    public String updateOrderItemQuantity(
            @RequestParam Integer id,
            @RequestParam Integer quantity,
            Model model) {
        if (invalidPositive(id)) return orderValidationError(model, "Order Item ID must be greater than 0.");
        if (invalidPositive(quantity)) return orderValidationError(model, "Quantity must be greater than 0.");
        try {
            String url = BASE_URL + "/orderitems/" + id + "/quantity?quantity=" + quantity;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Update Order Item Quantity");
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Update Order Item Quantity");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Update Order Item Quantity");
        }
    }

    // 6. GET BY ORDER ID
    @PostMapping("/orderitems/getByOrderId")
    public String getOrderItemsByOrderId(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/orderitems/order/" + orderId, List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "Order Items For Order #" + orderId);
            return "orderitems-table";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Order Items By Order ID");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Order Items By Order ID");
        }
    }

    // 7. GET BY ITEM ID
    @PostMapping("/orderitems/getByItemId")
    public String getOrderItemsByItemId(@RequestParam Integer itemId, Model model) {
        if (invalidPositive(itemId)) return orderValidationError(model, "Item ID must be greater than 0.");
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/orderitems/item/" + itemId, List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "Order Items For Menu Item #" + itemId);
            return "orderitems-table";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Order Items By Item ID");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Order Items By Item ID");
        }
    }

    // 8. GET BY ORDER ID + ITEM ID
    @PostMapping("/orderitems/getByOrderAndItem")
    public String getOrderItemsByOrderAndItem(
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        if (invalidPositive(itemId)) return orderValidationError(model, "Item ID must be greater than 0.");
        try {
            String url = BASE_URL + "/orderitems/order/" + orderId + "/item/" + itemId;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "Order Items For Order #" + orderId + " & Item #" + itemId);
            return "orderitems-table";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Order Items By Order + Item");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Order Items By Order + Item");
        }
    }

    // 9. TOTAL QUANTITY BY ORDER
    @PostMapping("/orderitems/getTotalQuantity")
    public String getTotalQuantityByOrder(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/total-quantity", Object.class);
            model.addAttribute("title", "Total Quantity For Order #" + orderId);
            model.addAttribute("statLabel", "Total Quantity");
            model.addAttribute("statValue", data);
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Total Quantity By Order");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Total Quantity By Order");
        }
    }

    // 10. TOTAL PRICE BY ORDER
    @PostMapping("/orderitems/getTotalPrice")
    public String getTotalPriceByOrder(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/total-price", Object.class);
            model.addAttribute("title", "Total Price For Order #" + orderId);
            model.addAttribute("statLabel", "Total Price");
            model.addAttribute("statValue", data);
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Total Price By Order");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Total Price By Order");
        }
    }

    // 11. MOST ORDERED ITEMS
    @PostMapping("/orderitems/mostOrdered")
    public String getMostOrderedItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/orderitems/most-ordered", List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "Most Ordered Items");
            return "orderitems-most-ordered";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Most Ordered Items");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Most Ordered Items");
        }
    }

    // 12. ENRICHED DETAILS BY ORDER
    @PostMapping("/orderitems/getDetailsByOrder")
    public String getOrderItemDetails(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) return orderValidationError(model, "Order ID must be greater than 0.");
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/details", List.class);
            model.addAttribute("orderItems", normalizeOrderItemList(data));
            model.addAttribute("title", "Enriched Details For Order #" + orderId);
            return "orderitems-details";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Order Item Details");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Order Item Details");
        }
    }

    // 13. GET MENU ITEM BY ORDER ITEM ID
    @PostMapping("/orderitems/getMenuItemByOrderItemId")
    public String getMenuItemByOrderItemId(@RequestParam Integer id, Model model) {
        if (invalidPositive(id)) return orderValidationError(model, "Order Item ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/" + id + "/menuitem", Object.class);
            model.addAttribute("orderItem", normalizeOrderItemRow(data));
            model.addAttribute("title", "Menu Item For Order Item #" + id);
            return "orderitems-single";
        } catch (HttpStatusCodeException e) {
            return handleOrderHttpError(e, model, "Get Menu Item By Order Item ID");
        } catch (Exception e) {
            return handleOrderUnexpectedError(e, model, "Get Menu Item By Order Item ID");
        }
    }

    @GetMapping("/orderitems")
    public String orderItemsPage() {
        return "nikhil";
    }
    // ================= RESTAURANT =================

    // ✅ 1. Create Restaurant
    @PostMapping("/restaurant/create")
    public String createRestaurant(@RequestParam String restaurantName,
            @RequestParam String restaurantAddress,
            @RequestParam String restaurantPhone,
            Model model) {

        RestaurantsDTO dto = new RestaurantsDTO();
        dto.setRestaurantName(restaurantName);
        dto.setRestaurantAddress(restaurantAddress);
        dto.setRestaurantPhone(restaurantPhone);

        Object data = restTemplate.postForObject(
                BASE_URL + "/api/restaurants",
                dto,
                Object.class);

        model.addAttribute("single", data);
        return "single";
    }

    // ✅ 2. Get All Restaurants
    @PostMapping("/restaurant/getAll")
    public String getAllRestaurants(Model model) {
        List<?> data = restTemplate.getForObject(BASE_URL + "/api/restaurants", List.class);
        model.addAttribute("data", data);
        
        return "table";
    }

    // ✅ 3. Get Restaurant By ID
    @PostMapping("/restaurant/getById")
    public String getRestaurantById(@RequestParam Integer id, Model model) {
        try {
            Object data = restTemplate.getForObject(BASE_URL + "/api/restaurants/id/" + id, Object.class);
        model.addAttribute("single", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "single";
    }

    // ✅ 4. Search By Name
    @PostMapping("/restaurant/searchByName")
    public String searchByName(@RequestParam String name, Model model) {
    	try {
        String url = BASE_URL + "/api/restaurants/search/name?name=" + name;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table";
    }

    // ✅ 5. Search By Address
    @PostMapping("/restaurant/searchByAddress")
    public String searchByAddress(@RequestParam String address, Model model) {
    	try {
        String url = BASE_URL + "/api/restaurants/search/address?address=" + address;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table";
    }

    // ✅ 6. Search By Phone
    @PostMapping("/restaurant/searchByPhone")
    public String searchByPhone(@RequestParam String phone, Model model) {
    	try {
        String url = BASE_URL + "/api/restaurants/search/phone?phone=" + phone;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table";
    }

    // ✅ 7. Update Restaurant
    @PostMapping("/restaurant/update")
    public String updateRestaurant(@RequestParam Integer id,
            @RequestParam String restaurantName,
            @RequestParam String restaurantAddress,
            @RequestParam String restaurantPhone,
            Model model) {

        // Build DTO
        RestaurantsDTO dto = new RestaurantsDTO();
        dto.setRestaurantId(id);
        dto.setRestaurantName(restaurantName);
        dto.setRestaurantAddress(restaurantAddress);
        dto.setRestaurantPhone(restaurantPhone);

        // Send PUT request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RestaurantsDTO> request = new HttpEntity<>(dto, headers);
        try {
        restTemplate.exchange(
                BASE_URL + "/api/restaurants/" + id,
                HttpMethod.PUT,
                request,
                Void.class);

        // ✅ Fetch updated data correctly
        
        Object updated = restTemplate.getForObject(
                BASE_URL + "/api/restaurants/id/" + id,
                Object.class);

        model.addAttribute("single", updated);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }

        return "single";
    }
   
    // ✅ 8. Delete Restaurant
    @PostMapping("/restaurant/delete")
    public String deleteRestaurant(@RequestParam Integer id, Model model) {
    	try {
        restTemplate.delete(BASE_URL + "/api/restaurants/" + id);
        model.addAttribute("message", "Deleted Successfully");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "result";
    }

    // ✅ 9. Get Ratings of a Restaurant
    @PostMapping("/restaurant/ratings")
    public String getRatings(@RequestParam Integer id, Model model) {
    	try {
        String url = BASE_URL + "/api/restaurants/" + id + "/ratings";
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    	} catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/restaurant/menuitems")
    public String getMenuItemsByRestaurantId(@RequestParam Integer id, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/api/restaurants/" + id + "/menuitems",
                    List.class
            );

            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "table";
    }
    // ================= RATINGS =================

    // ✅ CREATE
    @PostMapping("/ratings/create")
    public String createRating(@ModelAttribute Rating dto, Model model) {
    	try {
        String response = restTemplate.postForObject(
                BASE_URL + "/api/ratings", dto, String.class);

        model.addAttribute("message", response);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "result";
    }

    // ✅ GET ALL
    @PostMapping("/ratings/getAll")
    public String getAllRatings(Model model) {
    	try {
        List<?> data = restTemplate.getForObject(
                BASE_URL + "/api/ratings", List.class);

        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table";
    }

    // ✅ GET BY ID
    @PostMapping("/ratings/getById")
    public String getRatingById(@RequestParam Integer id, Model model) {
    	try {
        Object data = restTemplate.getForObject(
                BASE_URL + "/api/ratings/" + id, Object.class);

        model.addAttribute("single", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "single";
    }

    // ✅ GET BY RESTAURANT
    @PostMapping("/ratings/byRestaurant")
    public String getByRestaurant(@RequestParam Integer restaurantId, Model model) {
    	try{
        String url = BASE_URL + "/api/ratings/restaurant/" + restaurantId;

        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table2";
    }

    // ✅ GET BY ORDER
    @PostMapping("/ratings/byOrder")
    public String getByOrderRatings(@RequestParam Integer orderId, Model model) {
    	try {
        String url = BASE_URL + "/api/ratings/order/" + orderId;

        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "table";
    }

    // ✅ GET BY RATING VALUE
    @PostMapping("/ratings/byValue")
    public String getByRatingValue(@RequestParam Integer rating, Model model) {
    	try {
        String url = BASE_URL + "/api/ratings/value/" + rating;

        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    	} catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    // ✅ UPDATE
    @PostMapping("/ratings/update")
    public String updateRating(@RequestParam("id") Integer id,
            @ModelAttribute Rating dto,
            Model model) {
    	try {
        restTemplate.put(BASE_URL + "/api/ratings/" + id, dto);

        Object updated = restTemplate.getForObject(
                BASE_URL + "/api/ratings/" + id, Object.class);

        model.addAttribute("single", updated);
    	} catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    // ✅ DELETE
    @PostMapping("/ratings/delete")
    public String deleteRating(@RequestParam("id") Integer id, Model model) {
    	
    	try {
        restTemplate.delete(BASE_URL + "/api/ratings/" + id);

        model.addAttribute("message", "Rating deleted successfully");
    	} catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "result";
    }

    // ✅ GET ALL FLAT
    @PostMapping("/ratings/flat")
    public String getAllFlat(Model model) {
        List<?> data = restTemplate.getForObject(
                BASE_URL + "/api/ratings/flat", List.class);

        model.addAttribute("data", data);
        return "table";
    }

    // ✅ GET FLAT BY ID
    @PostMapping("/ratings/flatById")
    public String getFlatById(@RequestParam Integer id, Model model) {
    	try {
        Object data = restTemplate.getForObject(
                BASE_URL + "/api/ratings/flat/" + id, Object.class);

        model.addAttribute("single", data);
    	} catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }
    // ================= CUSTOMERS =================

//  @PostMapping("/customers/getAll")
//  public String getAllCustomers(Model model) {
//
//      Object response = safeGet(BASE_URL + "/customer/allcustomers", model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/customers/register")
//  public String registerCustomer(@RequestParam String name,
//                                 @RequestParam String email,
//                                 @RequestParam String phone,
//                                 Model model) {
//
//      String url = BASE_URL + "/customer/register";
//
//      Map<String, Object> request = new HashMap<>();
//      request.put("customerName", name);
//      request.put("customerEmail", email);
//      request.put("customerPhone", phone);
//
//      Object response = safePost(url, request, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  @PostMapping("/customers/getById")
//  public String getCustomerById(@RequestParam Integer id, Model model) {
//
//      Object response = safeGet(BASE_URL + "/customer/" + id, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  @PostMapping("/customers/byName")
//  public String getCustomerByName(@RequestParam String name, Model model) {
//
//      String url = BASE_URL + "/customer/by-name?name=" + name;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//
//  @PostMapping("/customers/byEmail")
//  public String getCustomerByEmail(@RequestParam String email, Model model) {
//
//      String url = BASE_URL + "/customer/by-email?email=" + email;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  @PostMapping("/customers/byPhone")
//  public String getCustomerByPhone(@RequestParam String phone, Model model) {
//
//      String url = BASE_URL + "/customer/by-phone?phone=" + phone;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  @PostMapping("/customers/byNameEmail")
//  public String getByNameAndEmail(@RequestParam String name,
//                                 @RequestParam String email,
//                                 Model model) {
//
//      String url = BASE_URL + "/customer/by-name-email?name=" + name + "&email=" + email;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/customers/byNamePhone")
//  public String getByNameAndPhone(@RequestParam String name,
//                                 @RequestParam String phone,
//                                 Model model) {
//
//      String url = BASE_URL + "/customer/by-name-phone?name=" + name + "&phone=" + phone;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/customers/byEmailPhone")
//  public String getByEmailAndPhone(@RequestParam String email,
//                                  @RequestParam String phone,
//                                  Model model) {
//
//      String url = BASE_URL + "/customer/by-email-phone?email=" + email + "&phone=" + phone;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  //================= ADDRESS =================
//
//  @PostMapping("/address/byId")
//  public String getAddressByAddressId(@RequestParam Integer id, Model model) {
//
//      String url = BASE_URL + "/address/by-id?id=" + id;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("single", response);
//      }
//
//      return "single";
//  }
//
//  @PostMapping("/address/byCity")
//  public String getAddressByCityName(@RequestParam String city, Model model) {
//
//      String url = BASE_URL + "/address/by-city?city=" + city;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byPostal")
//  public String getAddressByPostal(@RequestParam String postalCode, Model model) {
//
//      String url = BASE_URL + "/address/by-postal?postalCode=" + postalCode;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byState")
//  public String getAddressByState(@RequestParam String state, Model model) {
//
//      String url = BASE_URL + "/address/by-state?state=" + state;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byCityState")
//  public String getByCityAndState(@RequestParam String city,
//                                 @RequestParam String state,
//                                 Model model) {
//
//      String url = BASE_URL + "/address/by-city-state?city=" + city + "&state=" + state;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byCityPostal")
//  public String getByCityAndPostal(@RequestParam String city,
//                                  @RequestParam String postalCode,
//                                  Model model) {
//
//      String url = BASE_URL + "/address/by-city-postal?city=" + city + "&postalCode=" + postalCode;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byStatePostal")
//  public String getByStateAndPostal(@RequestParam String state,
//                                   @RequestParam String postalCode,
//                                   Model model) {
//
//      String url = BASE_URL + "/address/by-state-postal?state=" + state + "&postalCode=" + postalCode;
//
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
//  @PostMapping("/address/byCustomer")
//  public String getAddressByCustomer(@RequestParam Integer customerId, Model model) {
//
//      String url = BASE_URL + "/address/customer/" + customerId;
//      Object response = safeGet(url, model);
//
//      if (response != null) {
//          model.addAttribute("data", response);
//      }
//
//      return "table";
//  }
//
   
 // ================= CUSTOMERS =================


    @PostMapping("/customers/getAll")
    public String getAllCustomers(Model model) {
        try {
            String url = BASE_URL + "/customer/allcustomers";

            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }


    @PostMapping("/customers/register")
    public String registerCustomer(@RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String phone,
                                   Model model) {
        try {
            String url = BASE_URL + "/customer/register";

            Map<String, Object> request = new HashMap<>();
            request.put("customerName", name);
            request.put("customerEmail", email);
            request.put("customerPhone", phone);

            Object response = restTemplate.postForObject(url, request, Object.class);
            model.addAttribute("single", response);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }


    @PostMapping("/customers/getById")
    public String getCustomerById(@RequestParam Integer id, Model model) {
        try {
            String url = BASE_URL + "/customer/" + id;

            Object data = restTemplate.getForObject(url, Object.class);
            model.addAttribute("single", data);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "single";
    }


    @PostMapping("/customers/byName")
    public String getCustomerByName(@RequestParam String name, Model model) {
        try {
            String url = BASE_URL + "/customer/by-name?name=" + name;

            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "table";
    }



    @PostMapping("/customers/byEmail")
    public String getCustomerByEmail(@RequestParam String email, Model model) {
        try {
            String url = BASE_URL + "/customer/by-email?email=" + email;

            Object data = restTemplate.getForObject(url, Object.class);
            model.addAttribute("single", data);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "single";
    }


    @PostMapping("/customers/byPhone")
    public String getCustomerByPhone(@RequestParam String phone, Model model) {
        try {
            // phone arrives as "+1234567890" — encode + manually for URL
            String encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8);
            
            URI uri = new URI(BASE_URL + "/customer/by-phone?phone=" + encodedPhone);
            
            Object data = restTemplate.getForObject(uri, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "single";
    }


    @PostMapping("/customers/byNameEmail")
    public String getByNameAndEmail(@RequestParam String name,
                                   @RequestParam String email,
                                   Model model) {
        try {
            String url = BASE_URL + "/customer/by-name-email?name=" + name + "&email=" + email;

            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/customers/byNamePhone")
    public String getByNameAndPhone(@RequestParam String name,
                                   @RequestParam String phone,
                                   Model model) {
        try {
            String encodedName  = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8);
            URI uri = new URI(BASE_URL + "/customer/by-name-phone?name=" + encodedName + "&phone=" + encodedPhone);
            List<?> data = restTemplate.getForObject(uri, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }


    @PostMapping("/customers/byEmailPhone")
    public String getByEmailAndPhone(@RequestParam String email,
                                    @RequestParam String phone,
                                    Model model) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8);
            URI uri = new URI(BASE_URL + "/customer/by-email-phone?email=" + encodedEmail + "&phone=" + encodedPhone);
            Object data = restTemplate.getForObject(uri, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

  //================= ADDRESS =================


  @PostMapping("/address/byId")
  public String getAddressById(@RequestParam Integer id, Model model) {
      try {
          String url = BASE_URL + "/address/by-id?id=" + id;

          Object data = restTemplate.getForObject(url, Object.class);
          model.addAttribute("single", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", e.getMessage());
      }
      return "single";
  }


  @PostMapping("/address/byCity")
  public String getByCity(@RequestParam String city, Model model) {
      try {
          String url = BASE_URL + "/address/by-city?city=" + city;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byPostal")
  public String getByPostal(@RequestParam String postalCode, Model model) {
      try {
          String url = BASE_URL + "/address/by-postal?postalCode=" + postalCode;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byState")
  public String getByState(@RequestParam String state, Model model) {
      try {
          String url = BASE_URL + "/address/by-state?state=" + state;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byCityState")
  public String getByCityAndState(@RequestParam String city,
                                 @RequestParam String state,
                                 Model model) {
      try {
          String url = BASE_URL + "/address/by-city-state?city=" + city + "&state=" + state;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", "Unexpected error: " + e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byCityPostal")
  public String getByCityAndPostal(@RequestParam String city,
                                  @RequestParam String postalCode,
                                  Model model) {
      try {
          String url = BASE_URL + "/address/by-city-postal?city=" + city + "&postalCode=" + postalCode;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", "Unexpected error: " + e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byStatePostal")
  public String getByStateAndPostal(@RequestParam String state,
                                   @RequestParam String postalCode,
                                   Model model) {
      try {
          String url = BASE_URL + "/address/by-state-postal?state=" + state + "&postalCode=" + postalCode;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", "Unexpected error: " + e.getMessage());
      }
      return "table";
  }


  @PostMapping("/address/byCustomer")
  public String getByCustomer(@RequestParam Integer customerId, Model model) {
      try {
          String url = BASE_URL + "/address/customer/" + customerId;

          List<?> data = restTemplate.getForObject(url, List.class);
          model.addAttribute("data", data);

      } catch (HttpClientErrorException e) {
          model.addAttribute("error", e.getResponseBodyAsString());
      } catch (Exception e) {
          model.addAttribute("error", e.getMessage());
      }
      return "table";
  }
    
    
    // ================= coupons =================

    @PostMapping("/coupons/create")
    public String createCoupon(@RequestParam String code,
            @RequestParam Double discount,
            @RequestParam String expiryDate,
            Model model) {

        String url = BASE_URL + "/api/coupons";

        Map<String, Object> request = new HashMap<>();
        request.put("couponCode", code);
        request.put("discountAmount", discount);
        request.put("expiryDate", expiryDate);

        try {
            String response = restTemplate.postForObject(url, request, String.class);

            model.addAttribute("single", response);

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }

    @PostMapping("/coupons/getAll")
    public String getAllCoupons(Model model) {

        List<?> data = (List<?>) safeGet(BASE_URL + "/api/coupons", model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/getById")
    public String getCouponById(@RequestParam Integer id, Model model) {

        Object data = safeGet(BASE_URL + "/api/coupons/" + id, model, false);

        if (data != null) {
            model.addAttribute("single", data);
        }

        return "single";
    }

    @PostMapping("/coupons/byCode")
    public String getByCode(@RequestParam String code, Model model) {

        String url = BASE_URL + "/api/coupons/by-code?code=" + code;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byDiscount")
    public String getByDiscount(@RequestParam Double discount, Model model) {

        String url = BASE_URL + "/api/coupons/by-discount?discount=" + discount;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/discountRange")
    public String getByDiscountRange(@RequestParam Double min,
            @RequestParam Double max,
            Model model) {

        String url = BASE_URL + "/api/coupons/by-discount-range?min=" + min + "&max=" + max;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byExpiry")
    public String getByExpiry(@RequestParam String date, Model model) {

        String url = BASE_URL + "/api/coupons/by-expiry?date=" + date;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byCodeDiscount")
    public String getByCodeDiscount(@RequestParam String code,
            @RequestParam Double discount,
            Model model) {

        String url = BASE_URL + "/api/coupons/by-code-discount?code=" + code + "&discount=" + discount;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byCodeExpiry")
    public String getByCodeExpiry(@RequestParam String code,
            @RequestParam String date,
            Model model) {

        String url = BASE_URL + "/api/coupons/by-code-expiry?code=" + code + "&date=" + date;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byDiscountExpiry")
    public String getByDiscountExpiry(@RequestParam Double discount,
            @RequestParam String date,
            Model model) {

        String url = BASE_URL + "/api/coupons/by-discount-expiry?discount=" + discount + "&date=" + date;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/byCodeStarts")
    public String getByCodeStarts(@RequestParam String prefix, Model model) {

        String url = BASE_URL + "/api/coupons/by-code-starts?prefix=" + prefix;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/minDiscount")
    public String getByMinDiscount(@RequestParam Double min, Model model) {

        String url = BASE_URL + "/api/coupons/by-min-discount?min=" + min;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/maxDiscount")
    public String getByMaxDiscount(@RequestParam Double max, Model model) {

        String url = BASE_URL + "/api/coupons/by-max-discount?max=" + max;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/expired")
    public String getExpiredCoupons(Model model) {

        List<?> data = (List<?>) safeGet(BASE_URL + "/api/coupons/expired", model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/active")
    public String getActiveCoupons(Model model) {

        List<?> data = (List<?>) safeGet(BASE_URL + "/api/coupons/active", model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/coupons/updateDiscount")
    public String updateDiscount(@RequestParam Integer id,
            @RequestParam Double discount,
            Model model) {

        String url = BASE_URL + "/api/coupons/" + id + "/discount?discount=" + discount;

        try {
            restTemplate.put(url, null);
            model.addAttribute("single", "Discount updated successfully!");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }

    @PostMapping("/coupons/updateExpiry")
    public String updateExpiry(@RequestParam Integer id,
            @RequestParam String date,
            Model model) {

        String url = BASE_URL + "/api/coupons/" + id + "/expiry?date=" + date;

        try {
            restTemplate.put(url, null);
            model.addAttribute("single", "Expiry updated successfully!");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }

    @PostMapping("/coupons/updateAll")
    public String updateCoupon(@RequestParam Integer id,
            @RequestParam Double discount,
            @RequestParam String date,
            Model model) {

        String url = BASE_URL + "/api/coupons/" + id +
                "?discount=" + discount + "&date=" + date;

        try {
            restTemplate.put(url, null);
            model.addAttribute("single", "Coupon updated successfully!");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }
    // ================= ORDER COUPONS =================

    @PostMapping("/ordercoupons/getAll")
    public String getAllOrderCoupons(Model model) {
        List<?> data = (List<?>) safeGet(BASE_URL + "/api/ordercoupons", model, true);
        if (data != null) {
            model.addAttribute("single", data);
        }
        model.addAttribute("data", data);
        return "table";
    }

    @PostMapping("/ordercoupons/byOrder")
    public String getByOrder(@RequestParam Integer orderId, Model model) {
        String url = BASE_URL + "/api/ordercoupons/" + orderId + "/coupons";
        List<?> data = (List<?>) safeGet(url, model, true);
        if (data != null) {
            model.addAttribute("single", data);
        }
        model.addAttribute("data", data);
        return "table";
    }

    @PostMapping("/ordercoupons/byCoupon")
    public String getByCoupon(@RequestParam Integer couponId, Model model) {

        String url = BASE_URL + "/api/ordercoupons/coupon/" + couponId;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/ordercoupons/byOrderAndCoupon")
    public String getByOrderAndCoupon(@RequestParam Integer orderId,
            @RequestParam Integer couponId,
            Model model) {

        String url = BASE_URL + "/api/ordercoupons/order/" + orderId + "/coupon/" + couponId;

        List<?> data = (List<?>) safeGet(url, model, true);

        if (data != null) {
            model.addAttribute("data", data);
        }

        return "table";
    }

    @PostMapping("/ordercoupons/apply")
    public String applyCoupon(@RequestParam Integer orderId,
            @RequestParam Integer couponId,
            Model model) {

        String url = BASE_URL + "/api/ordercoupons/" + orderId +
                "/apply-coupon?couponId=" + couponId;

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }

    @PostMapping("/orders/assignDriver")
    public String assignDriver(@RequestParam Integer orderId,
            @RequestParam Integer driverId,
            Model model) {

        String url = BASE_URL + "/api/orders/" + orderId + "/assign-driver?driverId=" + driverId;

        WebClient webClient = WebClient.create();

        // ✅ Receive JSON as Map
        Map<String, Object> response = webClient.patch()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // ✅ Convert to entrySet for Thymeleaf
        model.addAttribute("single", response.entrySet());

        return "single";
    }

    @PostMapping("/orders/items")
    public String getOrderItems(@RequestParam Integer id, Model model) {

        String url = BASE_URL + "/api/orders/" + id + "/items";
        List<?> data = restTemplate.getForObject(url, List.class);

        model.addAttribute("data", data);
        return "table";
    }
//======================order=================
    // ================= ORDERS =================

    private final String ORDERS_BASE = BASE_URL + "/apifor/orders";

    private String ordersValidationError(Model model, String message) {
        model.addAttribute("orderValidationError", message);
        return "aditya";
    }

    private String ordersStatusErrorView(int statusCode) {
        if (statusCode == 404) return "orders-error-404";
        if (statusCode == 409) return "orders-error-409";
        return "orders-error-500";
    }

    private String handleOrdersHttpError(HttpStatusCodeException e, Model model, String action) {
        int statusCode = e.getStatusCode().value();
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", e.getResponseBodyAsString());
        return ordersStatusErrorView(statusCode);
    }

    private String handleOrdersUnexpectedError(Exception e, Model model, String action) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("action", action);
        model.addAttribute("errorBody", "Unexpected error: " + e.getMessage());
        return "orders-error-500";
    }

    private Map<String, Object> normalizeOrderRow(Object raw) {
        return objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
    }

    private List<Map<String, Object>> normalizeOrderList(List<?> rawList) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object row : rawList) {
            normalized.add(normalizeOrderRow(row));
        }
        return normalized;
    }

    // 1. GET ALL / BY STATUS
    @PostMapping("/orders/getAll")
    public String getAllOrders(@RequestParam(required = false) String status, Model model) {
        try {
            String url = ORDERS_BASE;
            if (status != null && !status.isBlank()) {
                url += "?status=" + status;
            }
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("orders", normalizeOrderList(data));
            model.addAttribute("title", (status != null && !status.isBlank())
                    ? "Orders with Status: " + status : "All Orders");
            return "orders-table";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get All Orders");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get All Orders");
        }
    }

    // 2. GET BY ID
    @PostMapping("/orders/getById")
    public String getOrderById(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) return ordersValidationError(model, "Order ID must be greater than 0.");
        try {
            Object data = restTemplate.getForObject(ORDERS_BASE + "/" + orderId, Object.class);
            model.addAttribute("order", normalizeOrderRow(data));
            model.addAttribute("title", "Order #" + orderId);
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get Order By ID");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get Order By ID");
        }
    }

    // 3. CREATE (Auto ID)
    @PostMapping("/orders/create")
    public String createOrder(
            @RequestParam Integer customerId,
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) Integer driverId,
            @RequestParam String orderStatus,
            @RequestParam String orderDate,
            Model model) {
        if (invalidPositive(customerId)) return ordersValidationError(model, "Customer ID must be greater than 0.");
        if (invalidPositive(restaurantId)) return ordersValidationError(model, "Restaurant ID must be greater than 0.");
        if (orderStatus == null || orderStatus.isBlank()) return ordersValidationError(model, "Order Status is required.");
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("customerId", customerId);
            request.put("restaurantId", restaurantId);
            if (driverId != null) request.put("deliveryDriverId", driverId);
            request.put("orderStatus", orderStatus);
            request.put("orderDate", orderDate.contains("T") ? orderDate : orderDate + "T00:00:00");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(ORDERS_BASE, HttpMethod.POST, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Create Order");
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Create Order");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Create Order");
        }
    }

    // 4. GET BY CUSTOMER (+ optional status)
    @PostMapping("/orders/getByCustomer")
    public String getOrdersByCustomer(
            @RequestParam Integer customerId,
            @RequestParam(required = false) String status,
            Model model) {
        if (invalidPositive(customerId)) return ordersValidationError(model, "Customer ID must be greater than 0.");
        try {
            String url = ORDERS_BASE + "/customer/" + customerId;
            if (status != null && !status.isBlank()) url += "?status=" + status;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("orders", normalizeOrderList(data));
            model.addAttribute("title", "Orders for Customer #" + customerId
                    + (status != null && !status.isBlank() ? " (" + status + ")" : ""));
            return "orders-table";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get Orders By Customer");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get Orders By Customer");
        }
    }

    // 5. GET BY RESTAURANT (+ optional status)
    @PostMapping("/orders/getByRestaurant")
    public String getOrdersByRestaurant(
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) String status,
            Model model) {
        if (invalidPositive(restaurantId)) return ordersValidationError(model, "Restaurant ID must be greater than 0.");
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId;
            if (status != null && !status.isBlank()) url += "?status=" + status;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("orders", normalizeOrderList(data));
            model.addAttribute("title", "Orders for Restaurant #" + restaurantId
                    + (status != null && !status.isBlank() ? " (" + status + ")" : ""));
            return "orders-table";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get Orders By Restaurant");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get Orders By Restaurant");
        }
    }

    // 6. GET BY DRIVER
    @PostMapping("/orders/getByDriver")
    public String getOrdersByDriver(@RequestParam Integer driverId, Model model) {
        if (invalidPositive(driverId)) return ordersValidationError(model, "Driver ID must be greater than 0.");
        try {
            List<?> data = restTemplate.getForObject(ORDERS_BASE + "/driver/" + driverId, List.class);
            model.addAttribute("orders", normalizeOrderList(data));
            model.addAttribute("title", "Orders for Driver #" + driverId);
            return "orders-table";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get Orders By Driver");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get Orders By Driver");
        }
    }

    // 7. COUNT BY RESTAURANT + STATUS
    @PostMapping("/orders/countByRestaurantStatus")
    public String countOrdersByRestaurantAndStatus(
            @RequestParam Integer restaurantId,
            @RequestParam String status,
            Model model) {
        if (invalidPositive(restaurantId)) return ordersValidationError(model, "Restaurant ID must be greater than 0.");
        if (status == null || status.isBlank()) return ordersValidationError(model, "Status is required.");
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId + "/count?status=" + status;
            Object data = restTemplate.getForObject(url, Object.class);
            Map<String, Object> map = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {});
            model.addAttribute("title", "Order Count — Restaurant #" + restaurantId + " (" + status + ")");
            model.addAttribute("statLabel", "Order Count");
            model.addAttribute("statValue", map.get("count"));
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Count Orders");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Count Orders");
        }
    }

    // 8. FULL UPDATE
    @PostMapping("/orders/update")
    public String updateOrder(
            @RequestParam Integer orderId,
            @RequestParam Integer customerId,
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) Integer driverId,
            @RequestParam String orderStatus,
            @RequestParam String orderDate,
            Model model) {
        if (invalidPositive(orderId)) return ordersValidationError(model, "Order ID must be greater than 0.");
        if (invalidPositive(customerId)) return ordersValidationError(model, "Customer ID must be greater than 0.");
        if (invalidPositive(restaurantId)) return ordersValidationError(model, "Restaurant ID must be greater than 0.");
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("customerId", customerId);
            request.put("restaurantId", restaurantId);
            if (driverId != null) request.put("deliveryDriverId", driverId);
            request.put("orderStatus", orderStatus);
            request.put("orderDate", orderDate.contains("T") ? orderDate : orderDate + "T00:00:00");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    ORDERS_BASE + "/" + orderId, HttpMethod.PUT, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Update Order #" + orderId);
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Update Order");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Update Order");
        }
    }

    // 9. UPDATE STATUS ONLY (PATCH)
    @PostMapping("/orders/updateStatus")
    public String updateOrderStatus(
            @RequestParam Integer orderId,
            @RequestParam String status,
            Model model) {
        if (invalidPositive(orderId)) return ordersValidationError(model, "Order ID must be greater than 0.");
        if (status == null || status.isBlank()) return ordersValidationError(model, "Status is required.");
        try {
            String url = ORDERS_BASE + "/" + orderId + "/status?status=" + status;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Update Status — Order #" + orderId);
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Update Order Status");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Update Order Status");
        }
    }

    // 10. ASSIGN DRIVER (PATCH)
    @PostMapping("/orders/assignDrivers")
    public String assignDriverToOrder(
            @RequestParam Integer orderId,
            @RequestParam Integer driverId,
            Model model) {
        if (invalidPositive(orderId)) return ordersValidationError(model, "Order ID must be greater than 0.");
        if (invalidPositive(driverId)) return ordersValidationError(model, "Driver ID must be greater than 0.");
        try {
            String url = ORDERS_BASE + "/" + orderId + "/assign-driver?driverId=" + driverId;
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            model.addAttribute("message", response.getBody());
            model.addAttribute("title", "Assign Driver — Order #" + orderId);
            return "orders-single";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Assign Driver");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Assign Driver");
        }
    }


    // 12. PAGED ORDERS
    @PostMapping("/orders/getPaged")
    public String getPagedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            String url = ORDERS_BASE + "/paged?page=" + page + "&size=" + size;
            Object data = restTemplate.getForObject(url, Object.class);
            Map<String, Object> pagedResponse = objectMapper.convertValue(data, new TypeReference<Map<String, Object>>() {});
            List<?> content = (List<?>) pagedResponse.get("items");
            model.addAttribute("orders", normalizeOrderList(content != null ? content : List.of()));
            model.addAttribute("title", "Paged Orders");
            model.addAttribute("pageInfo", "Page " + page + " | Size " + size
                    + " | Total Elements: " + pagedResponse.getOrDefault("totalElements", "?")
                    + " | Total Pages: " + pagedResponse.getOrDefault("totalPages", "?"));
            return "orders-paged";
        } catch (HttpStatusCodeException e) {
            return handleOrdersHttpError(e, model, "Get Paged Orders");
        } catch (Exception e) {
            return handleOrdersUnexpectedError(e, model, "Get Paged Orders");
        }
    }

    @GetMapping("/orders")
    public String ordersPage() {
        return "aditya";
    }

    // ================= DRIVERS =================

    @PostMapping("/drivers/getAll")
    public String getAllDrivers(Model model) {
        List<?> data = restTemplate.getForObject(BASE_URL + "/api/drivers", List.class);
        model.addAttribute("data", data);
        return "table";
    }

    @PostMapping("/drivers/getById")
    public String getDriverById(@RequestParam Integer id, Model model) {
    	try {
        Object data = restTemplate.getForObject(BASE_URL + "/api/drivers/" + id, Object.class);
        model.addAttribute("single", data);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "single";
    }

    @PostMapping("/drivers/create")
    public String createDriver(@RequestParam Integer id,@RequestParam String name,
            @RequestParam String phone,
            @RequestParam String vehicle,
            Model model) {
    	try {
        String url = BASE_URL + "/api/drivers";

        Map<String, Object> request = new HashMap<>();
        request.put("driverId",id);
        request.put("driverName", name);
        request.put("driverPhone", phone);
        request.put("driverVehicle", vehicle);

        Object response = restTemplate.postForObject(url, request, Object.class);
        model.addAttribute("single", response);
    } catch (HttpClientErrorException e) {
        model.addAttribute("error", e.getResponseBodyAsString());
    } catch (Exception e) {
        model.addAttribute("error", "Unexpected error: " + e.getMessage());
    }
        return "single";
    }
 // ================= DRIVERS (EXTENDED) =================

    @PostMapping("/drivers/update")
    public String updateDriver(@RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String vehicle,
            Model model) {

        String url = BASE_URL + "/api/drivers/" + id;

        Map<String, Object> request = new HashMap<>();
        request.put("driverName", name);
        request.put("driverPhone", phone);
        request.put("driverVehicle", vehicle);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            Object updated = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, Object.class).getBody();
            model.addAttribute("single", updated);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "single";
    }

    @PostMapping("/drivers/delete")
    public String deleteDriver(@RequestParam Integer id, Model model) {
        try {
            restTemplate.delete(BASE_URL + "/api/drivers/" + id);
            model.addAttribute("message", "Driver deleted successfully");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "result";
    }

    @PostMapping("/drivers/orders")
    public String getDriverOrders(@RequestParam Integer driverId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/api/drivers/" + driverId + "/orders",
                    List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/drivers/assignOrder")
    public String assignOrderToDriver(@RequestParam Integer driverId,
            @RequestParam Integer orderId,
            Model model) {

        String url = BASE_URL + "/api/drivers/" + driverId + "/assign-order/" + orderId;

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, HttpEntity.EMPTY, String.class);
            model.addAttribute("message", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "result";
    }

    @PostMapping("/drivers/completeOrder")
    public String completeOrder(@RequestParam Integer driverId,
            @RequestParam Integer orderId,
            Model model) {

        String url = BASE_URL + "/api/drivers/" + driverId + "/complete-order/" + orderId;

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, HttpEntity.EMPTY, String.class);
            model.addAttribute("message", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }

        return "result";
    }
//----------------------------------------------------------------------------------------------------
    private Object safeGet(String url, Model model, boolean isList) {
        try {
            if (isList) {
                List<?> rawList = restTemplate.getForObject(url, List.class);

                List<Map<String, Object>> result = new java.util.ArrayList<>();

                if (rawList != null) {
                    for (Object obj : rawList) {
                        if (obj instanceof Map) {
                            result.add((Map<String, Object>) obj);
                        }
                    }
                }

                return result;

            } else {
                Object obj = restTemplate.getForObject(url, Object.class);

                if (obj instanceof Map) {
                    return obj;
                }

                return obj;

            }
        } catch (HttpClientErrorException e) {

            String errorBody = e.getResponseBodyAsString();
            String message = errorBody;

            try {
                int start = errorBody.indexOf("\"message\":\"") + 11;
                int end = errorBody.indexOf("\"", start);
                message = errorBody.substring(start, end);
            } catch (Exception ignored) {
            }

            model.addAttribute("error", message);

        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return null;
    }
}