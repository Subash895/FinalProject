package com.smartCity.Web.Model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "market_rates")
public class MarketRate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String productName;
	private Double price;
	private String unit;
	private LocalDate priceDate;

	public MarketRate() {
	}

	public MarketRate(String productName, Double price, String unit) {
		this.productName = productName;
		this.price = price;
		this.unit = unit;
		this.priceDate = LocalDate.now();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public LocalDate getPriceDate() {
		return priceDate;
	}

	public void setPriceDate(LocalDate priceDate) {
		this.priceDate = priceDate;
	}
}
