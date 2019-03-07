package com.jay.cloud_board.tcp;

import com.jay.cloud_board.CloudBoardServer;
import com.jay.cloud_board.Logger4j;
import com.jay.cloud_board.bean.ConnectingSocketInfo;

import java.io.BufferedInputStream;
import java.net.Socket;


public class Reader {

	private static final String TAG = Reader.class.getSimpleName();

	private Reader() {
	}

	public static void read(final ConnectingSocketInfo connectingSocketInfo) {


		final ReaderHandler handler = new ReaderHandler();

		final Socket socket = connectingSocketInfo.getSocket();

		new Thread(new Runnable() {
			@Override
			public void run() {

				Logger4j.d(TAG, "----开启一个socket读取线程 socketinfo=" + connectingSocketInfo.toString());
				//读取数据
				while (true) {


					if (socket == null || socket.isClosed()) {
						Logger4j.e(TAG, "Reader线程中socket异常");
//						CloudBoardServer.sConnectingSocketInfos.remove(connectingSocketInfo);
						return;
					}

					byte[] bytes = new byte[1024];
					int size;

					//保存某协议的数据
					StringBuilder dataSb = new StringBuilder();

					//保存某协议的长度
					int[] dataLength = new int[1];

					BufferedInputStream bis = null;
					try {

						bis = new BufferedInputStream(socket.getInputStream());
						while ((size = bis.read(bytes)) > 0) {

							String piece = new String(bytes, 0, size).trim();
							Logger4j.d(TAG, "piece包:" + piece);

							/**
							 * 解包逻辑:
							 * 与Writer中组包规则相对应
							 * 组包规则:给需要发出的协议添加前缀:"$00022",其中"$"是起始标识,后5位为本协议转成byte数组后的长度
							 * 请先阅读:https://www.jianshu.com/p/45957e180925
							 * 并理解 自动拆包/粘包
							 *
							 * 到达unpack()方法,传入的数据包(参数piece), 有以下几种情况:
							 * 1.dataSb长度==0,该数据包以"$*****"的6位为前缀,之后为协议的原始内容;长度为*****(取值范围0-99999),赋值给dataLength
							 *      1.1.piece包余下没有 dataLength 位,即协议数据 被拆包了
							 *      1.2.piece包余下超过 dataLength 位,即协议数据 后面粘包了
							 *      1.3.piece包余下刚好 dataLength 位
							 *
							 * 2.dataSb长度!=0,即旧协议数据没读完,旧协议数据还缺  absentLength = dataLength.length - dataSb.length 位
							 *      1.1.piece包没有 absentLength 位,即协议数据 再次被拆包了
							 *      1.2.piece包超过 absentLength 位,即协议数据 后面粘包了
							 *      1.3.piece包刚好 absentLength 位
							 *
							 */
							//解包
							boolean success = unpack(dataSb, dataLength, piece);
							if (!success) {
								Logger4j.e(TAG, "unpack failed");
								dataSb.delete(0, dataSb.length());
								dataLength[0] = 0;
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
						Logger4j.e(TAG, "Reader线程中socket异常");
//						CloudBoardServer.sConnectingSocketInfos.remove(connectingSocketInfo);
						return;
					}
				}


			}


			/**
			 * 解包
			 */
			private boolean unpack(StringBuilder dataSb, int[] dataLength, String piece) {

				/*1.dataSb长度==0,该数据包以"$*****"的6位为前缀,之后为协议的原始内容;长度为*****(取值范围0-99999),赋值给dataLength*/
				if (dataSb.length() == 0) {

					//piece包必须超过6位:协议前缀就有6位
					if (piece.length() < 6)
						return false;

					/*//不可能的情况:"$*****"6位字符被分拆,或被接在piece包旧协议数据的最末尾*/

					//异常情况
					if (piece.indexOf("$") > piece.length() - 6)
						return false;

					//取出协议的长度值(5位数)
					try {
						dataLength[0] = Integer.parseInt(piece.substring(piece.indexOf("$") + 1, piece.indexOf("$") + 6));
						Logger4j.d(TAG, "新协议长度=" + dataLength[0]);
					} catch (Exception e) {
						Logger4j.e(TAG, "协议书写不规范");
						return false;
					}

					/*1.1.piece包余下没有 dataLength 位,即协议数据 被拆包了*/
					//取前缀"$10002"之后的真实数据
					piece = piece.substring(piece.indexOf("$") + 6, piece.length());
					if (piece.length() < dataLength[0]) {

						dataSb.append(piece);
						Logger4j.d(TAG, "一条piece解包后 dataSb=" + dataSb);
						return true;

						/*1.2.piece包余下超过 dataLength 位,即协议数据 后面粘包了*/
					} else if (piece.length() > dataLength[0]) {

						//读取旧协议数据,处理掉旧协议
						int absentLength = dataLength[0] - dataSb.length();
						return interceptAndRecursiveRemaining(dataSb, dataLength, piece, absentLength);

						/*1.3.piece包余下刚好 dataLength 位*/
					} else {
						appendPiece(dataSb, dataLength, piece);
						Logger4j.d(TAG, "一条piece解包后 dataSb=" + dataSb);
						return true;
					}

					/*2.dataSb长度!=0,即旧协议数据没读完,旧协议数据还缺  absentLength = dataLength.length - dataSb.length 位*/
				} else {
					int absentLength = dataLength[0] - dataSb.length();

					/*2.1.piece包没有 absentLength 位,即协议数据 再次被拆包了*/
					if (piece.length() < absentLength) {

						dataSb.append(piece);
						Logger4j.d(TAG, "一条piece解包后 dataSb=" + dataSb);
						return true;

						/*2.2.piece包超过 absentLength 位,即协议数据 后面粘包了*/
					} else if (piece.length() > absentLength) {

						//读取旧协议数据,处理掉旧协议
						return interceptAndRecursiveRemaining(dataSb, dataLength, piece, absentLength);
						/*2.3.piece包刚好 absentLength 位*/
					} else {

						appendPiece(dataSb, dataLength, piece);
						Logger4j.d(TAG, "一条piece解包后 dataSb=" + dataSb);
						return true;
					}

				}
			}

			/**
			 * piece包前面部分是旧协议数据,后面递归处理
			 */
			private boolean interceptAndRecursiveRemaining(StringBuilder dataSb, int[] dataLength, String piece, int absentLength) {
				dataSb.append(piece, 0, absentLength);
				handler.handleProtocol(connectingSocketInfo, dataSb.toString());

				//恢复变量值
				dataSb.delete(0, dataSb.length());
				dataLength[0] = 0;

				//递归处理余下的piece包数据
				piece = piece.substring(absentLength, piece.length());
				boolean success = unpack(dataSb, dataLength, piece);
				if (!success) {
					dataSb.delete(0, dataSb.length());
					dataLength[0] = 0;
					return false;
				}
				Logger4j.d(TAG, "一条piece解包后 dataSb=" + dataSb);
				return true;
			}

			/**
			 * 整个piece包都属于旧协议数据
			 */
			private void appendPiece(StringBuilder dataSb, int[] dataLength, String piece) {
				dataSb.append(piece);
				handler.handleProtocol(connectingSocketInfo, dataSb.toString());

				//恢复变量值
				dataSb.delete(0, dataSb.length());
				dataLength[0] = 0;
			}


		}).start();
	}


}
