package com.jdbc;

import java.sql.SQLException;

/**
 * JDBC增删改模板  继承JdbcTemplate模板并实现第五个方法
 * 此时我们需要stmt预处理对象  所以我们将父类中stmt属性写成protected
 * 子类也能够使用它
 */

class JdbcUpdateTemplate extends JdbcTemplate {
    /**
     * @param driver
     * @param url
     * @param username
     * @param password
     */
    /**
     *为什么还会报错  因为我们创建这个子类时  会创建父类对象
     * 但是我们在父类中没有提供无参的构造方法
     * 所以我们需要提供子类的有参构造方法  让子类传递这几个参数  通过父类有参构造方法去创建父类对象
     */
    public JdbcUpdateTemplate(String driver, String url, String username, String password) {
        super(driver, url, username, password);
    }

    @Override
    protected Object five() throws SQLException {
        return stmt.executeUpdate();
    }
}
