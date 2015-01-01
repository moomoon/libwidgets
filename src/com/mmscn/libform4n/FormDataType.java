package com.mmscn.libform4n;

/**
 * 从表单构建的数据类型
 * 
 * @author joey.jia@mmscn.com
 * 
 */
public enum FormDataType {
	/**
	 * 表单主数据
	 */
	MainJson, /**
	 * 表单附加体征数据
	 */
	ExtraSignJson, /**
	 * 表单附加跌倒评估数据
	 */
	ExtraFallJson, /**
	 * 表单附加压疮评估数据
	 */
	ExtraPressureSoreJson
}
