package com.foodservice.frontend.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FrontendController {

    private final String BASE_URL = "http://localhost:8082";
    private final String ORDERS_BASE = BASE_URL + "/api/orders";
    private RestTemplate restTemplate = new RestTemplate(
            new org.springframework.http.client.HttpComponentsClientHttpRequestFactory());

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

    private boolean invalidPositive(Integer value) {
        return value == null || value <= 0;
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


    @PostMapping("/menuitems/getAll")
    public String getAllMenuItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems", List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getById")
    public String getMenuItemById(@RequestParam Integer id, Model model) {
        try {
            Object data = restTemplate.getForObject(BASE_URL + "/menuitems/" + id, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/create")
    public String createMenuItem(
            @RequestParam Integer itemId,
            @RequestParam String itemName,
            @RequestParam(required = false) String itemDescription,
            @RequestParam Double itemPrice,
            @RequestParam Integer restaurantId,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems";

            Map<String, Object> request = new HashMap<>();
            request.put("itemId", itemId);
            request.put("itemName", itemName);
            if (itemDescription != null && !itemDescription.isBlank()) {
                request.put("itemDescription", itemDescription);
            }
            request.put("itemPrice", itemPrice);
            request.put("restaurantId", restaurantId);

            Object response = restTemplate.postForObject(url, request, Object.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/update")
    public String updateMenuItem(
            @RequestParam Integer itemId,
            @RequestParam String itemName,
            @RequestParam String itemDescription,
            @RequestParam Double itemPrice,
            @RequestParam Integer restaurantId,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/" + itemId;

            Map<String, Object> request = new HashMap<>();
            request.put("itemName", itemName);
            request.put("itemPrice", itemPrice);
            request.put("itemDescription",itemDescription);
            request.put("restaurantId", restaurantId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class);

            model.addAttribute("single", response.getBody());

        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/updatePrice")
    public String updateMenuItemPrice(
            @RequestParam Integer id,
            @RequestParam Double price,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/" + id + "/price?price=" + price;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, String.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/delete")
    public String deleteMenuItem(@RequestParam Integer id, Model model) {
        try {
            String url = BASE_URL + "/menuitems/" + id;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, String.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/deleteByRestaurant")
    public String deleteMenuItemsByRestaurant(@RequestParam Integer restaurantId, Model model) {
        try {
            String url = BASE_URL + "/menuitems/restaurant/" + restaurantId;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, Object.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/menuitems/getByRestaurant")
    public String getMenuItemsByRestaurant(@RequestParam Integer restaurantId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/menuitems/restaurant/" + restaurantId, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByOrder")
    public String getMenuItemsByOrder(@RequestParam Integer orderId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/menuitems/order/" + orderId, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByName")
    public String getMenuItemsByName(@RequestParam String name, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/menuitems/name/" + name, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByPriceRange")
    public String getMenuItemsByPriceRange(
            @RequestParam Double min,
            @RequestParam Double max,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/price-range?min=" + min + "&max=" + max;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByRestaurantAndPrice")
    public String getMenuItemsByRestaurantAndPrice(
            @RequestParam Integer restaurantId,
            @RequestParam Double min,
            @RequestParam Double max,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/restaurant/" + restaurantId
                    + "/price-range?min=" + min + "&max=" + max;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByCustomer")
    public String getMenuItemsByCustomer(@RequestParam Integer customerId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/menuitems/customer/" + customerId, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByOrderDate")
    public String getMenuItemsByOrderDate(@RequestParam String date, Model model) {
        try {
            String url = BASE_URL + "/menuitems/order-date?date=" + date;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/topSelling")
    public String getTopSellingMenuItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/menuitems/top-selling", List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByRating")
    public String getMenuItemsByRating(@RequestParam Integer rating, Model model) {
        try {
            String url = BASE_URL + "/menuitems/rating?rating=" + rating;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/getByRestaurantAndRating")
    public String getMenuItemsByRestaurantAndRating(
            @RequestParam Integer restaurantId,
            @RequestParam Integer min,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/restaurant/" + restaurantId + "/rating?min=" + min;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/sort")
    public String sortMenuItems(
            @RequestParam(defaultValue = "itemPrice") String field,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/sort?field=" + field + "&direction=" + direction;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/menuitems/page")
    public String getPagedMenuItems(
            @RequestParam Integer page,
            @RequestParam Integer size,
            Model model) {
        try {
            String url = BASE_URL + "/menuitems/page?page=" + page + "&size=" + size;
            Object data = restTemplate.getForObject(url, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @GetMapping("/menuitems")
    public String menuItemsPage() {
        return "menuitems";
    }

    // ================= ORDER ITEMS =================

    @PostMapping("/orderitems/getAll")
    public String getAllOrderItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(BASE_URL + "/orderitems", List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/getById")
    public String getOrderItemById(@RequestParam Integer id, Model model) {
        try {
            Object data = restTemplate.getForObject(BASE_URL + "/orderitems/" + id, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/create")
    public String createOrderItem(
            @RequestParam Integer orderItemId,
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            @RequestParam Integer quantity,
            Model model) {
        try {
            String url = BASE_URL + "/orderitems";

            Map<String, Object> request = new HashMap<>();
            request.put("orderItemId", orderItemId);
            request.put("orderId", orderId);
            request.put("itemId", itemId);
            request.put("quantity", quantity);

            Object response = restTemplate.postForObject(url, request, Object.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/createManual")
    public String createOrderItemManual(
            @RequestParam Integer orderItemId,
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            @RequestParam Integer quantity,
            Model model) {
        try {
            String url = BASE_URL + "/orderitems/manual";

            Map<String, Object> request = new HashMap<>();
            request.put("orderItemId", orderItemId);
            request.put("orderId", orderId);
            request.put("itemId", itemId);
            request.put("quantity", quantity);

            Object response = restTemplate.postForObject(url, request, Object.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/update")
    public String updateOrderItem(
            @RequestParam Integer id,
            @RequestParam Integer orderItemId,
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            @RequestParam Integer quantity,
            Model model) {
        try {
            String url = BASE_URL + "/orderitems/" + id;

            Map<String, Object> request = new HashMap<>();
            request.put("orderItemId", orderItemId);
            request.put("orderId", orderId);
            request.put("itemId", itemId);
            request.put("quantity", quantity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, Object.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/delete")
    public String deleteOrderItem(@RequestParam Integer id, Model model) {
        try {
            String url = BASE_URL + "/orderitems/" + id;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, Object.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/getByOrderId")
    public String getOrderItemsByOrderId(@RequestParam Integer orderId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/getByItemId")
    public String getOrderItemsByItemId(@RequestParam Integer itemId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/item/" + itemId, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/getByOrderAndItem")
    public String getOrderItemsByOrderAndItem(
            @RequestParam Integer orderId,
            @RequestParam Integer itemId,
            Model model) {
        try {
            String url = BASE_URL + "/orderitems/order/" + orderId + "/item/" + itemId;
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/getTotalQuantity")
    public String getTotalQuantityByOrder(@RequestParam Integer orderId, Model model) {
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/total-quantity", Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/getTotalPrice")
    public String getTotalPriceByOrder(@RequestParam Integer orderId, Model model) {
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/total-price", Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/mostOrdered")
    public String getMostOrderedItems(Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/most-ordered", List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/deleteByOrderId")
    public String deleteOrderItemsByOrderId(@RequestParam Integer orderId, Model model) {
        try {
            String url = BASE_URL + "/orderitems/order/" + orderId;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, Object.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/updateQuantity")
    public String updateOrderItemQuantity(
            @RequestParam Integer id,
            @RequestParam Integer quantity,
            Model model) {
        try {
            String url = BASE_URL + "/orderitems/" + id + "/quantity?quantity=" + quantity;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, Object.class);

            model.addAttribute("single", response.getBody());
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orderitems/getDetailsByOrder")
    public String getOrderItemDetails(@RequestParam Integer orderId, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/order/" + orderId + "/details", List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orderitems/getMenuItemByOrderItemId")
    public String getMenuItemByOrderItemId(@RequestParam Integer id, Model model) {
        try {
            Object data = restTemplate.getForObject(
                    BASE_URL + "/orderitems/" + id + "/menuitem", Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @GetMapping("/orderitems")
    public String orderItemsPage() {
        return "orderitems";
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
        String url = BASE_URL + "/api/restaurants/search/name?name=" + name;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
        return "table";
    }

    // ✅ 5. Search By Address
    @PostMapping("/restaurant/searchByAddress")
    public String searchByAddress(@RequestParam String address, Model model) {
        String url = BASE_URL + "/api/restaurants/search/address?address=" + address;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
        return "table";
    }

    // ✅ 6. Search By Phone
    @PostMapping("/restaurant/searchByPhone")
    public String searchByPhone(@RequestParam String phone, Model model) {
        String url = BASE_URL + "/api/restaurants/search/phone?phone=" + phone;
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
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
        return "single";
    }

    // ✅ 9. Get Ratings of a Restaurant
    @PostMapping("/restaurant/ratings")
    public String getRatings(@RequestParam Integer id, Model model) {
    	try {
        String url = BASE_URL + "/api/restaurants/" + id + "/ratings";
        List<?> data = restTemplate.getForObject(url, List.class);
        model.addAttribute("data", data);
    	} catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            model.addAttribute("error", "No Rating found for restaurant ID: " + id);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());

        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table1";
    }

    @PostMapping("/restaurant/menuitems")
    public String getMenuItemsByRestaurantId(@RequestParam Integer id, Model model) {
        try {
            List<?> data = restTemplate.getForObject(
                    BASE_URL + "/api/restaurants/" + id + "/menuitems",
                    List.class
            );

            model.addAttribute("data", data);

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            model.addAttribute("error", "No menu items found for restaurant ID: " + id);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
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
        String response = restTemplate.postForObject(
                BASE_URL + "/api/ratings", dto, String.class);

        model.addAttribute("message", response);
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
        return "table1";
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
        return "table3";
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
        return "table1";
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

  @PostMapping("/customers/getAll")
  public String getAllCustomers(Model model) {

      Object response = safeGet(BASE_URL + "/customer/allcustomers", model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/customers/register")
  public String registerCustomer(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 Model model) {

      String url = BASE_URL + "/customer/register";

      Map<String, Object> request = new HashMap<>();
      request.put("customerName", name);
      request.put("customerEmail", email);
      request.put("customerPhone", phone);

      Object response = safePost(url, request, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  @PostMapping("/customers/getById")
  public String getCustomerById(@RequestParam Integer id, Model model) {

      Object response = safeGet(BASE_URL + "/customer/" + id, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  @PostMapping("/customers/byName")
  public String getCustomerByName(@RequestParam String name, Model model) {

      String url = BASE_URL + "/customer/by-name?name=" + name;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }


  @PostMapping("/customers/byEmail")
  public String getCustomerByEmail(@RequestParam String email, Model model) {

      String url = BASE_URL + "/customer/by-email?email=" + email;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  @PostMapping("/customers/byPhone")
  public String getCustomerByPhone(@RequestParam String phone, Model model) {

      String url = BASE_URL + "/customer/by-phone?phone=" + phone;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  @PostMapping("/customers/byNameEmail")
  public String getByNameAndEmail(@RequestParam String name,
                                 @RequestParam String email,
                                 Model model) {

      String url = BASE_URL + "/customer/by-name-email?name=" + name + "&email=" + email;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/customers/byNamePhone")
  public String getByNameAndPhone(@RequestParam String name,
                                 @RequestParam String phone,
                                 Model model) {

      String url = BASE_URL + "/customer/by-name-phone?name=" + name + "&phone=" + phone;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/customers/byEmailPhone")
  public String getByEmailAndPhone(@RequestParam String email,
                                  @RequestParam String phone,
                                  Model model) {

      String url = BASE_URL + "/customer/by-email-phone?email=" + email + "&phone=" + phone;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  //================= ADDRESS =================

  @PostMapping("/address/byId")
  public String getAddressByAddressId(@RequestParam Integer id, Model model) {

      String url = BASE_URL + "/address/by-id?id=" + id;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("single", response);
      }

      return "single";
  }

  @PostMapping("/address/byCity")
  public String getAddressByCityName(@RequestParam String city, Model model) {

      String url = BASE_URL + "/address/by-city?city=" + city;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byPostal")
  public String getAddressByPostal(@RequestParam String postalCode, Model model) {

      String url = BASE_URL + "/address/by-postal?postalCode=" + postalCode;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byState")
  public String getAddressByState(@RequestParam String state, Model model) {

      String url = BASE_URL + "/address/by-state?state=" + state;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byCityState")
  public String getByCityAndState(@RequestParam String city,
                                 @RequestParam String state,
                                 Model model) {

      String url = BASE_URL + "/address/by-city-state?city=" + city + "&state=" + state;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byCityPostal")
  public String getByCityAndPostal(@RequestParam String city,
                                  @RequestParam String postalCode,
                                  Model model) {

      String url = BASE_URL + "/address/by-city-postal?city=" + city + "&postalCode=" + postalCode;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byStatePostal")
  public String getByStateAndPostal(@RequestParam String state,
                                   @RequestParam String postalCode,
                                   Model model) {

      String url = BASE_URL + "/address/by-state-postal?state=" + state + "&postalCode=" + postalCode;

      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
      }

      return "table";
  }

  @PostMapping("/address/byCustomer")
  public String getAddressByCustomer(@RequestParam Integer customerId, Model model) {

      String url = BASE_URL + "/address/customer/" + customerId;
      Object response = safeGet(url, model);

      if (response != null) {
          model.addAttribute("data", response);
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

    // ================= ORDERS =================
    @PostMapping("/orders/getAll")
    public String getAllOrders(@RequestParam(required = false) String status, Model model) {
        try {
            String url = ORDERS_BASE;
            if (status != null && !status.isBlank()) {
                url += "?status=" + status;
            }
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/getById")
    public String getOrderById(@RequestParam Integer orderId, Model model) {
        if (invalidPositive(orderId)) {
            model.addAttribute("error", "Order ID must be greater than 0.");
            return "single";
        }
        try {
            Object data = restTemplate.getForObject(ORDERS_BASE + "/" + orderId, Object.class);
            model.addAttribute("single", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/create")
    public String createOrder(
            @RequestParam Integer customerId,
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) Integer driverId,
            @RequestParam String orderStatus,
            @RequestParam String orderDate,
            Model model) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("customerId", customerId);
            request.put("restaurantId", restaurantId);
            if (driverId != null) request.put("deliveryDriverId", driverId);
            request.put("orderStatus", orderStatus);
            request.put("orderDate", orderDate.contains("T") ? orderDate : orderDate + "T00:00:00");
            Object response = restTemplate.postForObject(ORDERS_BASE, request, Object.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/createManual")
    public String createOrderManual(
            @RequestParam Integer orderId,
            @RequestParam Integer customerId,
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) Integer driverId,
            @RequestParam String orderStatus,
            @RequestParam String orderDate,
            Model model) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("orderId", orderId);
            request.put("customerId", customerId);
            request.put("restaurantId", restaurantId);
            if (driverId != null) request.put("deliveryDriverId", driverId);
            request.put("orderStatus", orderStatus);
            request.put("orderDate", orderDate.contains("T") ? orderDate : orderDate + "T00:00:00");
            Object response = restTemplate.postForObject(ORDERS_BASE + "/manual", request, Object.class);
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/getByCustomer")
    public String getOrdersByCustomer(@RequestParam Integer customerId,
            @RequestParam(required = false) String status,
            Model model) {
        try {
            String url = ORDERS_BASE + "/customer/" + customerId;
            if (status != null && !status.isBlank()) url += "?status=" + status;
            model.addAttribute("data", restTemplate.getForObject(url, List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/getByRestaurant")
    public String getOrdersByRestaurant(@RequestParam Integer restaurantId,
            @RequestParam(required = false) String status,
            Model model) {
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId;
            if (status != null && !status.isBlank()) url += "?status=" + status;
            model.addAttribute("data", restTemplate.getForObject(url, List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/getByDriver")
    public String getOrdersByDriver(@RequestParam Integer driverId, Model model) {
        try {
            model.addAttribute("data", restTemplate.getForObject(ORDERS_BASE + "/driver/" + driverId, List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/getByDateRange")
    public String getOrdersByDateRange(@RequestParam String startDate, @RequestParam String endDate, Model model) {
        try {
            String url = ORDERS_BASE + "/date-range?startDate=" + startDate + "&endDate=" + endDate;
            model.addAttribute("data", restTemplate.getForObject(url, List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/getByRestaurantDateRange")
    public String getOrdersByRestaurantDateRange(@RequestParam Integer restaurantId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId + "/date-range?startDate=" + startDate + "&endDate=" + endDate;
            model.addAttribute("data", restTemplate.getForObject(url, List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/countByRestaurantStatus")
    public String countOrdersByRestaurantStatus(@RequestParam Integer restaurantId,
            @RequestParam String status,
            Model model) {
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId + "/count?status=" + status;
            model.addAttribute("single", restTemplate.getForObject(url, Object.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/update")
    public String updateOrder(@RequestParam Integer orderId,
            @RequestParam Integer customerId,
            @RequestParam Integer restaurantId,
            @RequestParam(required = false) Integer driverId,
            @RequestParam String orderStatus,
            @RequestParam String orderDate,
            Model model) {
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
            Object response = restTemplate.exchange(ORDERS_BASE + "/" + orderId, HttpMethod.PUT, entity, Object.class).getBody();
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/updateStatus")
    public String updateOrderStatus(@RequestParam Integer orderId, @RequestParam String status, Model model) {
        try {
            String url = ORDERS_BASE + "/" + orderId + "/status?status=" + status;
            Object response = restTemplate.exchange(url, HttpMethod.PATCH, HttpEntity.EMPTY, Object.class).getBody();
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/assignDriver")
    public String assignDriver(@RequestParam Integer orderId, @RequestParam Integer driverId, Model model) {
        try {
            String url = ORDERS_BASE + "/" + orderId + "/assign-driver?driverId=" + driverId;
            Object response = restTemplate.exchange(url, HttpMethod.PATCH, HttpEntity.EMPTY, Object.class).getBody();
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/cancel")
    public String cancelOrder(@RequestParam Integer orderId, Model model) {
        try {
            String url = ORDERS_BASE + "/" + orderId + "/cancel";
            Object response = restTemplate.exchange(url, HttpMethod.PATCH, HttpEntity.EMPTY, Object.class).getBody();
            model.addAttribute("single", response);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/delete")
    public String deleteOrder(@RequestParam Integer orderId, Model model) {
        try {
            restTemplate.delete(ORDERS_BASE + "/" + orderId);
            model.addAttribute("single", "Order deleted successfully");
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/pendingUnassigned")
    public String getPendingUnassignedOrders(Model model) {
        try {
            model.addAttribute("data", restTemplate.getForObject(ORDERS_BASE + "/pending-unassigned", List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/driverActive")
    public String getDriverActiveOrders(@RequestParam Integer driverId, Model model) {
        try {
            model.addAttribute("data", restTemplate.getForObject(ORDERS_BASE + "/driver/" + driverId + "/active", List.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }

    @PostMapping("/orders/restaurantRevenue")
    public String getRestaurantRevenue(@RequestParam Integer restaurantId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {
        try {
            String url = ORDERS_BASE + "/restaurant/" + restaurantId + "/revenue?startDate=" + startDate + "&endDate=" + endDate;
            model.addAttribute("single", restTemplate.getForObject(url, Object.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/customerSummary")
    public String getCustomerSummary(@RequestParam Integer customerId, Model model) {
        try {
            model.addAttribute("single", restTemplate.getForObject(ORDERS_BASE + "/customer/" + customerId + "/summary", Object.class));
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/getPaged")
    public String getPagedOrders(@RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model) {
        try {
            String url = ORDERS_BASE + "/paged?page=" + page + "&size=" + size;
            Object paged = restTemplate.getForObject(url, Object.class);
            if (paged instanceof Map<?, ?> map && map.get("items") instanceof List<?> items) {
                model.addAttribute("data", new ArrayList<>(items));
                return "table";
            }
            model.addAttribute("single", paged);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "single";
    }

    @PostMapping("/orders/items")
    public String getOrderItems(@RequestParam Integer id, Model model) {
        try {
            String url = ORDERS_BASE + "/" + id + "/items";
            List<?> data = restTemplate.getForObject(url, List.class);
            model.addAttribute("data", data);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
        }
        return "table";
    }
    //====================order================
    
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