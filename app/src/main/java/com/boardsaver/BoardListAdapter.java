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


/**
 * Adapter class for managing the display of {@link Board} items in a {@link RecyclerView}.
 * Provides binding from the board data set to the card views displayed in the main list.
 */
public class BoardListAdapter extends RecyclerView.Adapter<BoardListAdapter.BoardListHolder> {

    private final Context context;
    private ArrayList<Board> boardList;
    private final OnItemClickListener listener;

    /**
     * Interface definition for a callback to be invoked when a board item is clicked or long-clicked.
     */
    public interface OnItemClickListener {
        /**
         * Called when a board item has been clicked.
         * @param item The {@link Board} object associated with the clicked item.
         */
        void onItemClick(Board item);

        /**
         * Called when a board item has been long-clicked.
         * @param item The {@link Board} object associated with the long-clicked item.
         */
        void onItemLongClick(Board item);
    }

    public BoardListAdapter(Context context, ArrayList<Board> boardList, OnItemClickListener listener) {
        this.context = context;
        this.boardList = boardList;
        this.listener = listener;
    }


    /**
     * ViewHolder class that describes an item view and metadata about its place within the RecyclerView.
     */
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

    /**
     * Updates the data set and refreshes the UI.
     * @param newBoards The new list of {@link Board} items.
     */
    public void updateItems(ArrayList<Board> newBoards) {
        this.boardList = newBoards;
        notifyDataSetChanged(); //FIXME:: add more specific notifyDataSet for each CRUD
    }
}
