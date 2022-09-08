package com.feedbackBackendApp.services;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.feedbackBackendApp.data.categoryresponse.ClassificationCategory;

@Service
public class CategoryClassificationService {

	@Value("${sentiment.confidence.value}")
	private double confidence;
	
	public static final Set<String> categoryList = new HashSet<String>();
	static {
		String[] categories = {
				"/Food & Drink"
				,"/Food & Drink/Beverages"
				,"/Food & Drink/Beverages/Alcoholic Beverages"
				,"/Food & Drink/Beverages/Coffee & Tea"
				,"/Food & Drink/Beverages/Juice"
				,"/Food & Drink/Beverages/Soft Drinks"
				,"/Food & Drink/Cooking & Recipes"
				,"/Food & Drink/Cooking & Recipes/BBQ & Grilling"
				,"/Food & Drink/Cooking & Recipes/Desserts"
				,"/Food & Drink/Cooking & Recipes/Soups & Stews"
				,"/Food & Drink/Food"
				,"/Food & Drink/Food & Grocery Retailers"
				,"/Food & Drink/Food/Baked Goods"
				,"/Food & Drink/Food/Breakfast Foods"
				,"/Food & Drink/Food/Candy & Sweets"
				,"/Food & Drink/Food/Grains & Pasta"
				,"/Food & Drink/Food/Meat & Seafood"
				,"/Food & Drink/Food/Snack Foods"
				,"/Food & Drink/Restaurants"
				,"/Food & Drink/Restaurants/Fast Food"
				,"/Food & Drink/Restaurants/Pizzerias"
				,"/Food & Drink/Restaurants/Restaurant Reviews & Reservations"		
				,"/Shopping/Consumer Resources/Product Reviews & Price Comparisons"
		};
		for(String valueString : categories) {
			categoryList.add(valueString);
		}
	}
	
	
	public boolean checkCategoryMatch(List<ClassificationCategory> messagecategoty) {
		
		if(messagecategoty == null || messagecategoty.isEmpty())
			return false;
		
		for(ClassificationCategory categoryClass : messagecategoty) {
			if(categoryClass.getConfidence()>=confidence && categoryList.contains(categoryClass.getName()))
			return true;
		}
		
		return false;
		
	}
	
}
