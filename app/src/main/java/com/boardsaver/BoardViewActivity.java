package com.boardsaver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;

public class BoardViewActivity extends AppCompatActivity {

    private String[] boardData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);

        Intent prevIntent = getIntent();
        boardData = prevIntent.getStringArrayExtra("boardData");

        if(boardData == null || boardData.length != 6) {
            finish();
            return;
        }

        buildView();

    }


    /**
     * populates view with board data from previous activity for user to focus on board
     *
     */
    private void buildView() {
        TextView tvDate = findViewById(R.id.tv_board_date);
        EditText etDescription = findViewById(R.id.et_board_description);
        ImageView ivBoardImage = findViewById(R.id.iv_board_capture);

        tvDate.setText(boardData[4]);
        etDescription.setText(boardData[5]);

        @SuppressLint("DiscouragedApi")
        int imageResourceId = getResources().getIdentifier(
                boardData[3],
                "drawable",
                getPackageName()
        );
        ivBoardImage.setImageResource(imageResourceId);
        
        Button btnSaveDetails = findViewById(R.id.btn_save_details);
        btnSaveDetails.setOnClickListener(v -> {
            String newDescription = etDescription.getText().toString().trim();
            saveBoardDetails(new Board(
                    Integer.parseInt(boardData[0]),
                    boardData[1],
                    boardData[2],
                    boardData[3],
                    LocalDateTime.parse(boardData[4]),
                    newDescription
            ));
        });
        
    }


    /**
     * saves updated board details to db on button click. returns to previous activity.
     *
     */
    private void saveBoardDetails(Board tempBoard) {
        //System.out.println("Save Clicked");
        try {
            BoardRepository boardRepo = new BoardRepository(this);
            boolean updateResult = boardRepo.updateItem(tempBoard);
            if(updateResult) {
                System.out.println("Board Updated");
            }
        } catch (Exception e) {
            Log.d("BoardSavr", "failed to update board details");
        }

        finish();

    }

}

