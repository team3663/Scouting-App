package com.cpr3663.cpr_timers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_timers.databinding.StartPageBinding;

public class StartPage extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private StartPageBinding startpageBinding;
    // To store the inputted name
    public static String NAME_SCOUTER;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        startpageBinding = StartPageBinding.inflate(getLayoutInflater());
        View page_root_view = startpageBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(startpageBinding.startPage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create a input text box
        EditText edit_Name = startpageBinding.editName;
        edit_Name.setText(NAME_SCOUTER);
        edit_Name.setTextColor(Color.BLACK);
        edit_Name.setHint("Input your Name:");
        edit_Name.setHintTextColor(Color.GRAY);
        edit_Name.setTextSize(20F);
        edit_Name.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        edit_Name.setX(-60F);
        edit_Name.setY(500F);
        ViewGroup.LayoutParams text_Time_LP = new ViewGroup.LayoutParams(2_620, 100);
        edit_Name.setLayoutParams(text_Time_LP);
        edit_Name.setBackgroundColor(Color.TRANSPARENT);

        // Create a button for when you are done inputting info
        Button but_SubmitPage1 = startpageBinding.butSubmitPage1;
        but_SubmitPage1.setText("Submit");
        but_SubmitPage1.setTextSize(24F);
        but_SubmitPage1.setTextColor(Color.WHITE);
        but_SubmitPage1.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        but_SubmitPage1.setX(1100F);
        but_SubmitPage1.setY(1000F);
        ViewGroup.LayoutParams but_Update_LP = new ViewGroup.LayoutParams(300, 200);
        but_SubmitPage1.setLayoutParams(but_Update_LP);
        but_SubmitPage1.setBackgroundColor(Color.BLACK);

        but_SubmitPage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NAME_SCOUTER = String.valueOf(edit_Name.getText());
                Intent GoToFieldOfPlay = new Intent(StartPage.this, FieldOfPlay.class);
                startActivity(GoToFieldOfPlay);
            }
        });
    }
}