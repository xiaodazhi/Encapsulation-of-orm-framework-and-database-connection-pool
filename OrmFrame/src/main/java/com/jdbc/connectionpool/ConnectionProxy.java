package com.jdbc.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 静态代理类
 * 连接对象的代理类
 */
public class ConnectionProxy extends AbstractConnection{

    boolean closeFlag = false ;  //false不关闭，释放 。 true 关闭
    boolean useFlag = false ;// flase空闲，true 被使用。

    public ConnectionProxy(Connection conn){
        super.conn = conn ;//从父类继承过来的Connection对象
    }

    @Override
    public void close() throws SQLException {
        if(closeFlag == true){
            conn.close();//关闭连接
        }else{
            //释放连接
            useFlag = false ;
        }
    }
}
