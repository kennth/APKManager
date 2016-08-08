package com.funmix.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import org.apache.log4j.Logger;
import com.funmix.common.DBMgr;
import com.funmix.common.Log4jLogger;
import com.funmix.utils.Utils;

public class GearmanMonitorThread {
	protected static Logger log = Log4jLogger.getLogger(GearmanMonitorThread.class);
	static long lastalert = 0;
	public static void main(String[] args) {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); 
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true"); 
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout"); 
		
		boolean stop = false;
		String result = null;
		try {
			log.info("GearmanMonitor Start!!!");
			String url = "http://192.168.99.35/Gearman-Monitor/queue.php";
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,id,worker from tcmcctask where concat(concat('Worker',cid),concat('-',chid)) = ?");
			PreparedStatement upstmt = conn.prepareStatement("update tcmcctask set task = ?,live=? where mobile = concat(concat('Worker',cid),concat('-',chid)) = ?");
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
			
			int resCode;
			int pos = 0;
			String function, alert = "";
			int wait, running, worker;
			
			while (!stop) {
				response = client.execute(httpget);
				resCode = response.getStatusLine().getStatusCode();
				if (resCode == 200) {
					result = EntityUtils.toString(response.getEntity());
					//httpget.abort();
					result = result.substring(result.indexOf("<tbody>") + 7, result.indexOf("</tbody>"));
					//log.info(result);
					pos = result.indexOf("<tr");
					while (pos > 0) {
						pos = result.indexOf("<td></td>", pos) + 9;
						pos = result.indexOf(">", pos) + 1;
						function = result.substring(pos, result.indexOf("</td>", pos)).trim();
						pos = result.indexOf("</td>", pos) + 6;
						pos = result.indexOf(">", pos) + 1;
						wait = Integer.parseInt(result.substring(pos, result.indexOf("</td>", pos)).trim());
						pos = result.indexOf("</td>", pos) + 6;
						pos = result.indexOf(">", pos) + 1;
						running = Integer.parseInt(result.substring(pos, result.indexOf("</td>", pos)).trim());
						pos = result.indexOf("</td>", pos) + 6;
						if(result.indexOf("images/s_warn.png",pos)>-1){
							pos = result.indexOf("images/s_warn.png",pos);
							pos = result.indexOf("/>", pos) + 2;
						}else{
							pos = result.indexOf(">", pos) + 1;
						}
						worker = Integer.parseInt(result.substring(pos, result.indexOf("</td>", pos)).trim());
						result = result.substring(result.indexOf("</tr>") + 4, result.length());
						pos = result.indexOf("<tr");
						
						if (wait > 100) {
							log.error(function + ":" + wait + "," + running + "," + worker);
							stmt.setString(1, function);
							rs = stmt.executeQuery();
							if (rs.next()) {
								alert = alert + "Wait=" + wait + ":" + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3).replaceAll(" ", "-") + "\n";
							}
						}
						if (worker <= 2) {
							stmt.setString(1, function);
							rs = stmt.executeQuery();
							if (rs.next() && rs.getInt(2) < 100) {
								log.warn(function + ":" + wait + "," + running + "," + worker + " || " + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3));
								//alert = alert + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3).replaceAll(" ", "-") + "\n";
							}
						}						
						/*upstmt.setInt(1, wait);
						upstmt.setInt(2, worker);
						upstmt.setString(3,function);
						upstmt.executeUpdate();*/
					}

					if (alert.length() > 0) {
						log.warn(alert);						
						sendAlert(alert);
					}
				} else {
					log.error("get code failed!");
				}
				log.info("wait for next check!");
				Utils.sleep(10000);
			}
			DbUtils.closeQuietly(conn);			
		} catch (Exception e) {
			log.error(e);
			log.info(result);
		}
	}
	

	
	private static void sendAlert(String alert) throws ClientProtocolException, IOException{
		if(System.currentTimeMillis() - lastalert < 5*60*1000) //5分钟最多发一次警告
			return;
		HttpGet httpalert = new HttpGet("http://192.168.99.102:8100/msg?to=chenliang&text=" + URLEncoder.encode(alert, "utf8"));
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(httpalert);
		int resCode = response.getStatusLine().getStatusCode();
		if (resCode == 200) {		
			log.info("send alert succ!");
		}else {
			log.error("get code failed!");
		}
		lastalert = System.currentTimeMillis();
	}

}
