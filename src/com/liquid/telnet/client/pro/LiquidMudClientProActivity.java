package com.liquid.telnet.client.pro;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.liquid.telnet.client.pro.ServerListDialog.ConnectReady;

public class LiquidMudClientProActivity extends Activity {
	private class CommandListener implements OnKeyListener {

		// @Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub

			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_DOWN: {
					EditText cmd = (EditText) findViewById(R.id.cmdText);
					if (historypos > 0) {
						historypos--;
					}
					if (historypos == 0) {
						cmd.setText(Messages
								.getString("LiquidMudClientProActivity.0")); //$NON-NLS-1$
					} else {
						cmd.setText(commandHistory.get(commandHistory.size()
								- historypos));
					}
					return true;
				}
				case KeyEvent.KEYCODE_DPAD_UP: {
					EditText cmd = (EditText) findViewById(R.id.cmdText);
					if (historypos < commandHistory.size()) {
						historypos++;
					}
					if (historypos == 0) {
						cmd.setText(Messages
								.getString("LiquidMudClientProActivity.1")); //$NON-NLS-1$
					} else {
						cmd.setText(commandHistory.get(commandHistory.size()
								- historypos));
					}
					return true;
				}
				case KeyEvent.KEYCODE_BACK: {
					LiquidMudClientProActivity.this.finish();
					return false;
				}
				case KeyEvent.KEYCODE_ENTER:
					EditText cmd = (EditText) findViewById(R.id.cmdText);
					sendData.push(cmd.getText()
							+ Messages
									.getString("LiquidMudClientProActivity.2")); //$NON-NLS-1$
					addText(cmd.getText()
							+ Messages
									.getString("LiquidMudClientProActivity.3"), Color.WHITE, Color.BLACK); //$NON-NLS-1$

					historypos = 0;

					if (commandHistory.size() > 1) {
						if (!(cmd
								.getText()
								.toString()
								.compareTo(
										commandHistory.get(commandHistory
												.size() - 1)) == 0)) {
							commandHistory.add(cmd.getText().toString());
							if (commandHistory.size() > HISTORY_BUFFER_SIZE) {
								commandHistory.remove(0);
							}
						}
					} else {
						commandHistory.add(cmd.getText().toString());
					}
					cmd.setText(Messages
							.getString("LiquidMudClientProActivity.4")); //$NON-NLS-1$

					scrolllocked = false;

					TextView textview = (TextView) findViewById(R.id.MainText);
					textview.scrollTo(
							0,
							(textview.getLineCount() * textview.getLineHeight())
									- textview.getHeight());
					return true;

				}
			}
			return false;
		}
	}

	private class OnReadyListener implements ServerListDialog.ConnectReady {

		// @Override
		public void connect(int pos) {
			// TODO Auto-generated method stub
			if (pos != 0) {
				Hostname = ServerListing.get(pos).IP;
				Port = ServerListing.get(pos).Port;

				tlisten = new ThreadListener();
				connectionThread = new Thread(new TelnetConnectionThread(
						Hostname, Port, sendData, tlisten));
				connectionThread.start();
			} else {
				ConnectionDialog dialog = new ConnectionDialog(
						LiquidMudClientProActivity.this,
						new ServerAddListener());
				dialog.show();
			}
		}

		// @Override
		public void delete(int pos) {

			ServerListing.remove(pos);

			ConnectReady cready = new OnReadyListener();
			ServerListDialog dialog = new ServerListDialog(
					LiquidMudClientProActivity.this, cready, ServerListing);
			dialog.show();
		}

		// @Override
		public void modify(int pos) {

			ConnectionDialog dialog = new ConnectionDialog(
					LiquidMudClientProActivity.this, pos,
					new ServerAddListener(), ServerListing.get(pos).ServerName,
					ServerListing.get(pos).IP, ServerListing.get(pos).Port);
			dialog.show();

		}

	}

	private class ServerAddListener implements ConnectionDialog.ServerAdd {

		// @Override
		public void Add(String Name, String H, int p) {
			// TODO Auto-generated method stub

			ServerListing.add(new ServerInfo(Name, H, p));

			ConnectReady cready = new OnReadyListener();
			ServerListDialog dialog = new ServerListDialog(
					LiquidMudClientProActivity.this, cready, ServerListing);
			dialog.show();
		}

		// @Override
		public void Modify(int pos, String name, String H, int p) {
			// TODO Auto-generated method stub

			ServerListing.get(pos).ServerName = name;
			ServerListing.get(pos).IP = H;
			ServerListing.get(pos).Port = p;

			ConnectReady cready = new OnReadyListener();
			ServerListDialog dialog = new ServerListDialog(
					LiquidMudClientProActivity.this, cready, ServerListing);
			dialog.show();
		}

	}

	public class ServerInfo {
		String IP;
		int Port;
		String ServerName;

		public ServerInfo(String name, String ip, int port) {
			ServerName = name;
			IP = ip;
			Port = port;
		}

		@Override
		public String toString() {
			return ServerName;
		}
	}

	public class TempData {
		public List<String> commandHistory;// = new ArrayList<String>();
		public Thread con;
		public SendStack stack;
		public List<String> viewBufferFull;// = new ArrayList<String>();
		public List<Integer> viewBufferFullBGColor;// = new
													// ArrayList<Integer>();
		public List<Integer> viewBufferFullColor;// = new ArrayList<Integer>();
	}

	private class ThreadListener implements
			TelnetConnectionThread.TelnetThreadListener {
		// @Override
		public void dataReady(Message m) {
			LiquidMudClientProActivity.this.TCUpdateHandler.sendMessage(m);
		}
	}

	protected static final int HISTORY_BUFFER_SIZE = 20;
	public static int LINE_REMOVAL_AMOUNT = 20;
	public static int MAX_TEXT_LINES = 200;

	/** Called when the activity is first created. */

	protected static final int MENU_CONNECT = 1;

	protected static final int MENU_DISCONNECT = 2;
	protected static final int MENU_OPTIONS = 3;

	private List<String> commandHistory;
	Thread connectionThread = null;
	private int historypos = 0;

	String Hostname = Messages.getString("LiquidMudClientProActivity.5"); //$NON-NLS-1$
	// private SpannableString inputSpanBuffer= new SpannableString("");
	private String inputBuffer;
	int Port = 23;
	private boolean scrolllocked = false; // false follows scroll true stays

	private float scrollstart = 0;

	private float scrolly = 0;

	private SendStack sendData;

	private ArrayList<ServerInfo> ServerListing;

	public Handler TCUpdateHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TelnetConnectionThread.TEXT_UPDATE: {
				addText(msg.getData().getString(
						Messages.getString("LiquidMudClientProActivity.6")), //$NON-NLS-1$
						msg.getData()
								.getInt(Messages
										.getString("LiquidMudClientProActivity.7")), //$NON-NLS-1$
						msg.getData()
								.getInt(Messages
										.getString("LiquidMudClientProActivity.8"))); //$NON-NLS-1$
				TextView textview = (TextView) findViewById(R.id.MainText);

				if (!scrolllocked)
					textview.scrollTo(
							0,
							(textview.getLineCount() * textview.getLineHeight())
									- textview.getHeight());

				break;
			}
			case TelnetConnectionThread.TEXT_SENT: {
				break;
			}
			case TelnetConnectionThread.TEXT_SCROLL: {
				TextView textview = (TextView) findViewById(R.id.MainText);
				textview.scrollTo(0,
						(textview.getLineCount() * textview.getLineHeight())
								- textview.getHeight());
				break;
			}
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private ThreadListener tlisten;
	// private SendStack recvData = new SendStack(50);

	private List<String> viewBufferFull;// = new ArrayList<String>();

	private List<Integer> viewBufferFullBGColor;// = new ArrayList<Integer>();

	private List<Integer> viewBufferFullColor;// = new ArrayList<Integer>();

	public void addText(String text, int color, int bgcolor) {

		TextView textview = (TextView) findViewById(R.id.MainText);

		if (!scrolllocked) {
			int viewBufferSize = viewBufferFull.size();
			int viewBufferLines = textview.getLineCount();

			if (viewBufferLines > MAX_TEXT_LINES) {
				int x = 0;
				int lines = 0;
				for (x = 0; (x < viewBufferSize)
						&& (lines <= viewBufferLines
								- (MAX_TEXT_LINES - LINE_REMOVAL_AMOUNT)); x++) {
					if (viewBufferFull.get(x).contains(
							Messages.getString("LiquidMudClientProActivity.9"))) { //$NON-NLS-1$
						lines++;
					}
				}

				viewBufferFull = viewBufferFull.subList(x,
						viewBufferFull.size() - 1);
				viewBufferFullColor = viewBufferFullColor.subList(x,
						viewBufferFullColor.size() - 1);
				viewBufferFullBGColor = viewBufferFullBGColor.subList(x,
						viewBufferFullBGColor.size() - 1);
				refreshView();

				textview.scrollTo(0,
						(textview.getLineCount() * textview.getLineHeight())
								- textview.getHeight());
			}
		}

		viewBufferFull.add(text);
		viewBufferFullColor.add(color);
		viewBufferFullBGColor.add(bgcolor);

		SpannableString viewSpanBuffer = new SpannableString(text);
		if (color != Color.WHITE)
			viewSpanBuffer.setSpan(new ForegroundColorSpan(color), 0,
					viewSpanBuffer.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (bgcolor != Color.BLACK)
			viewSpanBuffer.setSpan(new BackgroundColorSpan(bgcolor), 0,
					viewSpanBuffer.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		textview.append(viewSpanBuffer);

	}

	public void errormessage(String text) {
		addText(text, Color.RED, Color.BLUE);
	}

	private void LoadPrefs() {
		SharedPreferences prefs = getPreferences(0);

		int servers = prefs.getInt(
				Messages.getString("LiquidMudClientProActivity.10"), 0); //$NON-NLS-1$

		if (servers > 0) {
			for (int x = 1; x < servers + 1; x++) {
				ServerListing
						.add(new ServerInfo(
								prefs.getString(
										Messages.getString("LiquidMudClientProActivity.11") //$NON-NLS-1$
												+ x,
										Messages.getString("LiquidMudClientProActivity.12")), prefs.getString(Messages.getString("LiquidMudClientProActivity.13") + x, Messages.getString("LiquidMudClientProActivity.14")), prefs //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								.getInt(Messages
										.getString("LiquidMudClientProActivity.15") + x, 0))); //$NON-NLS-1$
			}
		} else {
			// SETUP DEFAULT SERVERS

		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		EditText cmd = (EditText) findViewById(R.id.cmdText);
		TextView textview = (TextView) findViewById(R.id.MainText);

		ServerListing = new ArrayList<ServerInfo>();

		ServerListing
				.add(new ServerInfo(
						Messages.getString("LiquidMudClientProActivity.16"), Messages.getString("LiquidMudClientProActivity.17"), 23)); //$NON-NLS-1$ //$NON-NLS-2$
		LoadPrefs();

		if (savedInstanceState == null) {

			// for(int x=0; x<100; x++)
			// {
			// viewBufferFull.add(((Character)'\n').toString());
			// viewBufferFullColor.add(Color.rgb(0,0,0));
			// }

			// ScrollView scroll = (ScrollView)findViewById(R.id.ScrollView01);
			// scroll.setVerticalScrollBarEnabled(true);
			// scroll.scrollTo(0, 1000);

			viewBufferFull = new ArrayList<String>();
			commandHistory = new ArrayList<String>();
			viewBufferFullColor = new ArrayList<Integer>();
			viewBufferFullBGColor = new ArrayList<Integer>();

			scrolllocked = false;

			if (connectionThread == null) {

				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			} else if (!this.connectionThread.isAlive()) {
				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			}
			sendData = new SendStack(50);
			// inputBuffer = "|";
		} else {
			viewBufferFull = ((TempData) this.getLastNonConfigurationInstance()).viewBufferFull;
			commandHistory = ((TempData) this.getLastNonConfigurationInstance()).commandHistory;
			viewBufferFullColor = ((TempData) this
					.getLastNonConfigurationInstance()).viewBufferFullColor;
			viewBufferFullBGColor = ((TempData) this
					.getLastNonConfigurationInstance()).viewBufferFullBGColor;

			connectionThread = ((TempData) this
					.getLastNonConfigurationInstance()).con;
			sendData = ((TempData) this.getLastNonConfigurationInstance()).stack;

			tlisten = new ThreadListener();
			sendData.push(tlisten);

			inputBuffer = savedInstanceState.getString(Messages
					.getString("LiquidMudClientProActivity.18")); //$NON-NLS-1$

			scrolllocked = false;

			if (connectionThread == null) {
				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			} else if (!this.connectionThread.isAlive()) {
				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			}

		}

		cmd.setText(inputBuffer);
		cmd.setOnKeyListener(new CommandListener());
		refreshView();
		textview.scrollTo(
				0,
				(textview.getLineCount() * textview.getLineHeight())
						- textview.getHeight());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_CONNECT, 0,
				Messages.getString("LiquidMudClientProActivity.19")); //$NON-NLS-1$
		menu.add(0, MENU_DISCONNECT, 0,
				Messages.getString("LiquidMudClientProActivity.20")); //$NON-NLS-1$
		return true;
	}

	@Override
	protected void onDestroy() {
		SavePrefs();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CONNECT:
			if (connectionThread == null) {
				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			} else if (!this.connectionThread.isAlive()) {
				ConnectReady cready = new OnReadyListener();
				ServerListDialog dialog = new ServerListDialog(this, cready,
						ServerListing);
				dialog.show();
			}
			return true;
		case MENU_DISCONNECT:
			if (connectionThread != null && this.connectionThread.isAlive()) {
				connectionThread.interrupt();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.connectionThread == null) {
			menu.findItem(MENU_CONNECT).setEnabled(true);
			menu.findItem(MENU_DISCONNECT).setEnabled(false);
		} else if (this.connectionThread.isAlive()) {
			menu.findItem(MENU_CONNECT).setEnabled(false);
			menu.findItem(MENU_DISCONNECT).setEnabled(true);
		} else {
			menu.findItem(MENU_CONNECT).setEnabled(true);
			menu.findItem(MENU_DISCONNECT).setEnabled(false);
		}
		return true;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		TempData temp = new TempData();
		temp.con = connectionThread;
		temp.stack = sendData;
		temp.viewBufferFull = viewBufferFull;
		temp.commandHistory = commandHistory;
		temp.viewBufferFullColor = viewBufferFullColor;
		temp.viewBufferFullBGColor = viewBufferFullBGColor;
		return temp;

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(
				Messages.getString("LiquidMudClientProActivity.21"), inputBuffer); //$NON-NLS-1$
		SavePrefs();
	}

	@Override
	public boolean onTouchEvent(MotionEvent motion) {
		TextView textview = (TextView) findViewById(R.id.MainText);
		switch (motion.getAction()) {
		case MotionEvent.ACTION_DOWN:
			scrollstart = textview.getScrollY();
			scrolly = motion.getY();
			break;
		case MotionEvent.ACTION_MOVE:

			float distance = (scrolly - motion.getY());
			float max_scroll = (textview.getLineCount() * textview
					.getLineHeight()) - textview.getHeight();

			if (distance + scrollstart > max_scroll) {

			} else if (distance + scrollstart <= 0) {

			} else {
				textview.scrollTo(0, Math.round(distance + scrollstart));

				if (textview.getScrollY() >= (max_scroll - 5)) {
					scrolllocked = false;
				} else {
					scrolllocked = true;
				}
			}
			scrollstart = textview.getScrollY();
			scrolly = motion.getY();
			break;
		default:
			break;
		}
		return false;
	}

	public void refreshView() {
		TextView textview = (TextView) findViewById(R.id.MainText);

		textview.setText(Messages.getString("LiquidMudClientProActivity.22")); //$NON-NLS-1$

		for (int x = 0; x < viewBufferFull.size(); x++) {
			SpannableString viewSpanBuffer = new SpannableString(
					viewBufferFull.get(x));
			if (viewBufferFullColor.get(x) != Color.WHITE)
				viewSpanBuffer.setSpan(new ForegroundColorSpan(
						viewBufferFullColor.get(x)), 0,
						viewSpanBuffer.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (viewBufferFullBGColor.get(x) != Color.BLACK)
				viewSpanBuffer.setSpan(new BackgroundColorSpan(
						viewBufferFullBGColor.get(x)), 0, viewSpanBuffer
						.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(viewSpanBuffer);
		}

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message m = new Message();
				m.what = TelnetConnectionThread.TEXT_SCROLL;
				TCUpdateHandler.sendMessage(m);
			}
		};
		textview.scrollTo(
				0,
				(textview.getLineCount() * textview.getLineHeight())
						- textview.getHeight());

	}

	private void SavePrefs() {
		SharedPreferences.Editor edit = getPreferences(0).edit();

		edit.putInt(
				Messages.getString("LiquidMudClientProActivity.23"), ServerListing.size() - 1); //$NON-NLS-1$

		for (int x = 1; x < ServerListing.size(); x++) {
			ServerInfo s = ServerListing.get(x);
			edit.putString(
					Messages.getString("LiquidMudClientProActivity.24") + x, s.ServerName); //$NON-NLS-1$
			edit.putString(
					Messages.getString("LiquidMudClientProActivity.25") + x, s.IP); //$NON-NLS-1$
			edit.putInt(
					Messages.getString("LiquidMudClientProActivity.26") + x, s.Port); //$NON-NLS-1$
		}
		edit.commit();
	}

}
