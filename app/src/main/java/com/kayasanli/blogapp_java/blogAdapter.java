package com.kayasanli.blogapp_java;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kayasanli.blogapp_java.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class blogAdapter extends RecyclerView.Adapter<blogAdapter.blogHolder> {

    ArrayList<blog> blogArrayList;
    public blogAdapter(ArrayList<blog> blogArrayList){
        this.blogArrayList = blogArrayList;
    }
    @NonNull
    @Override
    public blogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new blogHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull blogHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(blogArrayList.get(position).topic);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),MainActivity2.class);
                intent.putExtra("info","old");
                intent.putExtra("blogId",blogArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return blogArrayList.size();
    }

    public class blogHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public blogHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding =binding;
        }
    }
}
