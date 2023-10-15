package com.stc.inspireu.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.stc.inspireu.utils.AuthorizeUtil;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Authorize {

	public String[] roles() default {};

	public String permission() default "";

	public String resource() default AuthorizeUtil.url;

	public String resourceId() default "";

	public boolean checkRoleUrlScope() default true;

	public String logic() default AuthorizeUtil.roleOrPermission;

	public String parentId() default "";

	public String grandParentId() default "";

	public String parentResource() default "";

	public String grandParentResource() default "";

}
