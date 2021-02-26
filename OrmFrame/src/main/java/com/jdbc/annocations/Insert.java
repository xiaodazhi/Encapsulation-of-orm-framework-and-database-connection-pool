package com.jdbc.annocations;

import java.lang.annotation.*;

@Target(ElementType.METHOD) //可以用在方法上
@Retention(RetentionPolicy.RUNTIME) //在jvm中存在，可以通过反射获得注解信息
@Inherited  //注解可继承 我们在接口方法上定义的注解，在实现类的方法上也可以获得。
public @interface Insert {
    /**
     * 存储sql语句
     * @return
     */
    public String value() ;
}
