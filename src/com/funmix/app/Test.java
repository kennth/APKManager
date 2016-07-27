package com.funmix.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.dbutils.DbUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.funmix.common.DBMgr;
import com.funmix.utils.TypeUtil;
import com.funmix.utils.Utils;
//import com.tencent.msdk.tea.Base64; 
import com.tencent.msdk.tea.Base64;

public class Test {

	public static void main(String[] args) {
		
		 
		//parseAPK("ld2016_wl.apk");
		// genGameInfo(29);
		
		//genGameList();
		/*
		 * for(int i=13;i<=18;i++) genRerunScript(i);
		 */
		// System.out.println(getDevfromIP("192.169.33.104"));

		/*for(int i=25;i<=25;i++){ 
			 //genStopScript(i); 
			 //genRerunScript(i); 
			 //System.out.println(genTestUrl(i));
			 genGameInfo(i);
		 }*/
		//processFiles();
		//procCMCCTask();
		//genFirstStartScript();		
		genGameInfo(34);
		//chargeTest1(33);
		//chargeTest2("0f34k49OpKpQ","069614");
		//getMobArea();
	}
	
	private static void getMobArea() {
		try {
			String url = "";
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select mobile from tmpmob where area is null");
			PreparedStatement upstmt = conn.prepareStatement("update tmpmob set area = ? where mobile = ?");
			ResultSet rs = stmt.executeQuery();			

			HttpClient client = new DefaultHttpClient();
			HttpGet httpget;HttpResponse response;
			String codeNumb = "";String area;
			while (rs.next()) {
				url = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + rs.getString(1);
				httpget = new HttpGet(url);				
				// httppost.setHeader("Host", "");
				httpget.setHeader("Connection", "keep-alive");
				httpget.setHeader("Accept", "*/*");
				httpget.setHeader("Origin", "https://tcc.taobao.com");
				httpget.setHeader("X-Requested-With", "XMLHttpRequest");
				httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2540.0 Safari/537.36");
				httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");
				httpget.setHeader("Accept-Encoding", "deflate");
				httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");

				response = client.execute(httpget);
				int resCode = response.getStatusLine().getStatusCode();				
				int pos = 0;
				if (resCode == 200) {
					codeNumb = EntityUtils.toString(response.getEntity());
					System.out.println(codeNumb);
					pos = codeNumb.indexOf("province");
					area = codeNumb.substring(pos + 10, codeNumb.indexOf("',", pos));
					//System.out.println(rs.getString(1) + ":" + area);
					upstmt.setString(1, area);
					upstmt.setString(2, rs.getString(1));
					upstmt.executeUpdate();					
				} else {
					System.out.println("get code failed!");
				}
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
	
	private static void chargeTest1(int id) {
		try {
			String url = "";
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select cpid,cid,chid from tcmcctask where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				url = "http://183.129.241.3:8800/vcode?ms=13957186436&cpid=" + rs.getString("cpid") + "&cid=" + rs.getString("cid")+ "&chid=" + rs.getString("chid") + "&imei=351602398748242&imsi=460020597484618&pid=006111097015&cpparam=1234567890";
			}
			DbUtils.closeQuietly(conn);
			
			HttpClient client = new DefaultHttpClient();		
			HttpGet httpget = new HttpGet(url);
			//httppost.setHeader("Host", "");
			httpget.setHeader("Connection", "keep-alive");
			httpget.setHeader("Accept", "*/*");
			httpget.setHeader("Origin", "http://zj.189.cn");
			httpget.setHeader("X-Requested-With", "XMLHttpRequest");
			httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2540.0 Safari/537.36");
			httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpget.setHeader("Accept-Encoding", "deflate");
			httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");

			HttpResponse response = client.execute(httpget);
			int resCode = response.getStatusLine().getStatusCode();
			String codeNumb = "";
			if (resCode == 200) {
				codeNumb = EntityUtils.toString(response.getEntity());
				System.out.println(codeNumb);
			} else {
				System.out.println("get code failed!");
			}
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
	
	private static void chargeTest2(String sid,String vcode) {
		try {
			HttpClient client = new DefaultHttpClient();		
			HttpGet httpget = new HttpGet("http://183.129.241.3:8800/vpay?sid=" + sid + "&vcode=" + vcode);
			//httppost.setHeader("Host", "");
			httpget.setHeader("Connection", "keep-alive");
			httpget.setHeader("Accept", "*/*");
			httpget.setHeader("Origin", "http://zj.189.cn");
			httpget.setHeader("X-Requested-With", "XMLHttpRequest");
			httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2540.0 Safari/537.36");
			httpget.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpget.setHeader("Accept-Encoding", "deflate");
			httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");

			HttpResponse response = client.execute(httpget);
			int resCode = response.getStatusLine().getStatusCode();
			String codeNumb = "";
			if (resCode == 200) {
				codeNumb = EntityUtils.toString(response.getEntity());
				System.out.println(codeNumb);
			} else {
				System.out.println("get code failed!");
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		} catch (ClientProtocolException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static String genTestUrl(int id){
		String url = "";
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select cpid,cid,chid from tcmcctask where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				url = "curl \"http://183.129.241.3:8800/vcode?ms=15900000000&cpid=" + rs.getString("cpid") + "&cid=" + rs.getString("cid")+ "&chid=" + rs.getString("chid") + "&imei=867194028591199&imsi=460025824103409&pid=006068968010&cpparam=1234567890123456\"";
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return url;
	}
	
	private static void genFirstStartScript() {
		try {	
			processFiles();
			moveFiles();
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select id from tcmcctask where status=9 order by worker");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				genStopScript(rs.getInt(1));
			}
			rs = stmt.executeQuery();
			while(rs.next()){				
				genRerunScript(rs.getInt(1));				
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void procCMCCTask() {
		try {					
			moveFiles();
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select apkname from tcmcctask where cid is null or cid=''");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				parseAPK(rs.getString(1));
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void moveFiles() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select apkname from tcmcctask where filename = ? and apkname is not null");
			ResultSet rs;
			File[] flist = new File("d:\\ADBWorker\\crack").listFiles();
			for (File f : flist) {
				stmt.setString(1, f.getName());
				rs = stmt.executeQuery();
				if (rs.next())
					f.renameTo(new File("d:\\ADBWorker\\games\\" + rs.getString(1)));
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void processFiles() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("insert into tcmcctask (filename) values (?)");
			File[] flist = new File("d:\\ADBWorker\\crack").listFiles();
			for (File f : flist) {
				stmt.setString(1, f.getName());
				stmt.executeUpdate();
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static int getDevfromIP(String ip) {
		String[] tmps = ip.split("\\.");
		// System.out.println(TypeUtil.typeToString("", tmps));
		int id = (Integer.parseInt(tmps[2]) - 30) * 100 + Integer.parseInt(tmps[3]) - 100;
		return id;
	}

	private static void genGameList() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,cid,chid,worker,activity from tcmcctask where game<>''  and status>=1 order by game");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(2) + "-" + rs.getString(3) + "\t" + rs.getString(1));// + "\t" + rs.getString(4) + "\t" + rs.getString(5) + "");
				;
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void genStopScript(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select worker,activity from tcmcctask where id = ? and game<>''");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println("./stopapp.sh " + rs.getString(1) + " " + rs.getString(2).substring(0, rs.getString(2).indexOf("/")) + " >> rerun.log ");
				;
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void genRerunScript(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select worker,activity from tcmcctask where id = ? and game<>''");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println("./startapp.sh " + rs.getString(1) + " " + rs.getString(2) + " >> rerun.log &");
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void genGameInfo(int id) {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select game,cid,chid,cpid,filename from tcmcctask where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				System.out.println(rs.getString(5));
				System.out.println("CID:" + rs.getString(2));
				System.out.println("CHID:" + rs.getString(3));
				System.out.println("CPID:" + rs.getString(4));
				stmt = conn.prepareStatement("select consumexml from tcmccgame where CID = " + rs.getString(2));
				rs = stmt.executeQuery();
				if (rs.next()) {
					System.out.println(rs.getString(1));
				}
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void parseAPK(String apkname) {
		String path = apkname.substring(0,apkname.indexOf("."));
		if(!new File("D:\\ADBWorker\\games\\" + path).exists()){
			System.out.println(Utils.execCMD("cmd.exe /c D:\\ADBWorker\\games\\apk.bat d D:\\ADBWorker\\games\\" + apkname));
		}
		String CHARGE_FILENAME = "Charge.xml";
		String CONSUMECODEINFO_FILENAME = "ConsumeCodeInfo.xml";
		PublicKey publicKey = getPublicKeyFromX509();
		try {
			byte[] decryptChargeString = Utils.getFileContent("D:\\ADBWorker\\games\\" + path + "\\assets\\" + CHARGE_FILENAME);
			byte[] decryptConsumeString = Utils.getFileContent("D:\\ADBWorker\\games\\" + path + "\\assets\\" + CONSUMECODEINFO_FILENAME);
			byte[] realConsume = decryptData(publicKey, decryptConsumeString);
			byte[] realCharge = decryptData(publicKey, decryptChargeString);
			String consumeXml = new String(realConsume, "UTF8");
			System.out.println(consumeXml);
			String ChargeXML = new String(realCharge, "UTF8");
			// System.out.println(ChargeXML);
			String game,cid, chid, cpid, pkg, activity = null, consumecode;
			int pos, pos1;
			pos = ChargeXML.indexOf("<USR-TB-CID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CID>");
			cid = ChargeXML.substring(pos + "<USR-TB-CID>".length(), pos1);
			pos = ChargeXML.indexOf("<USR-TB-CHID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CHID>");
			chid = ChargeXML.substring(pos + "<USR-TB-CHID>".length(), pos1);
			pos = ChargeXML.indexOf("<USR-TB-CPID>");
			pos1 = ChargeXML.indexOf("</USR-TB-CPID>");
			cpid = ChargeXML.substring(pos + "<USR-TB-CPID>".length(), pos1);

			pos = consumeXml.indexOf("<gameName>");
			pos1 = consumeXml.indexOf("</gameName>");
			game = consumeXml.substring(pos + "<gameName>".length(), pos1);
			pos = consumeXml.indexOf("<consumercode>");
			pos1 = consumeXml.indexOf("</consumercode>");
			consumecode = consumeXml.substring(pos + "<consumercode>".length(), pos1);

			System.out.println(cid + "," + chid + "," + cpid + "," + consumecode);

			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File("D:\\ADBWorker\\games\\" + path + "\\AndroidManifest.xml"));
			// 获得所有子元素
			pkg = doc.getRootElement().getAttribute("package").getValue();
			System.out.println(pkg);
			List<Element> list = doc.getRootElement().getChild("application").getChildren("activity");
			for (Element el : list) {
				if (el.getChild("intent-filter") != null && el.getChild("intent-filter").getChild("category") != null) {
					if (el.getChild("intent-filter").getChild("category").getAttributes().get(0).getValue().equals("android.intent.category.LAUNCHER")) {
						List<Attribute> list1 = el.getAttributes();
						for (Attribute al : list1) {
							if (al.getName().equals("name")) {
								activity = al.getValue();
								System.out.println(activity);
							}
						}
					}
				}
			}
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("update tcmcctask set cid = ?,chid = ?,cpid = ?,consumecode = ?,activity = ?,game=? where apkname = ?");
			stmt.setString(1, cid);
			stmt.setString(2, chid);
			stmt.setString(3, cpid);
			stmt.setString(4, consumecode);
			stmt.setString(5, pkg + "/" + activity);
			stmt.setString(6, game);
			stmt.setString(7, apkname);
			stmt.executeUpdate();
			stmt = conn.prepareStatement("update tcmcctask a,tchannel b set game=concat(game,concat('-',b.channel)) where a.chid=b.chid and apkname = ?");
			stmt.setString(1, apkname);
			stmt.executeUpdate();
			stmt = conn.prepareStatement("insert into tcmccgame (cid,game,consumexml) values (?,?,?)");
			stmt.setString(1, cid);
			stmt.setString(2, game);
			stmt.setString(3, consumeXml);
			try{
				stmt.executeUpdate();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			DbUtils.closeQuietly(conn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static PublicKey getPublicKeyFromX509() {
		try {
			String CERT = "MIICITCCAYqgAwIBAgIEUlUcDDANBgkqhkiG9w0BAQUFADBUMQswCQYDVQQGEwJDTjELMAkGA1UECBMCSlMxCzAJBgNVBAcTAk5KMQ0wCwYDVQQKEwRjbWNjMQ0wCwYDVQQLEwRjbWNjMQ0wCwYDVQQDEwRjbWNjMCAXDTEzMTAwOTA5MDQxMloYDzIxMTMwOTE1MDkwNDEyWjBUMQswCQYDVQQGEwJDTjELMAkGA1UECBMCSlMxCzAJBgNVBAcTAk5KMQ0wCwYDVQQKEwRjbWNjMQ0wCwYDVQQLEwRjbWNjMQ0wCwYDVQQDEwRjbWNjMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCByTi+HI6nYLJGWQPDhTmoR1kjatJVapS5Ic3L/ht6CCTzgIwywu7tOnwDR1LXSy9KoEOHuTigmgnVlNYFTPpzqq+R4lPsmBThVf54wHK3L2Xjz2AqtFj7IcAacPrKZ5/Q4WXPsizDxcA3spWwqvQXkyEjwxg44KQuC0Oavpd84wIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAGQg1Qs6KVU1adTwdJ9rLIqnxyy9XlA8QpEvI5n9AR6pcS7Skkplga1bmA+qmwek569fGvZ7XRhwpVTD/FaPjTJD7LD2rQ8UadUSaQG8c1PiXmjTU3beo3a441aPeA6DBkXo6ZNWTTbD6gW9JGjMcKNYI/22ep6Wg2bkoBDY534Y";
			byte[] data = Base64.decode(CERT, Base64.DEFAULT);
			InputStream in = new ByteArrayInputStream(data);
			CertificateFactory f;

			f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) f.generateCertificate(in);
			return certificate.getPublicKey();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] decryptData(PublicKey publicKey, byte[] cipherData) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(2, publicKey);
			int inputLen = cipherData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;

			int i = 0;
			while (inputLen - offSet > 0) {
				byte[] cache;
				if (inputLen - offSet > 128)
					cache = cipher.doFinal(cipherData, offSet, 128);
				else {
					cache = cipher.doFinal(cipherData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return decryptedData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
