package com.jdbc;

import com.jdbc.connectionpool.ConnectionPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JdbcFactory:
 * 负责创建JdbcUtil工具
 */
public class JdbcFactory {

    public JdbcFactory(){
        /**
         * 提供无参构造器
         * 默认读取src下的db.properties文件
         */
        this("db.properties");
    }

    /**
     * 如果直接给我文件名
     * 我们人为的规定读取的文件都放置在src下
     * new JdbcFactory("db.properties");
     * @param fileName
     */
    public JdbcFactory(String fileName){
        /*
            简单的理解为获得src目录下的文件路径
            其实获得的是classpath目录下的文件
         */
        String path = Thread.currentThread().getContextClassLoader().getResource(fileName).getPath();
        File file = new File(path);
        //调用readFile方法读取文件
        readFile(file);
    }

    /**
     * 使用者根据程序  自己获得配置文件  交给工厂读取
     * File file = new File("path");
     * new JdbcFactory(file);
     */

    /**
     * 如果不给我文件名  那就传入文件路径  我们去读文件
     * @param file
     */
    public JdbcFactory(File file){
        readFile(file);
    }

    private String driver;
    private String url;
    private String username;
    private String password;

    private Integer total;
    private Integer maxWait;
    private Integer minIdle;
    //设计一个方法  目的是为了读取文件
    private void readFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            Properties properties = new Properties();
            //使用Properties类下的load方法  可以将inputStream流中的数据以key-value的形式存入Map集合中
            properties.load(inputStream);
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");

            String total = properties.getProperty("total");
            String maxWait = properties.getProperty("maxWait");
            String minIdle = properties.getProperty("minIdle");
            if (total != null && !"".equalsIgnoreCase(total)) {
                this.total = Integer.parseInt(total);
            }

            if (maxWait != null && !"".equalsIgnoreCase(maxWait)) {
                this.maxWait = Integer.parseInt(maxWait);
            }

            if (minIdle != null && !"".equalsIgnoreCase(minIdle)) {
                this.minIdle = Integer.parseInt(minIdle);
            }

            //调用方法  创建连接池
            this.createConnectionPool();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private ConnectionPool connectionPool;

    //创建连接池方法
    private void createConnectionPool () throws SQLException, ClassNotFoundException {
        connectionPool = new ConnectionPool(driver, url, username, password);
        connectionPool.setTotal(total);
        connectionPool.setMaxWait(maxWait);
        connectionPool.setMinIdle(minIdle);
    }


    public JdbcUtil getUtil(){
        //driver  url等信息我们想要让工厂去读文件
        JdbcUtil util = new JdbcUtil(driver,url,username,password);
        util.setConnectionPool(connectionPool);
        return util;
    }

    public SqlSession getSession(){
        SqlSession sqlSession = new SqlSession(driver,url,username,password,connectionPool);
        return sqlSession;
    }
}
