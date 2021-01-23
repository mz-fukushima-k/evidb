package com.mamezou.evidb.sqlgen.thymeleaf.dialect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

import com.mamezou.evidb.sqlgen.thymeleaf.util.StringHelper;

public class HelperDialect extends AbstractDialect implements IExpressionObjectDialect {

	private Map<String, Object> expressionObjects;

	public HelperDialect() {
		super("HelperDialect");
		this.expressionObjects = new HashMap<>();
		expressionObjects.put("stringHelper", new StringHelper());
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new IExpressionObjectFactory() {
			@Override
			public Set<String> getAllExpressionObjectNames() {
				return expressionObjects.keySet();
			}

			@Override
			public Object buildObject(IExpressionContext context, String expressionObjectName) {
				return expressionObjects.get(expressionObjectName);
			}

			@Override
			public boolean isCacheable(String expressionObjectName) {
				return false;
			}
		};
	}

}
