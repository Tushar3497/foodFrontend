package com.foodservice.frontend.controller;


import lombok.Data;

@Data
public class Rating {

	
    private Integer rating;        // rating value
    private String review;         // if you have
    private Integer restaurantId;
    private Integer orderId;

}