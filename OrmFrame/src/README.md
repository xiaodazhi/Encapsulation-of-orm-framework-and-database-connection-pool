此框架是模仿Mybatis框架自己封装的半自动的ORM框架  支持注解(使用动态代理模式)  
支持比较复杂的sql语句  并且能够根据条件将结果集中的数据组装成一些类型返回
并提供一种新的语法  cno = #{cno}  代替原生JDBC中的?  使domain中的属性名与数据库中的column名相对应
增强可读性
可支持多种数据库  例如Mysql数据库  Oracle数据库等
并且封装了数据库连接池  使连接可以复用  优化性能
与MyBatis框架的功能及使用方法类似

使用到了反射  注解  集合  异常等基本所有Java基础中的知识
及面向对象的设计思想
及Mysql数据库  jdbc操作等基本知识
及模板模式  工厂模式(及工厂的单实例管理)  策略模式  动态代理模式  静态代理模式  缺省适配器模式等多种设计模式
及并发编程中的解决线程安全问题的synchronized关键字等

例如
当用户所写的sql语句中含有group by等关键字  进行分组...时  所查询到的一条记录不能组成一个对象  
我们提供了方法selectListMap
将来用户所查询的记录  组合成List<Map<String,Object>>并返回给用户
如果用户多表关联  联合查询时  结果也不能组成对象  
所以我们提供一个方法selectMap  
将来用户所查询的记录  组合成Map<String,Object>并返回给用户

我们还支持用户自己传入类型参数  根据用户传入的类型将结果集中的数据转换成对应的类型  
并将数据放在容器中返回给用户

并支持策略模式  用户实现RowMapper接口  并自己写出将结果集中的数据组合成对象的策略
将策略作为参数传入方法  框架就可根据用户提供的策略来组成对象
例如

```java
class CarMapper implements RowMapper<Car> {
    @Override
    public Car mapping(Map<String,Object> rs) throws Exception {
        Integer cno = (Integer) rs.get("cno");
        String cname = (String) rs.get("cname");
        String color = (String) rs.get("color");
        Integer price = (Integer) rs.get("price");
        return new Car(cno,cname,color,price);
    }
}
```



如需使用  请将工程中的jdbc包  util包放在一起  打成Jar包  放入工程中即可使用
注意  使用此框架请参照框架使用方法进行使用(这里只介绍简单的使用) 

框架使用方法1(非注解)

以Mysql数据库为例(Oracle数据库新建配置文件oracle.properties)

如果是maven项目  请在resources目录下新建mysql.properties配置文件  
如果不是maven项目  请在工程src目录下新建mysql.properties配置文件
其中写入
username  password  url  driver  total  maxWait  minIdle
total  maxWait  minIdle这三个参数与封装的数据库连接池有关
total  数据库连接池中最大连接数
maxWait  连接最大等待时间  此参数是数据库连接池中目前已无可用的连接时的等待最大等待连接的时间  
配置此参数是为了等待连接池中可用的连接  当超过等待时间  则会报错
minIdle  数据库连接池中的最小空闲连接数  此参数是连接池扩容的条件
当数据库中的连接达到最小空闲连接数时  连接池将自动扩容  每次扩容10个连接

示例
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/jdbc05?characterEncoding=utf8&serverTimezone=UTC
username=root
password=root
total=120
maxWait=3000
minIdle=2

新增记录 

请在dao中设计新增方法
请将domain对象作为参数传入方法
并使用新的语法#{}写出sql语句
使用mysql数据库对应的MysqlFactoryUtil工具调用getFactory()获得一个JdbcFactory工厂对象
并调用getSession()方法获得sqlSession对象
使用sqlSession对象调用insert()方法  将sql语句和domain对象作为参数传入
如果需要返回值  则使返回值为int类型并接收返回值  如果不需要  则不用设计返回值

测试
创建domain对象作为新增方法参数
dao对象调用新增方法并执行  
此时  一条新的记录就会新增进表中

示例
public int save (Car car) {
    String sql = "insert into t_car values(null, #{cname}, #{color}, #{price})";
    SqlSession sqlSession = MysqlFactoryUtil.getFactory().getSession() ;
    int count = sqlSession.insert(sql, car);
    return count;
}
测试
CarDao2 dao = new CarDao2();
Car car = new Car(null, "benz", "black", 400000);
int count = dao.save(car);

修改记录 
与新增记录类似
public int update (Car car) {
    String sql = "update t_car set color = #{color} where cno = #{cno}" ;
    SqlSession session = MysqlFactoryUtil.getFactory().getSession() ;
    int count = session.update(sql, car);
    return count;
}  
测试
CarDao dao = new CarDao();
Car car = new Car(1,"null","red", 0);
int count = dao.update(car);

删除记录  传入主键id
public int delete (int cno) {
    String sql = "delete from t_car where cno = #{cno}" ;
    SqlSession session = MysqlFactoryUtil.getFactory().getSession() ;
    int count = session.delete(sql, cno);
    return count;
}
测试
CarDao dao = new CarDao();
int count = dao.delete(1);

查询单条  查询多条
需要用户自己传入将结果集中的数据转换成什么类型  例如Car.class

public List<Car> findAll (String cname) {
    String sql = "select * from t_car where cname = #{cname}";
    SqlSession sqlSession = MysqlFactoryUtil.getFactory().getSession() ;
    List<Car> cars = sqlSession.selectList(sql, cname, Car.class);
    for (Car car : cars) {
        System.out.println(car);
    }
    return cars;
}
测试
CarDao dao = new CarDao();
dao.findAll("bmw3");


框架使用方法2(基于注解)

一旦使用注解  则dao层就没有了存在的必要  
我们使dao层变成接口

查询操作对应@Select注解
修改操作对应@Update注解
删除操作对应@Delete注解
新增操作对应@Insert注解

在注解中写对应的sql语句

示例


```java
public interface CarDao {
    @Insert("insert into t_car values(null, #{cname}, #{color}, #{price})")
    public int save(Car car);

    @Delete("delete from t_car where cno = #{cno}")
    public int delete(int cno);

    @Select("select * from t_car")
    public List<Car> findAll();

    @Select("select * from t_car where cno = #{cno}")
    public Car findById(int cno) ;
}
```



测试基于注解
CarDao3 dao = MysqlFactoryUtil.getFactory().getSession().createDaoImpl(CarDao3.class);
Car car = new Car(null, "benz", "black", 400000);
dao.save(car);
dao.delete(8);
List<Car> cars = dao.findAll();
for (Car car1 : cars) {
    System.out.println(car1);
}



其他功能使用方法未写入此文档中  如果要使用其他功能  请仔细阅读此文档  并阅读部分源码  查看方法的参数和返回值 

即能够使用其他功能





