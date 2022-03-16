package com.example.recycletree;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

interface ClickListener<T> {

    void onItemCLick(T task);
}

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<Task> taskList;
    private ClickListener<Task> clickListener;

    RecyclerAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }


    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        final Task task = taskList.get(position);
        viewHolder.card_name.setText(task.getTask_name());
        viewHolder.reward.setText(task.getReward_pts() + " pts");
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemCLick(task);
            }
        });
    }

    public void setOnItemClickListener(ClickListener<Task> taskClickListener) {
        this.clickListener = taskClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView card_name;
        private TextView reward;
        private CardView cardView;

        public ViewHolder(View view) {
            super(view);
            card_name = view.findViewById(R.id.card_name);
            reward = view.findViewById(R.id.reward);
            cardView = view.findViewById(R.id.card_view);


        }
    }


}
