package org.secfirst.umbrella.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import org.secfirst.umbrella.BuildConfig;
import org.secfirst.umbrella.R;
import org.secfirst.umbrella.SettingsActivity;
import org.secfirst.umbrella.util.FolderChooserDialog;
import org.secfirst.umbrella.util.Global;
import org.secfirst.umbrella.util.OrmHelper;
import org.secfirst.umbrella.util.UmbrellaUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by dougl on 20/03/2018.
 */

public class DatabaseBackupDialog extends DialogFragment implements View.OnClickListener, Validator.ValidationListener {

    public static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;
    private TextView mOk;
    private TextView mCancel;
    @NotEmpty
    private EditText mFileName;
    public static String mPath;
    private RadioGroup mBackupTypeGroup;
    private RadioButton mShare;
    private RadioButton mExport;
    private CheckBox mWipeData;
    private View mView;
    private static boolean isCallbackShare = false;
    private static boolean isWipeData = false;
    private Validator mValidator;


    public static DatabaseBackupDialog newInstance() {
        Bundle args = new Bundle();
        DatabaseBackupDialog fragment = new DatabaseBackupDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCallbackShare && isWipeData) {
            wipeDatabase();
        } else if (isCallbackShare) {
            dismiss();
            Toast.makeText(getContext(), R.string.backup_file_shared, Toast.LENGTH_LONG).show();
            isCallbackShare = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.backup_database_dialog, container, false);
        mBackupTypeGroup = mView.findViewById(R.id.backup_type_group);
        mFileName = mView.findViewById(R.id.backup_file_name);
        mCancel = mView.findViewById(R.id.backup_cancel);
        mOk = mView.findViewById(R.id.backup_ok);
        mWipeData = mView.findViewById(R.id.backup_wipe_data);
        mShare = mView.findViewById(R.id.share_type);
        mExport = mView.findViewById(R.id.export_type);
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
        mOk.setTextColor(UmbrellaUtil.fetchAccentColor(getContext()));
        mCancel.setTextColor(UmbrellaUtil.fetchAccentColor(getContext()));

        initObject();
        return mView;
    }


    private void initObject() {
        mCancel.setOnClickListener(this);
        mOk.setOnClickListener(this);
        mBackupTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.share_type:

                    break;
                case R.id.export_type:
                    showFileChooserPreview();
                    break;
            }
        });
        mWipeData.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mView, R.string.write_external_permission_granted, Snackbar.LENGTH_SHORT)
                        .show();
                showFileChooserPreview();
            } else {
                // Permission request was denied.
                Snackbar.make(mView, R.string.write_external_permission_denied, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }


    private void showFileChooserPreview() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            showFileChooser();
        } else {
            requestExternalStoragePermission();
        }

    }

    private void showFileChooser() {
        new FolderChooserDialog.Builder(((SettingsActivity) getActivity()))
                .chooseButton(R.string.choose)
                .allowNewFolder(true, 0)
                .show();

    }

    private void requestExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(mView, R.string.write_external_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, view -> {
                // Request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }).show();

        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_EXTERNAL_STORAGE);
        }
    }

    private void shareDbFile(String fileName) {
        File databaseFile = getContext().getDatabasePath(OrmHelper.DATABASE_NAME);
        try {
            File dstDatabase = new File(Global.INSTANCE.getCacheDir().getPath() + "/" + fileName + ".db");
            UmbrellaUtil.copyFile(databaseFile, dstDatabase);
            Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, dstDatabase);
            Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                    .setType(getActivity().getContentResolver().getType(uri))
                    .setStream(uri)
                    .getIntent();
            //Provide read access
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PackageManager pm = getActivity().getPackageManager();

            if (shareIntent.resolveActivity(pm) != null)
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_form)));

            isCallbackShare = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backup_ok:
                mValidator.validate();
                break;
            case R.id.backup_cancel:
                dismiss();
                break;
            case R.id.backup_wipe_data:
                onWipeButton();
                break;
        }
    }

    private void onWipeButton() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.export_database_wipe_title)
                .content(R.string.export_database_wipe_content, true)
                .positiveText(R.string.ok)
                .show();
        isWipeData = mWipeData.isChecked();
    }

    private void onOkButton() {
        if (mShare.isChecked()) {
            shareDbFile(mFileName.getText().toString());
        } else if (mExport.isChecked()) {
            storeBackupFileIntoMemory(mPath);
            if (isWipeData) wipeDatabase();
            dismiss();

        }
    }

    private void wipeDatabase() {
        Global.INSTANCE.closeDbAndDAOs();
        Global.deleteDatabase(getContext().getDatabasePath(OrmHelper.DATABASE_NAME));
        Global.INSTANCE.removeSharedPreferences();
        isCallbackShare = false;
        UmbrellaUtil.doRestartApplication(getContext());
    }

    private void storeBackupFileIntoMemory(String path) {
        File dstDatabase = new File(path + "/" + mFileName.getText().toString() + ".db");
        File databaseFile = getContext().getDatabasePath(OrmHelper.DATABASE_NAME);
        try {
            UmbrellaUtil.copyFile(databaseFile, dstDatabase);
            Toast.makeText(getContext(), R.string.saved_backup, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.error_backup_store, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onValidationSucceeded() {
        onOkButton();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
        }
    }
}
