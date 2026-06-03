package com.boardsaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


/*
 * Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
 */
public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.BoardListHolder> {

    private final Context context;
    private ArrayList<Board> boardList;
    private final OnItemClickListener listener;

    // Click interface so MainActivity can react to taps on item
    public interface OnItemClickListener {
        void onItemClick(Board item);
        void onItemLongClick(Board item);
    }

    public BoardListAdapter(Context context, ArrayList<Board> boardList, OnItemClickListener listener) {
        this.context = context;
        this.boardList = boardList;
        this.listener = listener;
    }


    // ViewHolder: holds row views
    public static class BoardListHolder extends RecyclerView.ViewHolder {
        TextView tvBoardDate, tvUserId;
        ImageView ivBoardImage;

        public BoardListHolder(View itemView) {
            super(itemView);
            tvBoardDate = itemView.findViewById(R.id.tv_board_date);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            ivBoardImage = itemView.findViewById(R.id.iv_board_capture);
        }
    }

    @NonNull
    @Override
    public BoardListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.front_board_card, parent, false);
        return new BoardListHolder(view);
    }

    @Override
    public void onBindViewHolder(BoardListHolder holder, int position) {
        Board board = boardList.get(position);

        holder.tvBoardDate.setText(board.getDate().toString());
        holder.tvUserId.setText(board.getUserId());

        @SuppressLint("DiscouragedApi")
        int imageResourceId = context.getResources().getIdentifier(
                board.getImagePath(),
                "drawable",
                context.getPackageName()
        );
        holder.ivBoardImage.setImageResource(imageResourceId);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(board);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(board);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return boardList.size();
    }

    // Update list after DB changes
    public void updateItems(ArrayList<Board> newBoards) {
        this.boardList = newBoards;
        notifyDataSetChanged(); //FIXME:: add more specific notifyDataSet for each CRUD
    }
}
