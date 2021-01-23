package com.mamezou.evidb.dump.dao;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.seasar.doma.Dao;
import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.jdbc.builder.SelectBuilder;

import com.mamezou.evidb.dump.config.AppConfig;

@Dao(config = AppConfig.class)
public interface GenericDao {

	default <R> R selectAll(String query,
			Function<Stream<Map<String, Object>>, R> mapper) {

		AppConfig config = AppConfig.singleton();

		SelectBuilder builder = SelectBuilder.newInstance(config);

		return builder.sql(query).streamMap(MapKeyNamingType.NONE, mapper);

	}

}
