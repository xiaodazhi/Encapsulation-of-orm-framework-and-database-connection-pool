package com.jdbc;

/**
 * 自定义异常可以继承Exception  和  RuntimeException
 * 那么我们继承哪一个
 * RuntimeException运行时异常  对可能产生的异常不用必须做处理
 * 而Exception是要对可能产生的异常必须做处理
 *
 * 1.如果这种异常非常重要  我们就要提前做好处理
 * 如果这种异常不是很重要  我们就不用提前做处理
 * 2.如果这种异常经常会出现  我们也要提前做处理
 * 如果这种异常不会经常出现  我们就不用提前做处理
 *
 * 这里我们使用RuntimeException
 * 给一个注释
 * 通过构造器  我们传递一个字符串给用户提示更详细的错误所在
 * 将这个字符串交给父类去处理
 *
 */
public class SqlFormatException extends RuntimeException {
    public SqlFormatException(){}
    public SqlFormatException(String msg){
        super(msg);
    }
}
