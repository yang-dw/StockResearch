package com.kers.stock.bean;

import java.io.Serializable;

import com.kers.stock.utils.TimeUtils;

public class StockOptionalInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String stockCode;
	
	private String stockName;
	//涨幅
	private double percent;
	//现在价格
    private double price;
	//加入理由
	private String text = "";	
	//加入时价格
	private double oldPrice;
	
	private String tag;
	
	private String zb;
	
	//类型
	private int addType = 0;
	
	//加入指标类型 1:十字星
	private int jrzblt = 0;
	
	private String date = TimeUtils.getDate(TimeUtils.DATE_FORMAT);

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getOldPrice() {
		return oldPrice;
	}

	public void setOldPrice(double oldPrice) {
		this.oldPrice = oldPrice;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getZb() {
		return zb;
	}

	public void setZb(String zb) {
		this.zb = zb;
	}

	public int getJrzblt() {
		return jrzblt;
	}

	public void setJrzblt(int jrzblt) {
		this.jrzblt = jrzblt;
	}

	public int getAddType() {
		return addType;
	}

	public void setAddType(int addType) {
		this.addType = addType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	
	
	
}
