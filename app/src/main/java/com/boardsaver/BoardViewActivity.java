package com.boardsaver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BoardViewActivity extends AppCompatActivity {

    private String[] boardData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);

        Intent prevIntent = getIntent();
        boardData = prevIntent.getStringArrayExtra("boardData");

        if(boardData == null || boardData.length < 5) {
            finish();
            return;
        }

        buildView();

    }


    private void buildView() {
        TextView tvDate = findViewById(R.id.tv_board_date);
        TextView tvDescription = findViewById(R.id.tv_board_description);
        ImageView ivBoardImage = findViewById(R.id.iv_board_capture);

        tvDate.setText(boardData[3]);
        tvDescription.setText(boardData[4]);

        @SuppressLint("DiscouragedApi")
        int imageResourceId = getResources().getIdentifier(
                boardData[2],
                "drawable",
                getPackageName()
        );
        ivBoardImage.setImageResource(imageResourceId);


    }
}

