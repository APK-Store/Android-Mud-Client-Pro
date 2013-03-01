package com.liquid.telnet.client.pro;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConnectionDialog extends Dialog {

	private class ConnectListener implements android.view.View.OnClickListener {

		// @Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			EditText NameEdit = (EditText) findViewById(R.id.NameValue);
			EditText HostEdit = (EditText) findViewById(R.id.HostValue);
			EditText PortEdit = (EditText) findViewById(R.id.PortValue);
			Button saveButton = (Button) findViewById(R.id.SaveButton);

			if (saveButton.getText().toString()
					.compareTo(Messages.getString("ConnectionDialog.0")) == 0) { //$NON-NLS-1$
				serveradd.Add(NameEdit.getText().toString(), HostEdit.getText()
						.toString(), Integer.valueOf(PortEdit.getText()
						.toString()));
			} else {
				serveradd.Modify(position, NameEdit.getText().toString(),
						HostEdit.getText().toString(),
						Integer.valueOf(PortEdit.getText().toString()));
			}
			ConnectionDialog.this.dismiss();
		}
	}

	public interface ServerAdd {
		public void Add(String name, String H, int p);

		public void Modify(int pos, String name, String H, int p);
	}

	private String Host;
	private String Name;
	private int Port;

	int position = 0;

	ServerAdd serveradd;

	public ConnectionDialog(Context context, int pos, ServerAdd serverModFunc,
			String N, String H, int P) {
		super(context);
		position = pos;
		Name = N;
		Host = H;
		Port = P;
		this.serveradd = serverModFunc;
		// TODO Auto-generated constructor stub
	}

	public ConnectionDialog(Context context, ServerAdd serverAddFunc) {
		super(context);
		this.serveradd = serverAddFunc;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);
		setTitle(Messages.getString("ConnectionDialog.1")); //$NON-NLS-1$

		Button saveButton = (Button) findViewById(R.id.SaveButton);

		if (position > 0) {

			EditText NameEdit = (EditText) findViewById(R.id.NameValue);
			EditText HostEdit = (EditText) findViewById(R.id.HostValue);
			EditText PortEdit = (EditText) findViewById(R.id.PortValue);
			NameEdit.setText(Name);
			HostEdit.setText(Host);
			PortEdit.setText(String.valueOf(Port));
			saveButton.setText(Messages.getString("ConnectionDialog.2")); //$NON-NLS-1$
		} else {
			saveButton.setText(Messages.getString("ConnectionDialog.3")); //$NON-NLS-1$
		}

		saveButton.setOnClickListener(new ConnectListener());
	}

}
