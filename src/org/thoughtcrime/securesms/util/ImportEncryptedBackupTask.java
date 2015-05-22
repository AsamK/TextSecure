package org.thoughtcrime.securesms.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.EncryptedBackupExporter;
import org.thoughtcrime.securesms.database.NoExternalStorageException;

import java.io.IOException;

public class ImportEncryptedBackupTask extends AsyncTask<Void, Void, Integer> {
  private final Context context;

  private static final int SUCCESS    = 0;
  private static final int NO_SD_CARD = 1;
  private static final int ERROR_IO   = 2;

  private ProgressDialog progressDialog;

  public ImportEncryptedBackupTask(Context context) {
    this.context = context;
  }

  @Override
  protected void onPreExecute() {
    progressDialog = ProgressDialog.show(context,
            context.getString(R.string.ImportFragment_restoring),
            context.getString(R.string.ImportFragment_restoring_encrypted_backup),
            true, false);
  }

  @Override
  protected void onPostExecute(Integer result) {
    if (progressDialog != null)
      progressDialog.dismiss();

    if (context == null)
      return;

    switch (result) {
      case NO_SD_CARD:
        Toast.makeText(context,
                context.getString(R.string.ImportFragment_no_encrypted_backup_found),
                Toast.LENGTH_LONG).show();
        break;
      case ERROR_IO:
        Toast.makeText(context,
                context.getString(R.string.ImportFragment_error_importing_backup),
                Toast.LENGTH_LONG).show();
        break;
      case SUCCESS:
        DatabaseFactory.getInstance(context).reset(context);
        //Intent intent = new Intent(context, KeyCachingService.class);
        //intent.setAction(KeyCachingService.CLEAR_KEY_ACTION);
        //context.startService(intent);

        Toast.makeText(context,
                context.getString(R.string.ImportFragment_restore_complete),
                Toast.LENGTH_LONG).show();
        // Make sure the gcm registration is renewed after
        TextSecurePreferences.setGcmRegistrationId(context, null);
        // TODO Info dialog for user to restart the app
        // Make sure the app uses the new files
        System.exit(0);
    }
  }

  @Override
  protected Integer doInBackground(Void... params) {
    try {
      EncryptedBackupExporter.importFromSd(context);
      return SUCCESS;
    } catch (NoExternalStorageException e) {
      Log.w("ImportFragment", e);
      return NO_SD_CARD;
    } catch (IOException e) {
      Log.w("ImportFragment", e);
      return ERROR_IO;
    }
  }
}
