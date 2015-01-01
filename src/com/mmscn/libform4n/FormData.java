package com.mmscn.libform4n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <strong> 一个类中每一种{@link FormDataType}仅可使用一次。 </strong>
 * 
 * @author joey.jia@mmscn.com
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormData {
	/**
	 * @return {@link FormDataType}
	 */
	public FormDataType value();

}
