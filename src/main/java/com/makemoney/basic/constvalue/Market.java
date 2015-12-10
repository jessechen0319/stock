package com.makemoney.basic.constvalue;

public enum Market {
	ShangHai("sh"), ShenZhen("sz"), ChuangYeBan("sz");
	private String name;
	private Market(String name){
		this.setName(name);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
