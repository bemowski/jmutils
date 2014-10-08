package net.jmatrix.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

/**
 * 
 */
public final class DBUtils {
   static Logger log = ClassLogFactory.getLog();
   
   
   /**
    * Down-converts and java.sql.Date, java.sql.Time or java.sql.Timestamp
    * to a java.util.Date.  All of the java.sql.* objectes extend java.util.date, 
    * however Jackson has very different handling for these classes in terms 
    * of JSON serialization.
    */
   public static final java.util.Date javaUtilDate(java.util.Date d) {
      if (d == null)
         return null;
      java.util.Date nd=new java.util.Date();
      nd.setTime(d.getTime());
      return nd;
   }
   
   public static final Timestamp sqlTimestamp(java.util.Date d) {
      if (d == null)
         return null;
      Timestamp ts=new Timestamp(d.getTime());
      return ts;
   }
   
   public static final void close(Connection con, Statement state, ResultSet rs) {
      close(rs);
      close(state);
      close(con);
   }

   /** */
   public static final void close(Statement state) {
      if (state != null) {
         try {
            state.close();
         } catch (Exception ex) {
            log.error("Error closing statement: ", ex);
         }
      }
   }

   /** */
   public static final void close(ResultSet rs) {
      if (rs != null) {
         try {
            rs.close();
         } catch (Exception ex) {
            log.error("Error closing ResultSet: ", ex);
         }
      }
   }

   /** */
   public static final void close(Connection con) {
      if (con != null) {
         try {
            if (!con.isClosed())
               con.close();
         } catch (Exception ex) {
            log.error("Error closing connection: ", ex);
         }
      }
   }

   /** */
   public static int intFunction(Statement state, String sql)
         throws SQLException {
      int result = 0;

      ResultSet rs = null;
      try {
         log.trace("intFunction(sql): " + sql);
         rs = state.executeQuery(sql);
         if (rs.next()) {
            result = rs.getInt(1);
         }
      } finally {
         DBUtils.close(rs);
      }
      return result;
   }
   
   /** */
   public static Date dateFunction(Statement state, String sql)
         throws SQLException {
      Date result=null;

      ResultSet rs = null;
      try {
         log.trace("dateFunction(sql): " + sql);
         rs = state.executeQuery(sql);
         if (rs.next()) {
            result = rs.getTimestamp(1);
         }
      } finally {
         DBUtils.close(rs);
      }
      return result;
   }

   public static double doubleFunction(Connection con, String sql)
   throws SQLException {
      //Connection con=null;
      Statement state=null;
      ResultSet rs=null;
      try {
         //con=Config.getInstance().getDataSource().getConnection();
         state=con.createStatement();
         log.trace("doubleFunction(sql): " + sql);
         rs = state.executeQuery(sql);
         if (rs.next()) {
            return rs.getDouble(1);
         }
      } finally {
         //DBUtils.close(con, state, rs);
      }
      return -1d;
   }
   
   public static String stringFunction(Connection con, String sql)
   throws SQLException {
      //Connection con=null;
      Statement state=null;
      ResultSet rs=null;
      try {
         //con=Config.getInstance().getDataSource().getConnection();
         state=con.createStatement();
         log.trace("doubleFunction(sql): " + sql);
         rs = state.executeQuery(sql);
         if (rs.next()) {
            return rs.getString(1);
         }
      } finally {
         //DBUtils.close(con, state, rs);
      }
      return null;
   }
   
   public static List<String> stringListFunction(Connection con, String sql)
   throws SQLException {
      //Connection con=null;
      Statement state=null;
      try {
         //con=Config.getInstance().getDataSource().getConnection();
         state=con.createStatement();
         return stringListFunction(state, sql);
      } finally {
         //DBUtils.close(con, state, null);
      }
   }
   
   /** */
   public static List<String> stringListFunction(Statement state, String sql)
         throws SQLException {
      List<String> list = new ArrayList<String>();

      ResultSet rs = null;
      String perf="DBUtils.stringListFunction("+sql+")";
      try {
         PerfTrack.start(perf);
         log.debug("stringListFunction(sql): " + sql);
         rs = state.executeQuery(sql);
         while (rs.next()) {
            list.add(rs.getString(1));
         }
      } finally {
         PerfTrack.stop(perf);
         DBUtils.close(rs);
      }
      return list;
   }
   
   /** */
   public static final String sqlSafeId(String id) {
      StringBuilder sb=new StringBuilder();
      char ch[]=id.toCharArray();
      for (char c:ch) {
         if (Character.isLetterOrDigit(c) || c == '-' || c == '_') 
            sb.append(c);
      }
      
      return sb.toString();
   }
   
   public static final String toInString(List<String> ids) {
      StringBuilder sb=new StringBuilder();
      
      sb.append("(");
      int size=ids.size();
      for (int i=0; i<size; i++) {
         String id=ids.get(i);
         sb.append("'"+id+"'");
         if (i < size-1)
            sb.append(", ");
      }
      sb.append(")");
      return sb.toString();
   }
   
   public static final Connection getConnection(String driver, String url, String user,
         String pass) throws SQLException {
      try {
         log.debug("Driver: '"+driver+"'");
         Class.forName(driver);
         
         log.debug("Connecting to " + url + " as " + user);
         Connection con = DriverManager.getConnection(url, user, pass);
         
         return con;
      } catch (Exception ex) {
         throw new SQLException("Error getting connection...", ex);
      }
   }
}
