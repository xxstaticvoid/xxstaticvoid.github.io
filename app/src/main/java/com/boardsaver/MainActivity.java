package com.boardsaver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    private BoardListAdapter adapter;
    private BoardRepository boardRepo;
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get user privileges
        Intent prevIntent = getIntent();
        isLoggedIn = prevIntent.getBooleanExtra("isLoggedIn", false);


        // set up spare parts inventory repository
        boardRepo = new BoardRepository(this);

        RecyclerView rvInventory = findViewById(R.id.rv_boards_list);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BoardListAdapter(this, boardRepo.getAllItems(), new BoardListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Board board) {
                switchToBoardView(board);
            }

            @Override
            public void onItemLongClick(Board board) {
                deleteBoardFromBoardList(board);
            }
        });


        rvInventory.setAdapter(adapter);


        //set clicking action of FAB
        FloatingActionButton fab = findViewById(R.id.button_add_entry);
        fab.setOnClickListener(v -> {
            openCameraActivity();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null && boardRepo != null) {
            adapter.updateItems(boardRepo.getAllItems());
        }
    }


    /**
     * starts new activity with selected board data; allowing the user to focus on board
     *
     * @param board board data type from BoardListAdapter.boardList
     *
     */
    private void switchToBoardView(Board board) {
        if(!isLoggedIn) {
            return;
        }

        String[] boardData = {
                String.valueOf(board.getId()),
                board.getUserId(),
                board.getState(),
                board.getImagePath(),
                board.getDate().toString(),
                board.getDescription()
        };
        Intent intent = new Intent(this, BoardViewActivity.class);
        intent.putExtra("boardData", boardData);
        startActivity(intent);

    }


    /**
     * opens camera activity if user is logged in
     *
     */
    private void openCameraActivity() {
        //only allow parts to be added if user logged in
        if(!isLoggedIn) {
            return;
        }
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }


    /**
     * deletes selected board from board list
     *
     * @param board board data type from BoardListAdapter.boardList
     *
     */
    private void deleteBoardFromBoardList(Board board) {
        //only allow delete if logged in
        if(!isLoggedIn) {
            return;
        }
        try {
            boolean deleteResult = boardRepo.deleteBoard(board.getId());
            if(!deleteResult) {
                throw new RuntimeException();
            }
            adapter.updateItems(boardRepo.getAllItems());
        } catch(Exception e) {
            Log.d("MainActivity","failed to delete entry on " + board.getId());
        }
    }
}