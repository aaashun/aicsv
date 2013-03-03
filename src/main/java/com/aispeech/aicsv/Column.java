package com.aispeech.aicsv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

	/**
	 * (Optional) The name of the column. Defaults to the property or field name.
	 * 
	 * this name can not be duplicate
	 */
	public String name() default "";

	/**
	 * (Optional) Whether the csv column is nullable.
	 */
	public boolean nullable() default true;

	/**
	 * (Optional) Whether the csv column is unique.
	 * 
	 * @return
	 */
	public boolean unique() default false;

	/**
	 * (Optional) The value should match the given regex expression
	 * 
	 * @return
	 */
	public String matches() default ".*";

	public boolean trim() default true;
}