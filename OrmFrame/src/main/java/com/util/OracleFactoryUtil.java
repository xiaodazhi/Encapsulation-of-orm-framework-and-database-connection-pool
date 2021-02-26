package com.util;

import com.jdbc.JdbcFactory;

/**
 * MysqlFactoryUtil:
 */
public class OracleFactoryUtil {

    private static JdbcFactory factory;
    static {
        factory = new JdbcFactory("oracle.properties");
    }

    public static JdbcFactory getFactory() {
        return factory;
    }
}
