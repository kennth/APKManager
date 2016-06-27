package com.funmix.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.dbutils.DbUtils;
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
		// processFiles();
		// moveFiles();
		//parseAPK("ld2016_wl.apk");
		// genGameInfo(34);
		// genGameList();
		/*
		 * for(int i=13;i<=18;i++) genRerunScript(i);
		 */
		// System.out.println(getDevfromIP("192.169.33.104"));
		/*
		 * for(int i=1;i<=28;i++){ //genStopScript(i); genRerunScript(i); }
		 */
		/*
		 * genStopScript(34); genRerunScript(34);
		 */
		fixCMCCTask();
	}
	
	private static void fixCMCCTask() {
		try {
			Connection conn = DBMgr.getCon("helper");
			PreparedStatement stmt = conn.prepareStatement("select apkname from tcmcctask where cid is null");
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
			PreparedStatement stmt = conn.prepareStatement("select apkname from tcmcctask where filename = ?");
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
			PreparedStatement stmt = conn.prepareStatement("select game,cid,chid,worker,activity from tcmcctask where game<>''   order by game");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString(2) + "-" + rs.getString(3) + "\t" + rs.getString(1) + "\t" + rs.getString(4) + "\t" + rs.getString(5) + "");
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
				;
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
				if (el.getChild("intent-filter") != null && el.getChild("intent-filter").getChild("action") != null) {
					if (el.getChild("intent-filter").getChild("action").getAttributes().get(0).getValue().equals("android.intent.action.MAIN")) {
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
