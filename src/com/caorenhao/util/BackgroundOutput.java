package com.caorenhao.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 后台输出的IO流.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class BackgroundOutput {

	/** 输入流*/
	public InputStream inputStream;
	
	/** 输出流*/
	public OutputStream outputStream;
	
	private boolean finished = false;
	
	private Throwable t = null;
	
	private int lastReadLen = 0;
	/** 连续读0字节的次数 */
	private int readZeroCnt = 0;
	
	/**
	 * 输出流实例化.
	 *
	 * @param inputStream
	 * @param outputStream
	 */
	public BackgroundOutput(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	
	/**
	 * 判断输出流是否结束.
	 *
	 * @return boolean
	 */
	public boolean isFinished() {
		return this.finished;
	}
	
	/**
	 * 设置输出流结束.
	 *
	 * @param t
	 */
	public void setFinish(Throwable t) {
		if(this.finished)
			return;
		this.finished = true;
		this.t = t;
		IOUtil.forceClose(this.inputStream);
		IOUtil.forceClose(this.outputStream);
	}
	
	/**
	 * 判断是否存在异常.
	 *
	 * @return boolean
	 */
	public boolean hasException() {
		return this.t != null;
	}
	
	/**
	 * 获取异常.
	 *
	 * @return Throwable
	 */
	public Throwable getException() {
		return this.t;
	}
	
	/**
	 * TODO Put here a description of what this method does.
	 *
	 * @param buf
	 * @return int
	 * @throws Exception
	 */
	public int read(byte[] buf) throws Exception {
		int readLen = readImpl(buf);
		if(readLen != 0) {
			this.readZeroCnt = 0;
		} else if(this.lastReadLen == 0) {
			++this.readZeroCnt;
		}
		
		this.lastReadLen = readLen;
		return readLen;
	}
	
	private int readImpl(byte[] buf) throws Exception {
		if(isFinished())
			return -1;
		if(this.inputStream.available() == 0)
			return 0;
		int readLen = this.inputStream.read(buf);
		if(readLen > 0) {
			this.outputStream.write(buf, 0, readLen);
		}
		return readLen;
	}
	
	/**
	 * 尝试去结束输出流
	 */
	public void tryToFinish() {
		while(true) {
			if(isFinished())
				break;
			// 尝试次数过多，直接退出
			if(this.readZeroCnt > 5) {
				setFinish(null);
				break;
			}
			NetUtil.sleep(50);
		}
	}
}
