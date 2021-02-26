package com.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SqlHandler:
 * 为SqlSession对象执行的新版sql进行处理
 * 将其处理成原生sql
 * 额外处理参数
 * 将带#{}sql语句  处理成?的sql语句
 * 将对象参数  处理成数组参数
 */
public class SqlHandler {


    public static SqlAndParam execute(String sql,Object param){
        try {
            //将SQL中的#替换成?时  记录#{}中的key  将来会使用key从param对象中获得对应的属性值
            List<String> keys = new ArrayList<>();
            while (true) {
                int i1 = sql.indexOf("#{");
                int i2 = sql.indexOf("}");
                if (i1 == -1 || i2 == -1 || i1 > i2) {
                    //sql中没有成对的#{}  处理完毕了
                    break;
                }
                String key = sql.substring(i1 + 2, i2).trim();//获得了一个#{key}中的key
                keys.add(key);
            /*
                insert into t_car values(null,#{cname},#{color},#{price})
                "insert into t_car values(null," + ? + ",#{color},#{price})";
             */
                if (i2 == sql.length() - 1) {
                    //已经到达结尾
                    sql = sql.substring(0, i1) + "?";
                    break;
                } else {
                    sql = sql.substring(0, i1) + "?" + sql.substring(i2 + 1);//where cno = #{}
                    continue;
                }
            }
            //循环结束时我们就将sql中的#{}都变成了?  同时将#{key}中的key存入了keys集合  并且keys集合中记录key的顺序与对应的?顺序相同
            List<Object> params = new ArrayList<>();//按照?对应的key的顺序  装载对应的参数值

        /*
            判断传递的参数类型
            如果传递的是一个简单的类型(int String double)表示sql中应该只有一个#{}
                select("select * from t_car where cno = #{cno}",1001);
            如果传递的是一个对象  就需要反射根据key获得属性值
         */
            Class c = param.getClass();
            if (c == int.class || c == Integer.class || c == double.class || c == Double.class || c == String.class) {
                //表示sql中只有一个?
                params.add(param);
            } else if (c == Map.class || c == HashMap.class) {
                for (String key : keys) {
                    Map map = (Map) param;
                    Object value = map.get(key);
                    params.add(value);
                }
            } else {
                //是一个对象类型  需要使用反射根据keys  依次获得每一个key对应的属性值
                for (String key : keys) {
                    String mname = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    Method method = c.getMethod(mname);
                    Object value = method.invoke(param);//car.getCno();
                    params.add(value);
                }
            }
            System.out.println(sql);
            System.out.println(params);
            SqlAndParam sap = new SqlAndParam();
            sap.sql = sql;
            sap.params = params;
            return sap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
