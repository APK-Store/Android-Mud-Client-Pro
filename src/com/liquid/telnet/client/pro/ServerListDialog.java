package com.liquid.telnet.client.pro;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.liquid.telnet.client.pro.LiquidMudClientProActivity.ServerInfo;

public class ServerListDialog extends Dialog {

	public interface ConnectReady {
		public void connect(int pos);

		public void delete(int pos);

		public void modify(int pos);
	}

	private class ItemLongSelectedListener implements OnItemLongClickListener {

		// @Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			contextSelect = position;
			if (contextSelect > 0)
				alert2.show();
			return true;
		}

	}

	private class ItemSelectedListener implements OnItemClickListener {

		// @Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			cready.connect(position);

			ServerListDialog.this.dismiss();

		}
	}

	private Builder alert2;

	int contextSelect = 0;

	ConnectReady cready;

	ArrayList<ServerInfo> itemlist;

	public ServerListDialog(Context context, ConnectReady cfunc,
			ArrayList<ServerInfo> sinfo) {
		super(context);
		this.cready = cfunc;
		itemlist = sinfo;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.serverlist);
		setTitle(Messages.getString("ServerListDialog.0")); //$NON-NLS-1$

		ListView list = (ListView) findViewById(R.id.ListView01);

		list.setAdapter(new ArrayAdapter<ServerInfo>(this.getContext(),
				R.layout.test, itemlist));
		list.setTextFilterEnabled(true);

		list.setOnItemClickListener(new ItemSelectedListener());
		list.setOnItemLongClickListener(new ItemLongSelectedListener());

		setupAlerts();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(Messages.getString("ServerListDialog.1")); //$NON-NLS-1$
	}

	private void setupAlerts() {
		alert2 = new AlertDialog.Builder(this.getContext())
				.setTitle(Messages.getString("ServerListDialog.2")) //$NON-NLS-1$
				.setMessage(Messages.getString("ServerListDialog.3")) //$NON-NLS-1$
				.setPositiveButton(Messages.getString("ServerListDialog.4"), //$NON-NLS-1$
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								cready.modify(contextSelect);
								ServerListDialog.this.dismiss();
							}
						})
				.setNeutralButton(Messages.getString("ServerListDialog.5"), //$NON-NLS-1$
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								cready.connect(contextSelect);
								ServerListDialog.this.dismiss();
							}
						})
				.setNegativeButton(Messages.getString("ServerListDialog.6"), //$NON-NLS-1$
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								cready.delete(contextSelect);
								ServerListDialog.this.dismiss();
							}
						});

	}
}
