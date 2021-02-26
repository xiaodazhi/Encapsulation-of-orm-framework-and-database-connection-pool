package com.util;

import com.jdbc.JdbcFactory;

/**
 * MysqlFactoryUtil:
 */
public class MysqlFactoryUtil {

    private static JdbcFactory factory;
    static {
        factory = new JdbcFactory("mysql.properties");
    }

    public static JdbcFactory getFactory() {
        return factory;
    }
}
