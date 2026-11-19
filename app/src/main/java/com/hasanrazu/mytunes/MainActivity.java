
package com.hasanrazu.mytunes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_FOLDER = 1001;
    private static final String PREFS_NAME = "MyTunesPreferences";
    private static final String KEY_FOLDER_URI = "music_folder_uri";

    private FrameLayout contentContainer;
    private BottomNavigationView bottomNavigation;

    private Uri treeUri;

    private final ArrayList<String> folderIds = new ArrayList<>();
    private final ArrayList<String> folderNames = new ArrayList<>();

    private TextView txtFolderPath;
    private TextView btnBackFolder;
    private LinearLayout listLocalFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentContainer = findViewById(R.id.contentContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupBackNavigation();
        setupNavigation();
        showHomeScreen();

        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v ->
                showMessage("Search will be implemented later."));
    }

    private void setupNavigation() {

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showHomeScreen();
                return true;
            }

            if (id == R.id.nav_local) {
                showLocalScreen();
                return true;
            }

            if (id == R.id.nav_cloud) {
                showMessage("Google Drive integration will be implemented later.");
                return true;
            }

            if (id == R.id.nav_favorites) {
                showMessage("Favorites will be implemented later.");
                return true;
            }

            return false;
        });
    }

    private void showHomeScreen() {

        contentContainer.removeAllViews();

        View homeView = getLayoutInflater()
                .inflate(R.layout.view_home, contentContainer, false);

        contentContainer.addView(homeView);

        MaterialCardView cardLocal = homeView.findViewById(R.id.cardLocal);
        MaterialCardView cardCloud = homeView.findViewById(R.id.cardCloud);
        MaterialCardView cardFavorites = homeView.findViewById(R.id.cardFavorites);
        MaterialCardView cardPlaylists = homeView.findViewById(R.id.cardPlaylists);
        MaterialCardView cardSettings = homeView.findViewById(R.id.cardSettings);

        cardLocal.setOnClickListener(v ->
                bottomNavigation.setSelectedItemId(R.id.nav_local));

        cardCloud.setOnClickListener(v ->
                showMessage("Google Drive integration will be implemented later."));

        cardFavorites.setOnClickListener(v ->
                bottomNavigation.setSelectedItemId(R.id.nav_favorites));

        cardPlaylists.setOnClickListener(v ->
                showMessage("Playlists will be implemented later."));

        cardSettings.setOnClickListener(v ->
                showMessage("Settings will be implemented later."));
    }

    private void showLocalScreen() {

        contentContainer.removeAllViews();

        View localView = getLayoutInflater()
                .inflate(R.layout.view_local, contentContainer, false);

        contentContainer.addView(localView);

        txtFolderPath = localView.findViewById(R.id.txtFolderPath);
        btnBackFolder = localView.findViewById(R.id.btnBackFolder);
        listLocalFiles = localView.findViewById(R.id.listLocalFiles);

        localView.findViewById(R.id.btnChooseFolder)
                .setOnClickListener(v -> openFolderPicker());

        btnBackFolder.setOnClickListener(v -> goToParentFolder());

        loadSavedFolder();
    }

    private void openFolderPicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(intent, REQUEST_SELECT_FOLDER);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_SELECT_FOLDER
                || resultCode != Activity.RESULT_OK
                || data == null) {
            return;
        }

        Uri selectedUri = data.getData();

        if (selectedUri == null) {
            showMessage("Unable to access the selected folder.");
            return;
        }

        int permissionFlags = data.getFlags();

        // Check whether read permission was granted.
        if ((permissionFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            showMessage("Read permission was not granted.");
            return;
        }

        try {
            // Persist read permission.
            getContentResolver().takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            // Persist write permission only if it was granted.
            if ((permissionFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
                getContentResolver().takePersistableUriPermission(
                        selectedUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                );
            }

            // Save the selected folder URI.
            treeUri = selectedUri;

            SharedPreferences preferences =
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            preferences.edit()
                    .putString(KEY_FOLDER_URI, selectedUri.toString())
                    .apply();

            // Reset folder navigation.
            folderIds.clear();
            folderNames.clear();

            // Add the selected folder as the root folder.
            String rootId = DocumentsContract.getTreeDocumentId(treeUri);

            folderIds.add(rootId);
            folderNames.add(getDocumentName(rootId));

            // Display the selected folder contents.
            displayCurrentFolder();

        } catch (SecurityException e) {
            showMessage("Folder permission could not be saved.");
        } catch (Exception e) {
            showMessage("Unable to load the selected folder.");
        }
    }



    private void loadSavedFolder() {

        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedUri = preferences.getString(KEY_FOLDER_URI, null);

        if (savedUri == null) {
            txtFolderPath.setText("No folder selected");
            addMessage("Choose a music folder to begin.");
            return;
        }

        try {
            treeUri = Uri.parse(savedUri);

            String rootId =
                    DocumentsContract.getTreeDocumentId(treeUri);

            folderIds.clear();
            folderNames.clear();

            folderIds.add(rootId);
            folderNames.add(getDocumentName(rootId));

            displayCurrentFolder();

        } catch (Exception e) {
            txtFolderPath.setText("Folder access unavailable");
            addMessage("Please select your music folder again.");
        }
    }

    private void displayCurrentFolder() {

        if (treeUri == null || folderIds.isEmpty()) {
            return;
        }

        String currentId = folderIds.get(folderIds.size() - 1);
        String currentName = folderNames.get(folderNames.size() - 1);

        txtFolderPath.setText("Folder: " + currentName);

        btnBackFolder.setVisibility(
                folderIds.size() > 1 ? View.VISIBLE : View.GONE
        );

        listLocalFiles.removeAllViews();

        ArrayList<LocalFile> files = getFolderContents(currentId);

        if (files.isEmpty()) {
            addMessage("No folders or audio files found here.");
            return;
        }

        for (LocalFile file : files) {
            addFileRow(file);
        }
    }

    private ArrayList<LocalFile> getFolderContents(String parentId) {

        ArrayList<LocalFile> result = new ArrayList<>();

        Uri childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(
                        treeUri,
                        parentId
                );

        String[] projection = {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
        };

        try (Cursor cursor = getContentResolver().query(
                childrenUri,
                projection,
                null,
                null,
                null
        )) {

            if (cursor == null) {
                return result;
            }

            int idIndex = cursor.getColumnIndex(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID);

            int nameIndex = cursor.getColumnIndex(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME);

            int mimeIndex = cursor.getColumnIndex(
                    DocumentsContract.Document.COLUMN_MIME_TYPE);

            int sizeIndex = cursor.getColumnIndex(
                    DocumentsContract.Document.COLUMN_SIZE);

            while (cursor.moveToNext()) {

                String id = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                String mime = cursor.getString(mimeIndex);

                long size = sizeIndex >= 0 && !cursor.isNull(sizeIndex)
                        ? cursor.getLong(sizeIndex) : 0;

                boolean isFolder =
                        DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);

                boolean isAudio = isAudioFile(name, mime);

                if (isFolder || isAudio) {
                    result.add(new LocalFile(
                            id,
                            name,
                            isFolder,
                            size
                    ));
                }
            }

        } catch (Exception e) {
            showMessage("Unable to read this folder.");
        }

        Collections.sort(result, (first, second) -> {

            if (first.isFolder != second.isFolder) {
                return first.isFolder ? -1 : 1;
            }

            return first.name.compareToIgnoreCase(second.name);
        });

        return result;
    }

    private boolean isAudioFile(String name, String mime) {

        if (mime != null && mime.toLowerCase(Locale.ROOT).startsWith("audio/")) {
            return true;
        }

        if (name == null) {
            return false;
        }

        String lowerName = name.toLowerCase(Locale.ROOT);

        String[] extensions = {
                ".mp3", ".m4a", ".aac", ".ogg",
                ".opus", ".wav", ".flac", ".amr", ".wma"
        };

        for (String extension : extensions) {
            if (lowerName.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    private void addFileRow(LocalFile file) {

        TextView row = new TextView(this);

        String prefix = file.isFolder ? "📁  " : "♫  ";
        String detail = file.isFolder
                ? "Folder"
                : formatFileSize(file.size);

        row.setText(prefix + file.name + "\n      " + detail);
        row.setTextColor(Color.WHITE);
        row.setTextSize(15);
        row.setPadding(16, 16, 16, 16);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(
                android.R.drawable.list_selector_background
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = 4;
        row.setLayoutParams(params);

        row.setOnClickListener(v -> {

            if (file.isFolder) {

                folderIds.add(file.id);
                folderNames.add(file.name);

                displayCurrentFolder();

            } else {

                showMessage(
                        "Song found: " + file.name
                                + ". Playback will be added in Phase 4."
                );
            }
        });

        listLocalFiles.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#252A36"));

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        listLocalFiles.addView(divider, dividerParams);
    }

    private void goToParentFolder() {

        if (folderIds.size() > 1) {

            folderIds.remove(folderIds.size() - 1);
            folderNames.remove(folderNames.size() - 1);

            displayCurrentFolder();
        }
    }

    private String getDocumentName(String documentId) {

        Uri documentUri =
                DocumentsContract.buildDocumentUriUsingTree(
                        treeUri,
                        documentId
                );

        String[] projection = {
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
        };

        try (Cursor cursor = getContentResolver().query(
                documentUri,
                projection,
                null,
                null,
                null
        )) {

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }

        } catch (Exception ignored) {
            // Use a fallback name below.
        }

        return "Music Folder";
    }

    private String formatFileSize(long size) {

        if (size <= 0) {
            return "Audio file";
        }

        if (size < 1024 * 1024) {
            return String.format(Locale.getDefault(),
                    "%.1f KB", size / 1024.0);
        }

        return String.format(Locale.getDefault(),
                "%.1f MB", size / (1024.0 * 1024.0));
    }

    private void addMessage(String message) {

        TextView textView = new TextView(this);

        textView.setText(message);
        textView.setTextColor(Color.parseColor("#A5A9B5"));
        textView.setTextSize(14);
        textView.setPadding(12, 20, 12, 20);

        listLocalFiles.addView(textView);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupBackNavigation() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (bottomNavigation.getSelectedItemId()
                                == R.id.nav_local) {

                            if (folderIds.size() > 1) {
                                goToParentFolder();
                            } else {
                                bottomNavigation.setSelectedItemId(
                                        R.id.nav_home
                                );
                            }

                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
        );
    }

    private static class LocalFile {

        final String id;
        final String name;
        final boolean isFolder;
        final long size;

        LocalFile(String id, String name, boolean isFolder, long size) {
            this.id = id;
            this.name = name;
            this.isFolder = isFolder;
            this.size = size;
        }
    }
}
