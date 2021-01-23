package com.mamezou.evidb.dump.config;

import javax.sql.DataSource;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.dialect.StandardDialect;
import org.seasar.doma.jdbc.tx.LocalTransactionDataSource;
import org.seasar.doma.jdbc.tx.LocalTransactionManager;
import org.seasar.doma.jdbc.tx.TransactionManager;

import com.mamezou.evidb.dump.model.Settings;

public class AppConfig implements Config {

	public static final AppConfig INSTANCE = new AppConfig();

	private final Dialect dialect;

	private final LocalTransactionDataSource dataSource;

	private final TransactionManager transactionManager;

	private AppConfig() {

		Settings settings = Settings.get();

		dialect = new StandardDialect();

		dataSource = new LocalTransactionDataSource(settings.getUrl(), settings.getUser(), settings.getPassword());

		transactionManager = new LocalTransactionManager(
				dataSource.getLocalTransaction(getJdbcLogger()));

	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public static AppConfig singleton() {
		return INSTANCE;
	}

}
