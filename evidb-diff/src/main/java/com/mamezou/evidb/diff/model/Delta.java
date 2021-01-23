package com.mamezou.evidb.diff.model;

import java.util.Collection;

import lombok.Data;

@Data
public class Delta {
	private String name;
	private int before;
	private int after;
	private int create;
	private int update;
	private int delete;
	private Collection<Collection<Item>> details;
}
