package com.funmix.common;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.IOException;


/**
 * ͨ�� log4j ��װ��
 * �ⲿ����� Log4jLogger.getLogger() ����
 */
public class Log4jLogger /* extends org.apache.log4j.Logger */ {
	public static final String DEFAULT_USER_WORKPATH = System.getProperty("user.dir");
	public static final String DEFAULT_XML_CONFIGURATION_FILE = "log4j.xml";
	public static final String DEFAULT_FILEAPPENDER_NAME = "Log.All.File";

	static {
		System.setProperty("WorkPath", DEFAULT_USER_WORKPATH);
		DOMConfigurator.configure(DEFAULT_USER_WORKPATH + "/" + DEFAULT_XML_CONFIGURATION_FILE);
	}

	/**
	 * �̳и���Ĺ��캯��
	 *
	 * @param sName
	 */
	//protected Log4jLogger(String sName)
	//{
	//    super(sName);
	//}
	protected Log4jLogger() {
	}

	public static Logger getLogger(Class<?> clsClass) {
		return LogManager.getLogger(clsClass.getName());
	}

	public static Logger getLogger(String sClassName) {
		return LogManager.getLogger(sClassName);
	}

	public static Logger getLogger(String sClassName, LoggerFactory lfLoggerFactory) {
		return LogManager.getLogger(sClassName, lfLoggerFactory);
	}

	public static Logger getRootLogger() {
		return LogManager.getRootLogger();
	}

	/**
	 * ���� Log4j FileAppender �ļ���־���·��
	 *
	 * @param sAppenderName : Log4j.xml �е� FileAppender ����, Ĭ�� DEFAULT_FILEAPPENDER_NAME
	 * @param sPathName     : �ɲ��� Log4jLogger.DEFAULT_USER_WORKPATH + "/app20091110.log" ����ʽ��ʹ�����·��
	 * @return boolean
	 */
	public static boolean setFileAppenderPath(String sAppenderName, String sPathName) {
		if (sAppenderName != null && !sAppenderName.trim().equals("") && sPathName != null && !sPathName.trim().equals("")) {
			sPathName = sPathName.trim();

			Appender l4jAppender = LogManager.getLoggerRepository().getRootLogger().getAppender(sAppenderName);
			if (l4jAppender != null && (l4jAppender instanceof FileAppender)) {
				FileAppender l4jFileAppender = (FileAppender) l4jAppender;
				l4jFileAppender.setFile(sPathName);
				l4jFileAppender.activateOptions();
				return true;
			}
		}
		return false;
	}

	/**
	 * ���� Log4j FileAppender �ļ���־���·��
	 *
	 * @param sAppenderName : Log4j.xml �е� FileAppender ����, Ĭ�� DEFAULT_FILEAPPENDER_NAME
	 * @param sPathName     : �ɲ��� Log4jLogger.DEFAULT_USER_WORKPATH + "/app20091110.log" ����ʽ��ʹ�����·��
	 * @param bAppend       : ��־�Ƿ�Ϊ׷��ģʽ
	 * @return boolean
	 */
	public static boolean setFileAppenderPath(String sAppenderName, String sPathName, boolean bAppend) {
		if (sAppenderName != null && !sAppenderName.trim().equals("") && sPathName != null && !sPathName.trim().equals("")) {
			sPathName = sPathName.trim();

			Appender l4jAppender = LogManager.getLoggerRepository().getRootLogger().getAppender(sAppenderName);
			if (l4jAppender != null && (l4jAppender instanceof FileAppender)) {
				FileAppender l4jFileAppender = (FileAppender) l4jAppender;
				try {
					l4jFileAppender.setFile(sPathName, bAppend, l4jFileAppender.getBufferedIO(), l4jFileAppender.getBufferSize());
				}
				catch (IOException ioex) {
					LogLog.error("Log4jLogger.setFileAppenderPath() : PathName = '" + sPathName + "', Append = " + String.valueOf(bAppend) + ", IOException !", ioex);
					return false;
				}
				l4jFileAppender.activateOptions();
				return true;
			}
		}
		return false;
	}
}
