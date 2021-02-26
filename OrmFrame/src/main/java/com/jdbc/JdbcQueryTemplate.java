package com.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JdbcQueryTemplate extends JdbcTemplate {
    /**
     * @param driver
     * @param url
     * @param username
     * @param password
     */
    public JdbcQueryTemplate(String driver, String url, String username, String password) {
        super(driver, url, username, password);
    }

    @Override
    protected Object five() throws SQLException {
        ResultSet rs = stmt.executeQuery();
        List<Map<String,Object>> rows = new ArrayList<Map<String,Object>>();
        //将Result中的数据转移到List集合中
        while(rs.next()){
            Map<String,Object> row = new HashMap<String, Object>() ;
            for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
                String key = rs.getMetaData().getColumnName(i);//cno
                Object value = rs.getObject(i);//1001
                row.put(key.toLowerCase(),value);
            }
            rows.add(row);
        }
        return rows;
    }
}
