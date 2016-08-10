package com.funmix.app;

import java.sql.Connection;
import java.util.LinkedList;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.log4j.Logger;
import com.funmix.common.DBMgr;
import com.funmix.common.Log4jLogger;
import com.funmix.model.CMCCTask;
import com.funmix.model.Device;
import com.funmix.utils.ADBUtils;
import com.funmix.utils.TypeUtil;
import com.funmix.utils.Utils;

public class APKManageThread extends Thread {

	protected static Logger		log		= Log4jLogger.getLogger(APKManageThread.class);
	private Connection			conn;
	private QueryRunner			runner	= new QueryRunner();
	private LinkedList<String>	queue	= new LinkedList<String>();
	private Device				device	= new Device();
	private ADBUtils			adb;
	private CMCCTask			task;

	public static void main(String[] args) {
		String deviceid = "460";
		if (args.length > 0 && args[0] != null) {
			deviceid = args[0];
		}
		new APKManageThread(deviceid).start();
	}

	public APKManageThread(String deviceid) {
		this.device.setId(Integer.parseInt(deviceid));
	}

	private boolean init() {
		log.info("start init ");
		try {
			conn = DBMgr.getCon("helper");
			log.info(TypeUtil.typeToString("", task));
			device = runner.query(conn, "select * from tdevice where id=" + device.getId(), new BeanHandler<Device>(Device.class));
			log.info("database init end!");
			if (device != null) {
				log.info("line:" + device.getLine());
				if (device.getLine().equalsIgnoreCase("UKN"))
					return false;
				adb = new ADBUtils(device, queue, conn);
				log.info("adbUtils init end!");
				log.info("workPath :" + adb.getWorkDir());

			} else {
				return false;
			}
		} catch (Exception e) {
			log.error(e);
			return false;
		}
		log.info("init end");
		return true;
	}
	
	private void restartActivity() throws InterruptedException{
		log.info(adb.execADB("shell am force-stop " + task.getActivity()));
		sleep(2000);
		log.info(adb.execADB("shell am start -n " + task.getActivity()));
		sleep(8000);
	}
	
	private void reinstallActivity() throws InterruptedException{
		String packname = task.getActivity();
		packname = packname.substring(0,packname.indexOf("/"));
		log.info(adb.execADB("uninstall " + packname));
		sleep(1000);
		log.info(adb.execADB("install " + task.getApkname()));
		sleep(2000);
		log.info(adb.execADB("shell am start -n " + task.getActivity()));
		sleep(8000);
		if(adb.getTopActivity().indexOf(packname)==-1){
			log.info(adb.execADB("shell am start -n " + task.getActivity()));
			sleep(2000);
		}
	}

	public void run() {
		if (init() == false) {
			log.info("init Failed");
			return;
		}
		String packname = "";
		try {
			String cmd = "adb -s " + device.getDevice() + " shell logcat |grep -E 'HOOK|ActivityManager' ";
			log.info(cmd);
			adb.waitDeviceOn();
			adb.clearLogcat();
			ADBLogger adblog = new ADBLogger(adb.getQueue(), cmd);
			adblog.start();
			task = runner.query(conn, "select activity,apkname,status,hook from tcmcctask where status>=0 and left(worker,3)<=" + device.getId() + " and right(worker,3)>=" + device.getId(),
					new BeanHandler<CMCCTask>(CMCCTask.class));
			String tmp;
			long st = System.currentTimeMillis();
			int workcount=0,restart=0;
			int pos = -1;			
			packname = task.getActivity();
			packname = packname.substring(0,packname.indexOf("/"));
			log.info("packname:" + packname);
			String topActivity;
			log.info(adb.execADB("shell am start -n " + task.getActivity()));
			while((tmp=Utils.getLogbykey("Load work count", queue))==null){
				sleep(1000);			
				if(System.currentTimeMillis() -st > 60){//超过1分钟没装载成功
					log.error("load workcount timeout!");
					restartActivity();
				}
			}
			log.info(tmp);
			tmp = tmp.substring(tmp.indexOf(":")+1,tmp.length()).trim();
			workcount = Integer.parseInt(tmp);
			sleep(8000);
			log.info(TypeUtil.typeToString("", task));
			while (task.getStatus() > 0) {
				sleep(1000);
				if (task.getStatus() == 2) {	
					if(System.currentTimeMillis()-st>10){
						log.info("Keep game alive! Workcount=" + workcount + ",start activity=" + restart);
						st = System.currentTimeMillis();
					}
					topActivity = adb.getTopActivity();
					if(topActivity == null || topActivity.indexOf(packname)==-1){
						log.info("Current top activity is wrong:" + topActivity);
						log.info("Try restart activity! Workcount=" + workcount + ",start activity=" + restart);
						restartActivity();
						restart++;						
					}else{
						restart =0;
					}
					if(restart>5){	//reinstall
						reinstallActivity();
						workcount = 0;
						restart =0;
						log.info("#### WORKCOUNT RESET #####");
					}						
					while (adb.getQueue().size() > 0) {
						tmp = adb.getQueue().poll();
						if(tmp.indexOf(packname)>-1 || tmp.indexOf("HOOK")>-1)
							log.info(tmp);
						pos = tmp.indexOf("workcount");
						if(pos >-1){							
							workcount = Integer.parseInt(tmp.substring(pos+2,tmp.indexOf(",",pos))); 
							//workcount=workcount+1;
							log.info("#### GET WORKCOUNT=[ " +  workcount + " ] #####");
							if(workcount>=30){//cleardata,restart activity
								reinstallActivity();
								workcount = 0;
								restart =0;
								log.info("#### WORKCOUNT RESET #####");
							}
						}
					}
					sleep(1000);
				}else if (task.getStatus() == 9) { //install
					//updatehook
					log.info("Install Game Start!");
					//log.info("Soft Restart");
					//log.info( adb.execADB(" install -r " + Utils.getCurrentPath() + "//" + adb.) );		
					log.info("Install Game End!");
					adb.sqlUpdate("update tcmcctask set status=1 where cid=" + task.getCid() + " and chid=" + task.getChid());
				}
				/*if (System.currentTimeMillis() - st > 60 * 1000) {
					task = runner.query(conn, "select activity,apkname,status from tcmcctask where status>=0 and left(worker,3)<=" + device.getId() + " and right(worker,3)>=" + device.getId(),
							new BeanHandler<CMCCTask>(CMCCTask.class));
					st = System.currentTimeMillis();
				}*/
			}
		} catch (Exception e) {
			log.info(e);
			System.exit(0);
		} finally {
			if (log != null) {
				Utils.sleep(3000);
				log = null;
			}
			DbUtils.closeQuietly(conn);
		}
	}
	
	
}
