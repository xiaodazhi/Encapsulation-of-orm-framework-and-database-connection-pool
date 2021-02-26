package com.jdbc.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    private List<ConnectionProxy> connections ; //装载连接池中的所有的连接(通过静态代理产生的连接)
    private ConnectionGenerator generator ;
    private String driver ;
    private String url ;
    private String username ;
    private String password ;
    private Integer total = 100;//连接池中最大连接数  暂定为100
    private Integer maxWait = 2000 ;//最大等待时间(毫秒)
    private Integer minIdle = 2 ;//最小空闲数，当连接池空闲连接数少于数量时，准备补充新连接
    public ConnectionPool(String driver,String url,String username,String password) throws ClassNotFoundException, SQLException {
        this.driver = driver ;
        this.url = url ;
        this.username = username ;
        this.password = password ;

        //创建连接池时，就自动创建一组初始连接对象(10个连接)
        Class.forName(driver) ;
        connections = new ArrayList(10) ;
        for(int i=0;i<10;i++){
            Connection conn = DriverManager.getConnection(url,username,password) ;
            ConnectionProxy cp = new ConnectionProxy(conn) ;
            connections.add(cp) ;
        }

        this.generator = new ConnectionGenerator();
        //我们的这个线程什么时候关闭呢
        //我们认为这个线程的关闭条件是当连接池关闭时  我们的线程也关闭
        //连接池如何关闭  连接池随着我们的主线程关闭
        //所以我们的这个线程要设置成精灵线程  随着主线程的关闭而关闭
        //线程分两类  用户线程，守护线程
        //当主线程要关闭的时候  如果用户线程没有结束  那主线程就要等  当用户线程结束的时候  主线程才能关闭
        //当主线程要关闭的时候  守护线程必须关闭
        generator.setDaemon(true);//精灵线程或守护线程，
        generator.start();
    }

    //提供连接的方法  我们想要使用连接就调用这个方法去连接池中拿到一个连接
    public Connection getConnection() throws InterruptedException {
        //找到空闲连接，改变使用状态  让使用状态变成正在使用，返回连接对象
        int wait_time = 0 ;//定义等待时间
        wait:while(true) {
            find:for (ConnectionProxy cp : connections) {
                if (!cp.useFlag) { // cp.useFlag == false
                    //需要考虑线程安全的问题  做线程同步
                    synchronized (cp) {
                        if(!cp.useFlag) {
                            //当前连接状态是空闲  状态改为正在使用
                            cp.useFlag = true;
                            synchronized ("dmc") {
                                "dmc".notify();//唤醒线程
                            }
                            return cp;
                        }else{
                            continue wait;
                        }
                    }
                }
            }
            //没有空闲连接，等一会
            wait_time += 100 ;
            if(wait_time == 2000){//如果等待时间超过2秒  就不再继续寻找空闲连接  给用户抛出异常
                throw new ConnectionPoolException("connect time out ") ;
            }else{
                Thread.sleep(100);
                continue wait;
            }
        }
    }


    // 连接生成器，(内部类)
    // 随着连接池的启动  我们启动一个线程
    // 连接数量不足时创建连接
    private class ConnectionGenerator extends Thread{
        @Override
        public void run(){
            /* */
            try {
                checkAndCreate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    //检测连接数量，发现不足创建补充连接(连接池扩容)
    public void checkAndCreate() throws SQLException, InterruptedException {
        while(true){
            int count = 0 ;//记录剩余空闲连接的数量
            for(ConnectionProxy cp : connections){
                if(!cp.useFlag){
                    //当前连接是一个空闲连接
                    count++ ;
                }
            }
            if(count <= minIdle){
                //连接不充足了，补充，每次补充10个，暂定上限100
                int add_count = 10 ;
                if(connections.size() + 10 > total){
                    //此次补充过后，就会超上限了  所以我们最后一次进行补充需要计算到底补充多少个
                    add_count = total-connections.size() ;
                }

                for(int i=0;i<add_count;i++){
                    Connection conn = DriverManager.getConnection(url,username,password) ;
                    ConnectionProxy ncp = new ConnectionProxy(conn) ;
                    connections.add(ncp);
                }
            }
            synchronized ("dmc") {
                "dmc".wait();
            }
        }
    }
}

    //-----------------------------------------------------


    public void setTotal(Integer total) {
        this.total = total;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }
}

