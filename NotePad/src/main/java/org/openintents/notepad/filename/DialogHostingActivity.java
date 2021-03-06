package org.openintents.notepad.filename;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.File;

import org.openintents.distribution.DownloadOIAppDialog;
import org.openintents.intents.FileManagerIntents;
import org.openintents.notepad.PrivateNotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.filename.FilenameDialog.OnFilenamePickedListener;
import org.openintents.notepad.util.FileUriUtils;
import org.openintents.util.IntentUtils;

public class DialogHostingActivity extends Activity {

    public static final int DIALOG_ID_SAVE = 1;
    public static final int DIALOG_ID_OPEN = 2;
    public static final int DIALOG_ID_NO_FILE_MANAGER_AVAILABLE = 3;
    public static final int DIALOG_ID_UPLOAD = 4;
    public static final String EXTRA_DIALOG_ID = "org.openintents.notepad.extra.dialog_id";
    public static final String EXTRA_FILENAME = "org.openintents.notepad.extra.filename";
    private static final String TAG = "FilenameActivity";
    private static final boolean DEBUG = false;
    EditText mEditText;
    String mFilename;
    OnFilenamePickedListener mFilenamePickedListener = new OnFilenamePickedListener() {

        public void onFilenamePicked(String filename) {
            if (DEBUG) {
                Log.d(TAG, "Filename picked: " + filename);
            }

            Intent intent = getIntent();
            Uri uri = FileUriUtils.getUri(new File(filename));
            intent.setData(uri);

            setResult(RESULT_OK, intent);
        }

    };
    OnFilenamePickedListener mFilenamePickedForUploadListener = new OnFilenamePickedListener() {

        public void onFilenamePicked(String filename) {
            if (DEBUG) {
                Log.d(TAG, "Filename picked: " + filename);
            }

            Intent intent = getIntent();
            intent.putExtra(EXTRA_FILENAME, filename);

            setResult(RESULT_OK, intent);
        }

    };
    /**
     * Whether dialog is simply pausing while hidden by another activity or when
     * configuration changes. If this is false, then we can safely finish this
     * activity if a dialog gets dismissed.
     */
    private boolean mIsPausing = false;
    OnDismissListener mDismissListener = new OnDismissListener() {

        public void onDismiss(DialogInterface dialoginterface) {
            if (DEBUG) {
                Log.d(TAG, "Dialog dismissed. Pausing: " + mIsPausing);
            }
            if (!mIsPausing) {
                if (DEBUG) {
                    Log.d(TAG, "finish");
                }
                // Dialog has been dismissed by user.
                DialogHostingActivity.this.finish();
            } else {
                // Probably just a screen orientation change. Don't finish yet.
                // Dialog has been dismissed by system.
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        if (i != null && savedInstanceState == null) {
            if (DEBUG) {
                Log.d(TAG, "new dialog");
            }
            int dialogId = i.getIntExtra(EXTRA_DIALOG_ID, 0);
            switch (dialogId) {
                case DIALOG_ID_SAVE:
                    if (DEBUG) {
                        Log.i(TAG, "Show Save dialog");
                    }
                    saveFile();
                    break;
                case DIALOG_ID_OPEN:
                    if (DEBUG) {
                        Log.i(TAG, "Show Open dialog");
                    }
                    openFile();
                    break;
                case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
                    if (DEBUG) {
                        Log.i(TAG, "Show no file manager dialog");
                    }
                    showDialog(DIALOG_ID_NO_FILE_MANAGER_AVAILABLE);
                    break;
                case DIALOG_ID_UPLOAD:
                    if (DEBUG) {
                        Log.i(TAG, "Show Save dialog");
                    }
                    uploadFile();
                    break;
            }
        }
    }

    /**
     *
     */
    private void saveFile() {

        Uri uri = getIntent().getData();
        if (uri != null) {
            // Check whether intent exists
            Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
            intent.setData(uri);
            if (IntentUtils.isIntentAvailable(this, intent)) {
                intent.putExtra(
                        PrivateNotePadIntents.EXTRA_URI, getIntent()
                                .getStringExtra(PrivateNotePadIntents.EXTRA_URI)
                );
                intent.putExtra(
                        FileManagerIntents.EXTRA_TITLE,
                        getText(R.string.menu_save_to_sdcard)
                );
                intent.putExtra(
                        FileManagerIntents.EXTRA_BUTTON_TEXT,
                        getText(R.string.save)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            } else {
                mFilename = FileUriUtils.getFilename(getIntent().getData());
                showDialog(DIALOG_ID_SAVE);
            }
        } else {
            mFilename = getIntent().getStringExtra(EXTRA_FILENAME);
            showDialog(DIALOG_ID_SAVE);
        }
    }

    private void openFile() {

        // Check whether intent exists
        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
        intent.setData(getIntent().getData());
        if (IntentUtils.isIntentAvailable(this, intent)) {
            intent.putExtra(
                    PrivateNotePadIntents.EXTRA_URI, getIntent()
                            .getStringExtra(PrivateNotePadIntents.EXTRA_URI)
            );
            intent.putExtra(
                    FileManagerIntents.EXTRA_TITLE,
                    getText(R.string.menu_open_from_sdcard)
            );
            intent.putExtra(
                    FileManagerIntents.EXTRA_BUTTON_TEXT,
                    getText(R.string.open)
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        } else {
            mFilename = FileUriUtils.getFilename(getIntent().getData());
            showDialog(DIALOG_ID_OPEN);
        }
    }

    private void uploadFile() {
        mFilename = getIntent().getStringExtra(EXTRA_FILENAME);
        showDialog(DIALOG_ID_UPLOAD);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {
            case DIALOG_ID_SAVE:
                FilenameDialog fd = new FilenameDialog(this, true);
                fd.setTitle(R.string.menu_save_to_sdcard);
                fd.setFilename(mFilename);
                fd.setOnFilenamePickedListener(mFilenamePickedListener);
                dialog = fd;
                break;
            case DIALOG_ID_OPEN:
                fd = new FilenameDialog(this, true);
                fd.setTitle(R.string.menu_open_from_sdcard);
                fd.setFilename(mFilename);
                fd.setOnFilenamePickedListener(mFilenamePickedListener);
                dialog = fd;
                break;
            case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
                dialog = new DownloadOIAppDialog(
                        this,
                        DownloadOIAppDialog.OI_FILEMANAGER
                );
                break;
            case DIALOG_ID_UPLOAD:
                fd = new FilenameDialog(this, false);
                fd.setTitle(R.string.menu_upload);
                fd.setFilename(mFilename);
                fd.setOnFilenamePickedListener(mFilenamePickedForUploadListener);
                dialog = fd;
                break;
        }
        if (dialog == null) {
            dialog = super.onCreateDialog(id);
        }
        if (dialog != null) {
            dialog.setOnDismissListener(mDismissListener);
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
            case DIALOG_ID_SAVE:
                break;
            case DIALOG_ID_OPEN:
                break;
            case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
                DownloadOIAppDialog.onPrepareDialog(this, dialog);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) {
            Log.d(TAG, "onSaveInstanceState");
        }
        // It is important to set mIsPausing here, so that
        // the dialog does not get closed on orientation changes.
        mIsPausing = true;
        if (DEBUG) {
            Log.d(TAG, "onSaveInstanceState. Pausing: " + mIsPausing);
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(TAG, "onResume");
        }
        super.onResume();
        // In case another activity is called, and we are resumed,
        // mIsPausing should be reset to its original state.
        mIsPausing = false;
    }

}
