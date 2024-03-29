package com.liquid.telnet.client.pro;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;

public class TelnetConnectionThread implements Runnable {

	public interface TelnetThreadListener {
		public void dataReady(Message m);
	}

	protected static final int ALERT_MESSAGE = 0x0021;
	protected static final int CONNECTION_CLOSED = 0x0012;
	// MESSAGE Codes
	protected static final int CONNECTION_OPEN = 0x0011;
	// Stack Codes
	protected static final int MAIN_PAUSE = 0x0001;
	protected static final int MAIN_RESUME = 0x0002;
	protected static final int TELNET_ABORT = 238; // About process

	protected static final int TELNET_AO = 245; // Abort output
	protected static final int TELNET_AYT = 246; // Are you there

	protected static final int TELNET_BRK = 243; // Break
	protected static final int TELNET_DM = 242; // Data mark
	// option
	protected static final int TELNET_DO = 253; // Please, you use this option
	// sequence
	// Prefix to all telnet commands
	protected static final int TELNET_DONT = 254; // You are not to use this
	protected static final int TELNET_EC = 247; // Erase character
	protected static final int TELNET_EL = 248; // Erase line
	protected static final int TELNET_EOR = 239; // End of record
	protected static final int TELNET_GA = 249; // Go ahead
	// Telnet command codes
	protected static final int TELNET_IAC = 255; // Interpret as command escape
	protected static final int TELNET_IP = 244; // Interrupt process
	protected static final int TELNET_NOP = 241; // No operation.
	// number
	protected static final int TELNET_OPTION_3270 = 29; // Telnet 3270 regime
	protected static final int TELNET_OPTION_APRX = 4; // Approx message size
	// option
	protected static final int TELNET_OPTION_AUTH = 37; // Telnet authentication
	// Telnet options
	protected static final int TELNET_OPTION_BIN = 0; // Binary transmission
	protected static final int TELNET_OPTION_BYTE = 19; // Byte macro
	protected static final int TELNET_OPTION_DATA = 20; // Data entry terminal
	protected static final int TELNET_OPTION_ECHO = 1; // Echo
	protected static final int TELNET_OPTION_ENVIR = 36; // Telnet environment
	protected static final int TELNET_OPTION_EOR = 25; // End of record

	// disposition
	protected static final int TELNET_OPTION_EXT = 17; // Extended ascii
	// option
	protected static final int TELNET_OPTION_EXTOP = 255; // Extended-options-list
	protected static final int TELNET_OPTION_LINE = 34; // Linemode
	// character set
	protected static final int TELNET_OPTION_LOGO = 18; // Logout
	protected static final int TELNET_OPTION_NAWS = 31; // Negotiate about
														// option
	protected static final int TELNET_OPTION_NENVIR = 39; // Telnet environment
	protected static final int TELNET_OPTION_OCRD = 10; // Out carriage-return
	// disposition
	protected static final int TELNET_OPTION_OFD = 13; // Output formfeed
														// disposition
	protected static final int TELNET_OPTION_OHT = 11; // Output horizontal
	// tabstops
	protected static final int TELNET_OPTION_OHTD = 12; // Out horizontal tab
	// disposition
	protected static final int TELNET_OPTION_OLD = 16; // Output linefeed
														// trans/echo
	protected static final int TELNET_OPTION_OLW = 8; // Output line width
														// identification
	protected static final int TELNET_OPTION_OM = 27; // Output marking
	protected static final int TELNET_OPTION_OPS = 9; // Output page size
	// disposition
	protected static final int TELNET_OPTION_OVT = 14; // Output vertical
														// tabstops
	protected static final int TELNET_OPTION_OVTD = 15; // Output vertical tab
	protected static final int TELNET_OPTION_RECN = 2; // Reconnection
	protected static final int TELNET_OPTION_REM = 7; // Remote controlled
	protected static final int TELNET_OPTION_RFC = 33; // Remote flow control
	protected static final int TELNET_OPTION_SNDL = 23; // Send location
	// negotiation
	protected static final int TELNET_OPTION_STAT = 5; // Status
	protected static final int TELNET_OPTION_SUP = 21; // supdup protocol
	protected static final int TELNET_OPTION_SUPO = 22; // supdup output
	protected static final int TELNET_OPTION_SUPP = 3; // Suppress go ahead
	protected static final int TELNET_OPTION_TACACS = 26; // Tacacs user
	protected static final int TELNET_OPTION_TERM = 24; // Terminal type
	protected static final int TELNET_OPTION_TIM = 6; // Timing mark
	protected static final int TELNET_OPTION_TLN = 28; // Terminal location
	// window size
	protected static final int TELNET_OPTION_TS = 32; // Terminal speed
	protected static final int TELNET_OPTION_X3 = 30; // X.3 PAD
	protected static final int TELNET_OPTION_XDL = 35; // X display location
	protected static final int TELNET_SB = 250; // Subnegotiate
	protected static final int TELNET_SE = 240; // End of subnegotiation
	protected static final int TELNET_SUSP = 237; // Suspend process
	protected static final int TELNET_WILL = 251; // I will use option
	protected static final int TELNET_WONT = 252; // I won't use option
	protected static final int TELNET_xEOF = 236; // End of file: EOF already
													// used
	protected static final int TEXT_FINISHED = 0x0004;
	protected static final int TEXT_SCROLL = 0x0003;
	protected static final int TEXT_SENT = 0x0002;

	protected static final int TEXT_UPDATE = 0x0001;
	private int bgcolor;
	@SuppressWarnings("unused")
	private int bold = 0;
	private int color;

	private TelnetThreadListener DataListener;
	private String Host;
	private InputStream inStream;
	private String LeftOvers;

	private PrintWriter outStream;
	private int Port;

	// public BufferedWriter out;

	private SendStack sendData;

	private Socket skt;

	public TelnetConnectionThread(String h, int p, SendStack ss,
			TelnetThreadListener tl) {
		// TODO Auto-generated constructor stub
		this.Host = h;
		this.Port = p;
		this.DataListener = tl;
		this.sendData = ss;
		LeftOvers = Messages.getString("TelnetConnectionThread.0"); //$NON-NLS-1$
		color = Color.WHITE;
		bgcolor = Color.BLACK;
	}

	private void parseBuffer(char[] dataBuffer, int bufferSize) {

		String Formatted = Messages.getString("TelnetConnectionThread.1"); //$NON-NLS-1$
		char[] buffer = new char[bufferSize + LeftOvers.length()];

		for (int x = 0; x < LeftOvers.length(); x++) {
			buffer[x] = LeftOvers.charAt(x);
		}

		for (int x = LeftOvers.length(); x < LeftOvers.length() + bufferSize; x++) {
			buffer[x] = dataBuffer[x - LeftOvers.length()];
		}
		bufferSize = bufferSize + LeftOvers.length();

		for (int x = 0; x < bufferSize; x++) {

			// For Debugging
			// Formatted += "(" + Integer.toHexString(buffer[x]) + ")";
			switch (buffer[x]) {
			case TELNET_IAC: {
				// Formatted += "(IAC)";
				if (x + 1 < bufferSize) {
					x++;
					switch (buffer[x]) {
					case TELNET_WILL: {
						if (x + 1 < bufferSize) {
							// Formatted += "(WILL)";
							x++;
							switch (buffer[x]) {
							case TELNET_OPTION_EOR: {
								// Formatted += "(EOR)";
								sendData.push(Messages
										.getString("TelnetConnectionThread.2") + (char) TELNET_IAC //$NON-NLS-1$
										+ (char) TELNET_DONT
										+ (char) TELNET_OPTION_EOR);
								break;
							}
							default: {
								// Don't know what it is so tell the server
								// don't use it
								// sendData.push("" + (char)TELNET_IAC +
								// (char)TELNET_DONT + (char)buffer[x]);
								break;
							}
							}
						}
						break;
					}
					case TELNET_DONT: {
						if (x + 1 < bufferSize) {
							// Formatted += "(WILL)";
							x++;
							switch (buffer[x]) {
							case TELNET_OPTION_EOR: {
								// Formatted += "(EOR)";
								sendData.push(Messages
										.getString("TelnetConnectionThread.3") + (char) TELNET_IAC //$NON-NLS-1$
										+ (char) TELNET_DONT
										+ (char) TELNET_OPTION_EOR);
								break;
							}
							default: {
								// Formatted += "(" +
								// Integer.toHexString((char)buffer[x]) + ")";
								// //Don't know what it is so tell the server
								// don't use it
								// sendData.push("" + (char)TELNET_IAC +
								// (char)TELNET_DONT + (char)buffer[x]);
								break;
							}
							}
						}
						break;
					}
					case TELNET_SB: {
						for (x += 1; x < bufferSize; x++) {
							if (buffer[x] == TELNET_IAC) {
								if (buffer[x] + 1 < bufferSize) {
									x++;
									if (buffer[x] == TELNET_SE) {
										break;
									}
								}
							}
						}
					}
					default: {
						if (x + 1 < bufferSize) {
							x++;
						}
						break;
					}
					}
				}
				break;
			}
			case '\033': {
				if (Formatted.length() > 0) {
					sendMessageText(Formatted, color, bgcolor);
					Formatted = Messages.getString("TelnetConnectionThread.4"); //$NON-NLS-1$
					LeftOvers = Messages.getString("TelnetConnectionThread.5"); //$NON-NLS-1$
				}

				if (x + 1 < bufferSize) {
					if (buffer[x + 1] == '[') {
						int y = 0;
						for (y = x + 2; y < bufferSize; y++) {
							if (buffer[y] == 'm') {
								int startpos = x + 2;
								for (int j = startpos + 1; j <= y; j++) {

									String code = Messages
											.getString("TelnetConnectionThread.6"); //$NON-NLS-1$
									if (buffer[j] == ';') {
										for (int n = startpos; n < j; n++)
											code += buffer[n];

										startpos = j + 1;
									} else if (buffer[j] == 'm') {
										for (int n = startpos; n < j; n++)
											code += buffer[n];

										startpos = j + 1;
									}

									if (code != Messages
											.getString("TelnetConnectionThread.7")) //$NON-NLS-1$
										switch (Integer.valueOf(code)) // text
																		// codes
										{
										case 0: {
											bold = 0;
											color = Color.WHITE;
											bgcolor = Color.BLACK;
											break;
										}
										case 1: {
											bold = 1;
											break;
										}
										case 30: {
											color = Color.BLACK;
											break;
										}
										case 31: {
											color = Color.RED;
											break;
										}
										case 32: {
											color = Color.GREEN;
											break;
										}
										case 33: {
											color = Color.YELLOW;
											break;
										}
										case 34: {
											color = Color.BLUE;
											break;
										}
										case 35: {
											color = Color.MAGENTA;
											bold = 1;
											break;
										}
										case 36: {
											color = Color.CYAN;
											break;
										}
										case 37: {
											color = Color.WHITE;
											break;
										}
										case 39: {
											color = Color.WHITE;
											break;
										}
										case 40: {
											bgcolor = Color.BLACK;
											break;
										}
										case 41: {
											bgcolor = Color.RED;
											break;
										}
										case 42: {
											bgcolor = Color.GREEN;
											break;
										}
										case 43: {
											bgcolor = Color.YELLOW;
											break;
										}
										case 44: {
											bgcolor = Color.BLUE;
											break;
										}
										case 45: {
											bgcolor = Color.MAGENTA;
											bold = 1;
											break;
										}
										case 46: {
											bgcolor = Color.CYAN;
											break;
										}
										case 47: {
											bgcolor = Color.WHITE;
											break;
										}
										case 49: {
											bgcolor = Color.BLACK;
											break;
										}
										default: {
											break;
										}
										}
								}
								x = y;
								break;
							} else if ((buffer[y] >= 'a' && buffer[y] <= 'z')
									|| (buffer[y] >= 'A' && buffer[y] <= 'Z')) {
								x = y;
								break;
							} else if (y == bufferSize - 1) {
								for (int n = x; n < bufferSize; n++) {
									LeftOvers += buffer[n];
								}
								return;
							}
						}
					}
				}
				break;
			}
			case '\r': {
				break;
			}
			case '\n': {
				Formatted += Messages.getString("TelnetConnectionThread.8"); //$NON-NLS-1$
				sendMessageText(Formatted, color, bgcolor);
				Formatted = Messages.getString("TelnetConnectionThread.9"); //$NON-NLS-1$
				break;
			}
			default: {
				if (buffer[x] >= 0x20 && buffer[x] <= 0x7E) {
					Formatted += buffer[x];
				}

				// else // For Debugging
				// Formatted += "X" + Integer.toHexString((char)buffer[x]) +
				// "X";

				break;
			}
			}
		}
		if (Formatted.length() > 0) {
			// LeftOvers = Formatted;
			sendMessageText(Formatted, color, bgcolor);
			Formatted = Messages.getString("TelnetConnectionThread.10"); //$NON-NLS-1$
		}
	}

	// @Override
	public synchronized void run() {

		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		// int color = Color.WHITE;
		try {
			sendMessageText(
					Messages.getString("TelnetConnectionThread.11"), Color.WHITE, Color.BLACK); //$NON-NLS-1$
			skt = new Socket(Host, Port);
			skt.setSoTimeout(50);
			inStream = skt.getInputStream();
			outStream = new PrintWriter(skt.getOutputStream());
			// out = new BufferedWriter(outStream);

			sendMessageText(
					Messages.getString("TelnetConnectionThread.12"), Color.WHITE, Color.BLACK); //$NON-NLS-1$
			// TODO Auto-generated method stub

			while (!Thread.currentThread().isInterrupted()) {
				// if(inStream.ready()) {

				char[] dataBuffer = new char[2000];
				String tempBuffer = Messages
						.getString("TelnetConnectionThread.13"); //$NON-NLS-1$

				int numChars = 0;// inStream.read(dataBuffer);
				for (numChars = 0; numChars < 2000; numChars++) {

					int temp = 0;
					try {
						temp = inStream.read();
					} catch (IOException e) {
						temp = -1;
					}
					if (temp == -1)
						break;
					else
						dataBuffer[numChars] = (char) temp;
				}
				// inStream.
				if (numChars == -1) {
					Thread.currentThread().interrupt();
				} else {

					parseBuffer(dataBuffer, numChars);

					for (int x = 0; x < numChars; x++) {
						tempBuffer += dataBuffer[x];
					}
				}

				Thread.yield();
				// }

				if (outStream.checkError()) {
					Thread.currentThread().interrupt();
				}

				if (!sendData.isEmpty()) {
					Object stackObject = sendData.pop();
					Class<? extends Object> objectClass = stackObject
							.getClass();

					if (objectClass == String.class) {
						// String Formatted ="(SENT ";
						// char[] temp = ((String)stackObject).toCharArray();
						// for(int x=0; x<((String)stackObject).length(); x++)
						// {
						// outStream.append(temp[x]);
						// Formatted += (char)temp[x];
						// }
						// for(int j=0; j<test.length(); j++)
						// Formatted += Integer.toHexString(test.charAt(j));
						// Formatted += ")";
						// sendMessageText(Formatted, Color.WHITE, Color.BLACK);
						outStream.write((String) stackObject);
						// sendMessageText((String)stackObject,Color.WHITE,Color.BLACK);
						outStream.flush();
					} else if (objectClass == Integer.class) {
						switch ((Integer) stackObject) {
						case MAIN_PAUSE: {

						}
						case MAIN_RESUME: {

						}
						default:
							break;
						}
					} else {
						DataListener = (TelnetThreadListener) stackObject;
					}

				}

			}

			inStream.close();
			outStream.close();
			sendMessageText(
					Messages.getString("TelnetConnectionThread.14"), Color.WHITE, Color.BLACK); //$NON-NLS-1$
		} catch (UnknownHostException e1) {
			sendMessageText(
					Messages.getString("TelnetConnectionThread.15"), Color.WHITE, Color.BLACK); //$NON-NLS-1$
		} catch (IOException e1) {
			sendMessageText(
					Messages.getString("TelnetConnectionThread.16"), Color.WHITE, Color.BLACK); //$NON-NLS-1$
			e1.printStackTrace();
		} catch (IllegalStateException e) {
			sendMessageText(
					Messages.getString("TelnetConnectionThread.17"), Color.WHITE, //$NON-NLS-1$
					Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
			// sendMessageText("\nException\n",Color.WHITE);
		}

	}

	@SuppressWarnings("unused")
	private void sendMessageScroll() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message m = new Message();
		m.what = TEXT_SCROLL;
		DataListener.dataReady(m);
	}

	@SuppressWarnings("unused")
	private void sendMessageSent() {
		Message m = new Message();
		m.what = TEXT_SENT;
		DataListener.dataReady(m);
	}

	private void sendMessageText(String text, int color, int bgcolor) {
		Bundle updateText = new Bundle();
		updateText.putString(
				Messages.getString("TelnetConnectionThread.18"), text); //$NON-NLS-1$
		updateText.putInt(
				Messages.getString("TelnetConnectionThread.19"), color); //$NON-NLS-1$
		updateText.putInt(
				Messages.getString("TelnetConnectionThread.20"), bgcolor); //$NON-NLS-1$
		Message m = new Message();
		m.what = TEXT_UPDATE;
		m.setData(updateText);
		DataListener.dataReady(m);
	}
}