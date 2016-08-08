package com.funmix.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.dbutils.DbUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import com.funmix.common.DBMgr;

public class GearmanMonitorThread {

	public static void main(String[] args) {
		
		try {
			String url = "http://192.168.99.35/Gearman-Monitor/queue.php";		
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,id,worker from tcmcctask where concat(concat('Worker',cid),concat('-',chid)) = ?");
			PreparedStatement upstmt = conn.prepareStatement("update tcmcctask set area = ? where mobile = ?");
			ResultSet rs;
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader("Connection", "keep-alive");
			httpget.setHeader("Accept", "*/*");
			httpget.setHeader("Origin", "");
			httpget.setHeader("X-Requested-With", "XMLHttpRequest");
			httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2540.0 Safari/537.36");
			httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpget.setHeader("Accept-Encoding", "deflate");
			httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
			HttpResponse response;
			String result;
			
			HttpGet httpalert;
			
			response = client.execute(httpget);			
			int resCode = response.getStatusLine().getStatusCode();
			int pos = 0;
			String function,alert="";
			int wait,running,worker;
			if (resCode == 200) {
				result = EntityUtils.toString(response.getEntity());	
				httpget.abort();
				result = result.substring(result.indexOf("<tbody>")+7,result.indexOf("</tbody>"));
				//System.out.println(result);
				pos = result.indexOf("<tr");
				while(pos>0){
					pos = result.indexOf("<td></td>",pos)+9;
					pos = result.indexOf(">",pos) + 1;
					function = result.substring(pos,result.indexOf("</td>",pos)).trim();					
					pos = result.indexOf("</td>",pos)+6;
					pos = result.indexOf(">",pos) + 1;					
					wait = Integer.parseInt(result.substring(pos,result.indexOf("</td>",pos)).trim());
					pos = result.indexOf("</td>",pos)+6;
					pos = result.indexOf(">",pos) + 1;
					running = Integer.parseInt(result.substring(pos,result.indexOf("</td>",pos)).trim());
					pos = result.indexOf("</td>",pos)+6;
					pos = result.indexOf(">",pos) + 1;
					worker = Integer.parseInt(result.substring(pos,result.indexOf("</td>",pos)).trim());					
					result = result.substring(result.indexOf("</tr>")+4,result.length());
					pos = result.indexOf("<tr");
					
					if(wait>100){
						System.out.println(function + ":" + wait + "," + running + "," + worker);
						alert = "JobWait_" + wait+"_";
						stmt.setString(1, function);
						rs = stmt.executeQuery();
						if(rs.next()){		
							alert = alert + "_" + rs.getString(1).replaceAll("-","_") + "_" + rs.getString(2) + "_" + rs.getString(3).replaceAll(" ","-") + "\n"; 
						}
					}
					if(worker == 0){
						System.out.println(function + ":" + wait + "," + running + "," + worker);
						alert = "ALL_Worker_Lost_";
						stmt.setString(1, function);
						rs = stmt.executeQuery();
						if(rs.next() && rs.getInt(2)<100){							
							alert = alert + rs.getString(1).replaceAll("-","_") + "_" + rs.getString(2) + "_" + rs.getString(3).replaceAll(" ","_") + "\n"; 
						}						
					}
					httpalert = new HttpGet("http://192.168.99.102:8100/msg?to=chenliang&text=" + alert);
					response = client.execute(httpalert);		
					httpalert.abort();
				}
				/*
				 * upstmt.setString(1, area);
				 * upstmt.setString(2, rs.getString(1));
				 * upstmt.executeUpdate();
				 */
			} else {
				System.out.println("get code failed!");
			}
			DbUtils.closeQuietly(conn);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		} catch (ClientProtocolException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
