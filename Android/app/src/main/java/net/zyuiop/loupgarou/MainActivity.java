package net.zyuiop.loupgarou;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import net.zyuiop.loupgarou.network.NetworkManager;

public class MainActivity extends AppCompatActivity {

	private EditText address;
	private EditText port;
	private EditText username;
	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		address = (EditText) findViewById(R.id.host);
		port = (EditText) findViewById(R.id.port);
		username = (EditText) findViewById(R.id.username);
		button = (Button) findViewById(R.id.connect);
	}

	public void connect(View view) {
		final String host = address.getText().toString();
		final String port = this.port.getText().toString();
		final String username = this.username.getText().toString();

		if (host.isEmpty()) {
			address.setError(getString(R.string.error_host_empty));
			return;
		}
		if (port.isEmpty()) {
			this.port.setError(getString(R.string.error_port_empty));
			return;
		}
		if (username.isEmpty()) {
			this.username.setError(getString(R.string.error_username_empty));
			return;
		}

		button.setEnabled(false);

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				try {
					NetworkManager manager = new NetworkManager(username, host, Integer.parseInt(port));
					manager.setCurrentStage(MainActivity.this);
					manager.connect();
				} catch (final Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setMessage(e.getClass().getName() + " : " + e.getMessage())
									.setTitle(R.string.connection_error)
									.setNeutralButton("OK", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											dialogInterface.dismiss();
										}
									});
							AlertDialog dialog = builder.create();
							dialog.show();
							button.setEnabled(true);
						}
					});
				}
				return null;
			}
		}.execute();


	}

	public void loginFailure(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				button.setEnabled(true);
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(message)
						.setTitle(R.string.connection_error)
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
}
