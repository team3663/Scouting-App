package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.PostMatchBinding;

public class PostMatch extends AppCompatActivity {
    // =============================================================================================
    // Constants
    // =============================================================================================


    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PostMatchBinding postMatchBinding;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        postMatchBinding = PostMatchBinding.inflate(getLayoutInflater());
        View page_root_view = postMatchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(postMatchBinding.postMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create a button for when you are done inputting info
        Button but_Next = postMatchBinding.butNext;
        but_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GoToSubmitData = new Intent(PostMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);
            }
        });
    }
}