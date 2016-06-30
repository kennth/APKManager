import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.funmix.common.DBMgr;

public class genModelfromDatabase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * 
		 * @author
		 * @version 创建时间：Jun 9, 2011 4:03:21 PM
		 * @return
		 * @desc 根据配置文件获得连接 Connection
		 */
/*		String a = null;		
		System.out.println(StringUtils.isNotEmpty(a));
		a = "";
		System.out.println(StringUtils.isNotEmpty(a));
		a = " ";
		System.out.println(StringUtils.isNotEmpty(a));*/
		getModel("tcmcctask", "helper");
		//getGrid("tcmcctask", "helper");
		//getGrid("titemlist", "aceon");
//		getGrid("titemlist", "aceon");
		//System.out.println("1A1234".substring(1,2));
	}
	
	public static void getGrid(String tableName, String Owner) {
		Connection connection;		
		String sqlString = "SELECT column_name,data_type,numeric_precision FROM information_schema.COLUMNS where table_name=? ORDER BY ORDINAL_POSITION";
		//System.out.println(sqlString);
		try {
			connection = DBMgr.getCon(Owner);
			PreparedStatement pStatement = connection.prepareStatement(sqlString);
			pStatement.setString(1, tableName);
			ResultSet rs = pStatement.executeQuery();
			String datatype;
			String colNames = "";
			String colModel = "";
			//{name:'id',index:'id', width:35,align:'center'},
			while (rs.next()) {
				colNames = colNames + "'" + rs.getString(1).toUpperCase() + "',"; 
				colModel = colModel + "{name:'" + rs.getString(1) + "',index:'" + rs.getString(1) ;
				datatype = rs.getString(2).toLowerCase();
				if(datatype.equals("float") || datatype.equals("double")|| datatype.equals("decimal") || datatype.indexOf("bigint")>-1 || datatype.indexOf("int")>-1){
					colModel = colModel + "', width:60,align:'right',editable:true,searchoptions:{sopt:['eq','ne','lt','le','gt','ge']}, editoptions:{defaultValue:'0'}},"; 
				}else{
					colModel = colModel + "', width:100,align:'left',editable:true,searchoptions:{sopt:['cn','eq']}},"; 
				}
				colModel = colModel + "\n";
			}
			System.out.println( colNames.toLowerCase().replaceAll("'", "\""));
			System.out.println( colNames);
			System.out.println( colModel);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void getModel(String tableName, String Owner) {
		Connection connection;		
		String sqlString = "SELECT column_name,data_type,numeric_precision FROM information_schema.COLUMNS where table_name=? ORDER BY ORDINAL_POSITION";
		//System.out.println(sqlString);
		try {
			connection = DBMgr.getCon(Owner);
			PreparedStatement pStatement = connection.prepareStatement(sqlString);
			pStatement.setString(1, tableName);
			ResultSet rs = pStatement.executeQuery();
			String datatype;
			while (rs.next()) {
				datatype = rs.getString(2).toLowerCase();
				if(datatype.equals("float") || datatype.equals("double")|| datatype.equals("decimal")){
					System.out.print("double");
				}else if(datatype.indexOf("bigint")>-1){
					System.out.print("long");
				}else if(datatype.indexOf("int")>-1){
					System.out.print("int");
				}else{
					System.out.print("String");
				}
				System.out.println( " " + rs.getString(1).toLowerCase() + ";");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
