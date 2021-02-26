package com.jdbc;

import com.jdbc.connectionpool.ConnectionPool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 对外可以使用的Jdbc工具类
 * 提供更具体的增删改查方法  让用户认为这四个方法与增删改查相对应
 * 此时我们封装的其他三个模板都可以写成默认的权限修饰符
 * 仅在本包中可以访问  用户使用时只使用JdbcUtil这个类就可以完成所有操作
 * 表面上看我有insert  update  delete三个方法
 * 但其实底层只有一个方法
 * 还有一个问题  如果用户想要做的是insert  但是调用了update方法  发现insert也能做
 * 此时用户就会产生疑问
 * 所以为了避免这样的情况发生  为了更好的提升用户体验
 * 我们就让insert只能做新增  update只能做修改  delete只能做删除
 * 通过截取sql中最开始的关键字(首先要去掉前面没用的空格trim())  忽略大小写进行比较  调用equalsIgnoreCase()方法
 * 有没有其他的方法也可以做到大小写都可以进行比较呢
 * if(sql.toLowerCase().startsWith("insert"))
 * 我们先把用户传入的sql全部变成小写  再从头开始与insert进行比较  这样也可以
 */
public class JdbcUtil {
    private String driver;
    private String url;
    private String username;
    private String password;

    public JdbcUtil(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 此时我们用可变参数  即动态参数列表  就是一个动态数组
     * 长度可变  对于用户来讲  不传参  传多个参数都是可以的
     * 我们封装的底层内部全用数组
     */
    public int insert(String sql,Object...param){
        //if(sql.toLowerCase().startsWith("insert"))
        if(sql.trim().substring(0,6).equalsIgnoreCase("insert")) {
            JdbcUpdateTemplate t = new JdbcUpdateTemplate(
                    driver,
                    url,
                    username,
                    password);
            t.setConnectionPool(connectionPool);
            return (int) t.executeJdbc(sql, param);
            /**
             * 如果用户调用了insert方法  但是关键字写的不是insert
             * 我们就需要自定义一个异常抛给用户  提示用户sql有误
             */
        }else{
            throw new SqlFormatException("not a insert sql{"+sql+"}");
        }
    }
    public int update(String sql,Object...param){
        //if(sql.toLowerCase().startsWith("insert"))
        if(sql.trim().substring(0,6).equalsIgnoreCase("update")) {
            JdbcUpdateTemplate t = new JdbcUpdateTemplate(
                    driver,
                    url,
                    username,
                    password);
            t.setConnectionPool(connectionPool);
            return (int) t.executeJdbc(sql, param);
            /**
             * 如果用户调用了update方法  但是关键字写的不是update
             * 我们就需要自定义一个异常抛给用户  提示用户sql有误
             */
        }else{
            throw new SqlFormatException("not a update sql{"+sql+"}");
        }
    }
    public int delete(String sql,Object...param){
        //if(sql.toLowerCase().startsWith("insert"))
        if(sql.trim().substring(0,6).equalsIgnoreCase("delete")) {
            JdbcUpdateTemplate t = new JdbcUpdateTemplate(
                    driver,
                    url,
                    username,
                    password);
            t.setConnectionPool(connectionPool);
            return (int) t.executeJdbc(sql, param);
            /**
             * 如果用户调用了delete方法  但是关键字写的不是delete
             * 我们就需要自定义一个异常抛给用户  提示用户sql有误
             */
        }else{
            throw new SqlFormatException("not a delete sql{"+sql+"}");
        }
    }

    /**
     * 如果用户所写的sql语句中含有group by等关键字  进行分组...时
     * 所查询到的一条记录不能组成一个对象  那么该怎么办呢
     * 所以我们再提供一个方法  将来用户所查询的记录
     * 能组成对象的组成对象
     * 不能组成对象的我们就把List集合返回给用户
     */
    public List<Map<String,Object>> selectListMap(String sql,Object...param){
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")){
            JdbcQueryTemplate t = new JdbcQueryTemplate(
                    driver,
                    url,
                    username,
                    password
            );
            t.setConnectionPool(connectionPool);
            List<Map<String,Object>> rs = (List<Map<String,Object>>) t.executeJdbc(sql,param);
            return rs;
        }else{
            throw new SqlFormatException("not a select sql{"+sql+"}");
        }
    }

    /**
     * 如果用户多表关联  联合查询时  结果也不能组成对象
     * 所以我们再提供一个方法  就用于查询一个Map
     */
    public Map<String,Object> selectMap(String sql, Object...param) {
        List<Map<String,Object>> rows = selectListMap(sql, param);
        if (rows == null || rows.size() == 0) {
            return null;
        } else {
            return rows.get(0);
        }
    }

    /**
     * 我们使用策略模式实现ORM  即将查询的表数据装载到Java的实体类对象中
     * 此时我们需要其他人传入一个策略  由于可变参数只能放在参数列表的最后  所以我们将策略参数放在第二个
     * 但是所提供的策略不是随意的  策略必须得有一定的规则   接口/抽象类就是规则  我们创建一个接口RowMapper
     * 策略需要指定你要把一行数据组成什么类型的对象
     * 我们将由数据库记录加载成的所有对象装入一个集合中  返回这个集合
     */
    public <T> List<T> selectList(String sql, RowMapper<T> strategy, Object...param){
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")){
            JdbcQueryTemplate t = new JdbcQueryTemplate(
                    driver,
                    url,
                    username,
                    password
            );
            t.setConnectionPool(connectionPool);
            /**
             * 此时我们得到的就并不是结果集了  而是一个List<Map<String,Object>
             */
            List<Map<String,Object>> rs = (List<Map<String,Object>>) t.executeJdbc(sql,param);
            List<T> rows = new ArrayList<T>();
                try {
                    for(Map<String,Object> r:rs){
                        T row = strategy.mapping(r);
                        rows.add(row);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return rows;
        }else{
            throw new SqlFormatException("not a select sql{"+sql+"}");
        }
    }

    /**
     * 我们再给用户提供一个selectOne方法
     * 如果用户清楚的知道自己只查询一条记录  我们就不用给用户返回一个集合了
     * 不用用户一个一个的去取出来  简化用户的操作
     *
     */
    public <T> T selectOne(String sql, RowMapper<T> strategy, Object...param) {
        List<T> rows = selectList(sql, strategy, param);
        if (rows == null || rows.size() == 0) {
            return null;
        } else {
            return rows.get(0);
        }
    }

    /**
     * 此时我们不使用策略模式去将查询得到的记录转换成我们想要的对象
     * 而是利用反射将查询到的记录转换成我们想要的对象
     * 前面我们使用策略模式时  需要用户提供策略  如何去组装这个对象
     * 而我们利用反射  我们就不用用户给我们提供策略  而是通过反射找到这个domain实体类
     * 再通过反射的一些操作  将查询到的记录转换成这个domain实体对象的样子
     * 反射要比策略模式更好  但是策略模式也有它的优点  通过策略模式  你可以将查询到的记录变成任何你想要的类型  不一定局限于对象
     * 反射具有一定的局限性  只能转换成对象
     * 利用反射  我们需要将想要转换的类型以传参的方式告诉我
     *
     * 注意  这里根据sql语句的不同  分成许多种情况  有多种类型的对象
     * selectList("select * from t_car",Car class);  如果是这条语句  要转换的类型就是Car
     * selectList("select count(*) from t_car",Long class);  如果是这条语句  要转换的类型可能是long类型
     * selectList("select cname from t_car",String class);  如果时这条语句  要转换的类型可能是String类型
     *
     * 所以情况很多  我们只能将最常见的情况拿出来进行分析判断
     * String  Int  Integer  对象  double  Double  long  Long等等
     *
     */
    public <T> List<T> selectList(String sql,Class<T> type, Object...param){
        if(sql.trim().substring(0,6).equalsIgnoreCase("select")) {
            JdbcQueryTemplate t = new JdbcQueryTemplate(
                    driver,
                    url,
                    username,
                    password
            );
            t.setConnectionPool(connectionPool);
            List<Map<String, Object>> rs = (List<Map<String, Object>>) t.executeJdbc(sql, param);
            List<T> rows = new ArrayList<T>();
            try {
                /**orm 将查询表中数据装载到Java实体类中
                 */
                for (Map<String, Object> r : rs) {
                    Object row = null;
                    if (type == int.class || type == Integer.class) {
                        /**如果所要转换的类型是Int  或  Integer
                         * 那么说明  只有一个int类型的数据
                         * 我就将这个数据取出来  放入Collection集合中
                         * Collection是所有集合的父接口
                         * 遍历后要把它装入rows集合
                         * 注意 这个循环只做一次  因为只有一个数据
                         */
                        Collection cs = r.values();
                        for (Object c : cs) {
                            /**
                             * 我们需要将取出的数据强转成Integer类型
                             * row = (Integer)c;
                             * 如果数据是一个long类型呢  我们无法进行强转
                             * 我们就利用Number这个类做为一个跳板
                             * 调用intValue()方法 将long类型转为Integer类型
                             */
                            row = ((Number) c).intValue();
                        }
                    } else if (type == long.class || type == Long.class) {
                        Collection cs = r.values();
                        for (Object c : cs) {
                            row = ((Number) c).longValue();
                        }
                    }else if (type == double.class || type == Double.class) {
                        Collection cs = r.values();
                        for (Object c : cs) {
                            row = ((Number) c).doubleValue();
                        }
                    } else if (type == String.class) {
                        Collection cs = r.values();
                        for (Object c : cs) {
                            row = (String) c;
                            /**不能使用toString  如果是一个null  toString就会报空指针异常
                             * 如果null强转成String就不会报错
                             */
                        }
                    } else {
                        /**
                         * 组成domain实体类对象
                         * 通过反射来创建对象
                         */
                        row = type.newInstance();//Car car = new Car();
                        /**
                         * 从封装特性的角度而言  更推荐通过set方法  找到对应属性
                         * 通过set方法来给属性赋值
                         * 通过反射获得实体中的所有属性名  Map中找到与之同名的表数据
                         * 为其赋值
                         */
                        Method[] ms = type.getMethods();//得到所有属性的set方法
                        for(Method m:ms){
                            String mname = m.getName();//得到所有的方法名
                            if(mname.startsWith("set")){
                                //证明是一个set方法  -->找到对应的属性   setCno()-->cno
                                //通过set方法名如何找到属性名  set往后就是属性名  我们只需将第一个字母变小写就得到了属性名
                                mname = mname.substring(3);//截取掉set
                                mname = mname.toLowerCase();
                                Object value = r.get(mname);
                                if(value==null){//说明当前对象属性没有对应的表数据
                                    continue;//继续判断下一个属性
                                }else {
                                    //当前的属性有对应的表数据  使用set方法赋值
                                    //使用反射调用方法并赋值
                                    //注意  这里还存在一个类型转换问题
                                    Class p = m.getParameterTypes()[0];//获取方法的参数类型
                                    if(p==int.class || p==Integer.class){
                                        m.invoke(row,((Number)value).intValue());//car.setCno();  将car对象放进来 这就表示调用car对象的set方法
                                    }else if(p==long.class || p==Long.class){
                                        m.invoke(row,((Number)value).longValue());
                                    }else if(p==double.class || p==Double.class){
                                        m.invoke(row,((Number)value).doubleValue());
                                    }else if(p==String.class){
                                        m.invoke(row,(String)value);
                                    }
                                }
                            }
                        }
                    }
                    rows.add((T) row);
                }
                return rows;
            } catch(Exception e){
                e.printStackTrace();
            }
            return rows;
        }else{
            throw new SqlFormatException("not a select sql{" + sql + "}");
        }
    }
    public <T> T selectOne(String sql ,Class<T> type, Object...param){
        List<T> rows = selectList(sql,type,param);
        if(rows == null || rows.size() == 0){
            return null ;
        }else{
            return rows.get(0) ;
        }
    }
    private ConnectionPool connectionPool;

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}


