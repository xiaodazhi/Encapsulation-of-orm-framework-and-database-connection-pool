package com.jdbc;

import java.util.Map;

/**
 * 用来指定查询的每条记录组成对应对象的策略规则
 */
public interface RowMapper<T> {
    /**将结果集对象中的一条记录  组成  对应的domain对象
     * 切记不要循环结果集  因为我们在JdbcUtil中已经循环了结果集
     * 参数是一个Map集合 而返回值就是一个T对象
     * 就是通过这个抽象方法将一个Map集合变成一个T对象
     */
    public T mapping(Map<String, Object> row) throws Exception;
}
