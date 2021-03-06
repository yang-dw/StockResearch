package com.kers.stock.bean;

public class StockTag {

	private String tagType;
	private String tagName;
	private String stockCode;
	private String stockName;
	private String tagExpTime;// tag有效期
	private double tagFuNum;

	public String getTagType() {
		return tagType;
	}

	public void setTagType(String tagType) {
		this.tagType = tagType;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

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

	public String getTagExpTime() {
		return tagExpTime;
	}

	public void setTagExpTime(String tagExpTime) {
		this.tagExpTime = tagExpTime;
	}

	@Override
	public String toString() {
		return "StockTag [tagType=" + tagType + ", tagName=" + tagName + ", stockCode=" + stockCode + ", stockName="
				+ stockName + ", tagExpTime=" + tagExpTime + "]";
	}

	public double getTagFuNum() {
		return tagFuNum;
	}

	public void setTagFuNum(double tagFuNum) {
		this.tagFuNum = tagFuNum;
	}

}
