package com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir.SelectDirectoryAdapter.OnDirectoryClickedListerner;
import com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir.HandleStorages.StorageType;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectFolderDialog extends Dialog implements OnClickListener, OnDirectoryClickedListerner, OnItemSelectedListener {
    public static OnDirectorySelectedListerner onDirectorySelectedListerner;
    private SelectDirectoryAdapter adapter;
    public File currentDir;
    
    public AlertDialog dialog;
    private ArrayList<File> directories;
    private boolean isExternalStorageSelected = false;
    
    public SharedPreferences prefs;
    private RecyclerView rv;
    private Spinner spinner;
    private List<HandleStorages> storages = new ArrayList();
    private TextView tv_currentDir;
    private TextView tv_empty;

    private class DirectoryFilter implements FileFilter {
        private DirectoryFilter() {
        }

        public boolean accept(File file) {
            return file.isDirectory() && !file.isHidden();
        }
    }

    private class SortFileName implements Comparator<File> {
        private SortFileName() {
        }

        public int compare(File file, File file2) {
            return file.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public SelectFolderDialog(@NonNull Context context) {
        super(context);
    }

    
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initialize();
    }

    private void initialize() {
        try {
            setContentView(R.layout.contetn_selectdirector);
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory());
            sb.append(File.separator);
            sb.append(CoderlyticsConstants.APPDIR);
            this.currentDir = new File(sb.toString());
            ContextCompat.getExternalFilesDirs(getContext().getApplicationContext(), null);
            this.storages.add(new HandleStorages(Environment.getExternalStorageDirectory().getPath(), StorageType.Internal));
            this.prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            generateFoldersList();
            initView();
            initRecyclerView();
        } catch (Exception unused) {
        }
    }

    private void initRecyclerView() {
        try {
            this.rv.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), 1, false);
            this.rv.setLayoutManager(linearLayoutManager);
            this.rv.addItemDecoration(new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation()));
            if (!isDirectoryEmpty()) {
                this.adapter = new SelectDirectoryAdapter(getContext(), this, this.directories);
                this.rv.setAdapter(this.adapter);
            }
            this.tv_currentDir.setText(this.currentDir.getPath());
        } catch (Exception unused) {
        }
    }

    private boolean isDirectoryEmpty() {
        if (this.directories.isEmpty()) {
            this.rv.setVisibility(View.GONE);
            this.tv_empty.setVisibility(View.VISIBLE);
            return true;
        }
        this.rv.setVisibility(View.VISIBLE);
        this.tv_empty.setVisibility(View.GONE);
        return false;
    }

    private void generateFoldersList() {
        try {
            this.directories = new ArrayList<>(Arrays.asList(this.currentDir.listFiles(new DirectoryFilter())));
            Collections.sort(this.directories, new SortFileName());
            String str = CoderlyticsConstants.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Directory size ");
            sb.append(this.directories.size());
            Log.d(str, sb.toString());
        } catch (Exception unused) {
        }
    }

    private void initView() {
        try {
            Button button = (Button) findViewById(R.id.btn_cancel);
            ((Button) findViewById(R.id.btn_ok)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!SelectFolderDialog.this.currentDir.canWrite()) {
                        Toast.makeText(SelectFolderDialog.this.getContext(), "Cannot write to selected directory. Path will not be saved.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PrefUtils.saveStringValue(SelectFolderDialog.this.getContext(), SelectFolderDialog.this.getContext().getString(R.string.savelocation_key), SelectFolderDialog.this.currentDir.getPath());
                    SelectFolderDialog.onDirectorySelectedListerner.onDirectorySelected();
                    SelectFolderDialog.this.dismiss();
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    SelectFolderDialog.this.dismiss();
                }
            });
            ImageButton imageButton = (ImageButton) findViewById(R.id.nav_up);
            ImageButton imageButton2 = (ImageButton) findViewById(R.id.create_dir);
            this.tv_currentDir = (TextView) findViewById(R.id.tv_selected_dir);
            this.rv = (RecyclerView) findViewById(R.id.rv);
            this.tv_empty = (TextView) findViewById(R.id.tv_empty);
            this.spinner = (Spinner) findViewById(R.id.storageSpinner);
            imageButton.setOnClickListener(this);
            imageButton2.setOnClickListener(this);
            ArrayList arrayList = new ArrayList();
            for (HandleStorages type : this.storages) {
                arrayList.add(type.getType() == StorageType.Internal ? "Internal Storage" : "Removable Storage");
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), 17367048, arrayList);
            arrayAdapter.setDropDownViewResource(17367049);
            this.spinner.setAdapter(arrayAdapter);
            this.spinner.setOnItemSelectedListener(this);
        } catch (Exception unused) {
        }
    }

    private void changeDirectory(File file) {
        try {
            this.currentDir = file;
            String str = CoderlyticsConstants.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Changed dir is: ");
            sb.append(file.getPath());
            Log.d(str, sb.toString());
            generateFoldersList();
            if (!isDirectoryEmpty()) {
                this.adapter = new SelectDirectoryAdapter(getContext(), this, this.directories);
                this.rv.swapAdapter(this.adapter, true);
            }
            this.tv_currentDir.setText(this.currentDir.getPath());
        } catch (Exception unused) {
        }
    }

    public void setCurrentDir(String str) {
        try {
            File file = new File(str);
            boolean exists = file.exists();
            String str2 = CoderlyticsConstants.TAG;
            if (exists) {
                if (file.isDirectory()) {
                    this.currentDir = file;
                    Log.d(str2, "Directory set");
                    return;
                }
            }
            createFolder(file.getPath());
            Log.d(str2, "Directory created");
        } catch (Exception unused) {
        }
    }

    public void setOnDirectoryClickedListerner(OnDirectorySelectedListerner onDirectorySelectedListerner2) {
        onDirectorySelectedListerner = onDirectorySelectedListerner2;
    }

    private void newDirDialog(Bundle bundle) {
        try {
            View inflate = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.contetn_edittext_directory, null);
            final EditText editText = (EditText) inflate.findViewById(R.id.et_new_folder);
            editText.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                public void afterTextChanged(Editable editable) {
                    if (SelectFolderDialog.this.dialog != null) {
                        SelectFolderDialog.this.dialog.getButton(-1).setEnabled(!editable.toString().trim().isEmpty());
                    }
                }
            });
            this.dialog = new Builder(getContext()).setTitle(R.string.alert_title_create_folder).setMessage(R.string.alert_message_create_folder).setView(inflate).setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    String trim = editText.getText().toString().trim();
                    if (!trim.isEmpty()) {
                        SelectFolderDialog.this.createFolder(trim);
                    }
                }
            }).create();
            if (bundle != null) {
                this.dialog.onRestoreInstanceState(bundle);
            }
            this.dialog.show();
            this.dialog.getButton(-1).setEnabled(!editText.getText().toString().trim().isEmpty());
        } catch (Exception unused) {
        }
    }

    
    public boolean createFolder(String str) {
        File file;
        try {
            if (this.currentDir == null)
            {
                Toast.makeText(getContext(), "No directory selected", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if (!this.currentDir.canWrite())
            {
                Toast.makeText(getContext(), getContext().getString(R.string.error_permission_make_dir), Toast.LENGTH_SHORT).show();
                return false;
            }
            else
                {
                if (str.contains(Environment.getExternalStorageDirectory().getPath())) {
                    file = new File(str);
                } else {
                    file = new File(this.currentDir, str);
                }
                if (file.exists()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.dir_exist), Toast.LENGTH_SHORT).show();
                    changeDirectory(new File(this.currentDir, str));
                    return false;
                } else if (!file.mkdir())
                {
                    Toast.makeText(getContext(), "Error creating directory", Toast.LENGTH_SHORT).show();
                    Log.d(CoderlyticsConstants.TAG, file.getPath());
                    return false;
                } else
                    {
                    changeDirectory(new File(this.currentDir, str));
                    return true;
                }
            }
        } catch (Exception unused) {
        }
        return false;
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.create_dir) {
            newDirDialog(null);
        } else if (id == R.id.nav_up) {
            try {
                File file = new File(this.currentDir.getParent());
                Log.d(CoderlyticsConstants.TAG, file.getPath());
                if (this.isExternalStorageSelected) {
                    changeExternalDirectory(file);
                } else if (file.getPath().contains(((HandleStorages) this.storages.get(0)).getPath())) {
                    changeDirectory(file);
                }
            } catch (Exception unused) {
            }
        }
    }

    private void changeExternalDirectory(File file) {
        try {
            String removableSDPath = getRemovableSDPath(((HandleStorages) this.storages.get(1)).getPath());
            if (file.getPath().contains(removableSDPath) && file.canWrite()) {
                changeDirectory(file);
            } else if (file.getPath().contains(removableSDPath) && !file.canWrite()) {
                Toast.makeText(getContext(), R.string.external_storage_dir_not_writable, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception unused) {
        }
    }

    private String getRemovableSDPath(String str) {
        int indexOf = str.indexOf("Android");
        StringBuilder sb = new StringBuilder();
        sb.append("Short code is: ");
        sb.append(str.substring(0, indexOf));
        String sb2 = sb.toString();
        String str2 = CoderlyticsConstants.TAG;
        Log.d(str2, sb2);
        String substring = str.substring(0, indexOf - 1);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("External Base Dir ");
        sb3.append(substring);
        Log.d(str2, sb3.toString());
        return substring;
    }

    public void OnDirectoryClicked(File file) {
        changeDirectory(file);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
        StringBuilder sb = new StringBuilder();
        sb.append("Selected storage is: ");
        sb.append(this.storages.get(i));
        Log.d(CoderlyticsConstants.TAG, sb.toString());
        this.isExternalStorageSelected = ((HandleStorages) this.storages.get(i)).getType() == StorageType.External;
        if (this.isExternalStorageSelected && !this.prefs.getBoolean(CoderlyticsConstants.ALERT_EXTR_STORAGE_CB_KEY, false)) {
            showExtDirAlert();
        }
        changeDirectory(new File(((HandleStorages) this.storages.get(i)).getPath()));
    }

    private void showExtDirAlert() {
        try {
            View inflate = View.inflate(getContext(), R.layout.content_alert_checkbox, null);
            final CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.donot_warn_cb);
            new Builder(getContext()).setTitle(R.string.alert_ext_dir_warning_title).setMessage(R.string.alert_ext_dir_warning_message).setView(inflate).setNeutralButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        if (checkBox.isChecked()) {
                            SelectFolderDialog.this.prefs.edit().putBoolean(CoderlyticsConstants.ALERT_EXTR_STORAGE_CB_KEY, true).apply();
                        }
                    } catch (Exception unused) {
                    }
                }
            }).create().show();
        } catch (Exception unused) {
        }
    }
}
