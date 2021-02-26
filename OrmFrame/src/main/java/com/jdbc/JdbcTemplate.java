package com.jdbc;

import com.jdbc.connectionpool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

abstract class JdbcTemplate {
    /**
     * 此时  我们只是把JDBC的流程进行了封装
     * 但是我们的封装还是存在缺陷
     * 第一个缺陷  用户必须主动的去区分自己所要执行的sql操作到底是增删改还是查询
     * 再去创建增删改模板  或者  查询模板
     * 第二个缺陷  用户执行查询模板后  会有一个返回值 ResultSet结果集
     * 用户还需要去自己处理这个结果集  这不是封装的目的
     * 所以我们在JDBC流程封装的基础上还要再进行封装  JdbcUtil
     * 尽量减少用户的操作
     */
    /**
     * 而针对第五步不具体的操作  我们需要提供两个具体的模板JdbcUpdateTemplate   JdbcQueryTemplate  去继承并实现它
     */

    private String driver;
    private String url;
    private String username;
    private String password;
    private Connection conn;
    protected PreparedStatement stmt;
    private ResultSet rs;

    /**
     * @param driver
     * @param url
     * @param username
     * @param password
     * 这四个参数需要用户传进来  所以我们提供有参构造方法
     */
    public JdbcTemplate(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 对外提供一个executeJdbc()方法
     * 任何人想执行JDBC操作  只需要调用executeJdbc()方法
     */
    public Object executeJdbc(String sql,Object[] param){
        try{
            one();
            two();
            three();
            four(sql,param);
            Object result = five();
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                six();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 引入jar文件
     */
    private void one(){}
    /**
     * 加载驱动
     Class.forName("com.mysql.cj.jdbc.Driver");
     此时我们不能直接将驱动类写为com.mysql.cj.jdbc.Driver
     因为我们封装的是JDBC  连接Java的不一定是MySQL数据库  还有可能是其他的数据库oracle  redis等
     所以我们要把驱动类写成动态的  我们让用户将驱动类通过变量传进来
     */
    private void two(){
        //注意  创建连接需要加载驱动
        //我们在自己封装的连接池中已经加载了驱动
        //这里就无需再加载驱动了
//        try {
//            Class.forName(driver);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 创建连接
     */
    private void three(){
        try {
            //conn = DriverManager.getConnection(url,username,password);
            //使用自己封装的连接池来获得连接
            conn = connectionPool.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建预处理对象  需要用到conn连接  所以我们把连接写成一个成员变量
     * sql我们也不能定死  我们需要用户给我们传进来sql  我们再来处理sql
     * 另外  sql中的参数也需要用户传递给我们
     * 由于参数的数量不确定  所以我们用一个数组来装它们
     * 由于参数的类型我们也不确定  所以我们用Object类型的数组
     * 为什么不用泛型  T<T>   虽然这里的泛型可以代表任何类型
     * 但是  当用户给我一个参数的时候  这个泛型类型就已经确定了
     * 如果第二个参数的类型与第一个不同  第二个参数就无法处理了
     * 由于我不确定有几个参数 所以我们要用循环来给这几个参数赋值
     * 我们赋值也要用Object类型
     */
    private void four(String sql,Object[] param) {
        try {
            stmt = conn.prepareStatement(sql);
            for(int i = 0 ; i < param.length ; i ++){
                stmt.setObject(i + 1,param[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行sql  需要用到命令集对象stmt  所以将stmt也写成成员变量
     * 这里我们发现出现了问题  既可以执行增删改操作  又可以执行查询操作
     * 那么我们到底执行什么操作
     * 取决于的是用户给我们的sql
     * 所以我们将这个方法写成抽象方法  而抽象方法必须放在抽象类里  所以这个类需要变成抽象类
     * 这就正好印证了模板模式  为什么还会报错  因为私有方法不能被继承
     * 而抽象方法必须被继承  所以我们要设置权限符为protected
     * 不论执行的是增删改还是查询  都会有返回值  如果是查询  返回值是一个结果集ResultSet
     * 如果是增删改  返回值是int  记录修改的条数  那么我们最终返回什么呢  Object
     */
    protected abstract Object five() throws SQLException;

    /**
     * 各种关闭
     * 如果第五步产生了结果集  我们也需要关闭结果集
     * 所以结果集也需要写成成员变量
     * 做严谨的判断  创建出来才关闭  避免空指针异常
     */
    private void six() throws SQLException{
        if(rs!=null) {
            rs.close();
        }
        if(stmt!=null){
            stmt.close();
        }
        if(conn!=null) {
            conn.close();
        }
    }

    private ConnectionPool connectionPool;

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}
