package com.foodservice.frontend.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RestaurantsDTO {
	
	    private Integer restaurantId;
	    private String restaurantName;
	    private String restaurantAddress;
	    private String restaurantPhone;
	
}
