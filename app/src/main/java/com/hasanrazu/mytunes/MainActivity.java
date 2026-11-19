
package com.hasanrazu.mytunes;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupNavigation();
        setupClickListeners();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupNavigation() {

        bottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;

            } else if (itemId == R.id.nav_local) {
                showMessage("Local Music will be implemented in Phase 3.");

            } else if (itemId == R.id.nav_cloud) {
                showMessage("Cloud Music will be implemented in a later phase.");

            } else if (itemId == R.id.nav_favorites) {
                showMessage("Favorites will be implemented in a later phase.");
            }

            return true;
        });
    }

    private void setupClickListeners() {

        MaterialCardView cardLocal = findViewById(R.id.cardLocal);
        MaterialCardView cardCloud = findViewById(R.id.cardCloud);
        MaterialCardView cardFavorites = findViewById(R.id.cardFavorites);
        MaterialCardView cardPlaylists = findViewById(R.id.cardPlaylists);
        MaterialCardView cardSettings = findViewById(R.id.cardSettings);

        ImageButton btnSearch = findViewById(R.id.btnSearch);

        cardLocal.setOnClickListener(v ->
                showMessage("Local Music will be implemented in Phase 3."));

        cardCloud.setOnClickListener(v ->
                showMessage("Google Drive integration will be implemented later."));

        cardFavorites.setOnClickListener(v ->
                showMessage("Favorites will be implemented later."));

        cardPlaylists.setOnClickListener(v ->
                showMessage("Playlists will be implemented later."));

        cardSettings.setOnClickListener(v ->
                showMessage("Settings will be implemented later."));

        btnSearch.setOnClickListener(v ->
                showMessage("Search will be implemented later."));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
