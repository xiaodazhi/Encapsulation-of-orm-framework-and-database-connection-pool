package com.jdbc;

import com.jdbc.annocations.Delete;
import com.jdbc.annocations.Insert;
import com.jdbc.annocations.Select;
import com.jdbc.annocations.Update;
import com.jdbc.connectionpool.ConnectionPool;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * SqlSession:
 * 实现Jdbc交互
 * 与JdbcUtil作用相同
 * 当前对象针对于一种新的sql语法
 * insert into t_car values(null, #{cname}, #{color}, #{price})
 */
public class SqlSession {

    private JdbcUtil util;
    public SqlSession(String driver,String url,String username,String password, ConnectionPool connectionPool){
        util = new JdbcUtil(driver,url,username,password);
        util.setConnectionPool(connectionPool);
    }

    public int insert(String sql, Object param){
        /*
            传进来的sql 是 "insert into t_car values(null,#{cname},#{color},#{price})";
            调用Util.insert()需要传递的sql: "insert into t_car values(null,?,?,?)"

            传进来的参数是一个car对象
            调用util.insert()需要传递一个数组  数组中包括{car.cname,car.color,car.price}

            我们在这里需要做的就是sql与参数的处理
            SqlHandler去做这件事  最后将sql和参数组装成一个SqlAndParam对象返回
         */
        SqlAndParam sop = SqlHandler.execute(sql,param);
        return util.insert(sop.sql,sop.params.toArray());
    }

    public int insert(String sql){
        return util.insert(sql);
    }

    public int update(String sql,Object param){
        SqlAndParam sop = SqlHandler.execute(sql,param);
        return util.update(sop.sql,sop.params.toArray());
    }

    public int update(String sql){
        return util.update(sql);
    }

    public int delete(String sql,Object param){
        SqlAndParam sop = SqlHandler.execute(sql,param);
        return util.delete(sop.sql,sop.params.toArray());
    }

    public int delete(String sql){
        return util.delete(sql);
    }

    public List<Map<String,Object>> selectListMap(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectListMap(sp.sql,sp.params.toArray());
    }

    public Map<String,Object> selectMap(String sql , Object param){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectMap(sp.sql,sp.params.toArray());
    }

    public <T> List<T> selectList(String sql , Object param , Class<T> type){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectList(sp.sql,type,sp.params.toArray());
    }

    public <T> T selectOne(String sql , Object param , Class<T> type){
        SqlAndParam sp = SqlHandler.execute(sql,param) ;
        return util.selectOne(sp.sql,type,sp.params.toArray());
    }

    public List<Map<String,Object>> selectListMap(String sql ){
        return util.selectListMap(sql);
    }

    public Map<String,Object> selectMap(String sql){
        return util.selectMap(sql);
    }

    public <T> List<T> selectList(String sql ,Class<T> type){
        return util.selectList(sql,type);
    }

    public <T> T selectOne(String sql, Class<T> type){
        return util.selectOne(sql,type);
    }

    /**
     * 根据指定的dao接口规则，创建其对应的实现类
     * @param daoInterface
     * @param <T>
     * @return
     */
    //getMapper
    public <T> T createDaoImpl(Class<T> daoInterface){
        //匿名内部类
        return (T) Proxy.newProxyInstance(//Proxy帮我们创建动态代理实例  需要三个参数
            daoInterface.getClassLoader(), //在dao接口的同包下创建代理类
            new Class[]{daoInterface}, //创建代理类也实现指定的接口 class Proxy implements CarDao3
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    //当业务程序*仿佛*调用dao接口的方法时，其实调用的就是当前invoke方法
                    //所以从业务而言，我们希望调用接口中的方法时执行什么操作，就在当前的invoke方法中写相应的代码
                    /*
                        proxy 当前产生的代理对象，只需要知道，不要(反射)应用。会产生栈内存溢出
                        method 当前调用的方法  dao.save() -->method 表示 save
                        args 调用方法时传递的参数 dao.save(car,book) --> args{car,book}
                     */
                    Annotation a = method.getAnnotations()[0] ; //获得当前方法上的注解
                    Method vm = a.getClass().getMethod("value");//获得注解中的value属性(方法)
                    String sql = (String) vm.invoke(a);// @Insert.value(); 获得sql

                    Object param = args==null?null:args[0] ; //获得本次方法调用时传递的参数。人为要求只能传递0或1个参数。

                    //此时获得了本次业务操作对应的sql和参数param
                    //需要判断是什么注解  再去做相应的操作
                    Object result = null ;//返回值
                    if(a.annotationType() == Insert.class){
                        if(param == null){
                            result = insert(sql) ;
                        }else{
                            result = insert(sql,param) ;
                        }
                    }else if(a.annotationType() == Update.class){
                        if(param == null){
                            result = update(sql) ;
                        }else{
                            result = update(sql,param) ;
                        }
                    }else if(a.annotationType() == Delete.class){
                        if(param == null){
                            result = delete(sql) ;
                        }else{
                            result = delete(sql,param) ;
                        }
                    }else if(a.annotationType() == Select.class){
                        Class rt = method.getReturnType() ;
                        if(rt == List.class){//根据方法的返回值类型决定调用哪个方法
                            //查询结果组成的类型应该是方法的返回类型中的泛型 List<Car> -> Car
                            //如何使用反射获得泛型
                            Type type = method.getGenericReturnType() ; //获得完整的返回值类型(包括泛型)
                            ParameterizedType pt = (ParameterizedType) type; //type是所有类型的父类，需要强转成可以获得的泛型的类型
                            Class fx = (Class) pt.getActualTypeArguments()[0];//获得List集合中的那1个泛型参数
                            if(fx == Map.class){
                                if(param == null){
                                    result = selectListMap(sql) ;
                                }else{
                                    result = selectListMap(sql,param) ;
                                }
                            }else{
                                if(param == null){
                                    result = selectList(sql,fx);
                                }else{
                                    result = selectList(sql,param,fx);
                                }
                            }
                        }else{
                            if(rt == Map.class){
                                if(param == null){
                                    result = selectMap(sql);
                                }else{
                                    result = selectMap(sql,param);
                                }
                            }else{
                                if(param == null){
                                    result = selectOne(sql,rt);
                                }else{
                                    result = selectOne(sql,param,rt);
                                }
                            }
                        }
                    }

                    return result;
                }
            }
        );
    }
}
