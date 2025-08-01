package com.tapbi.spark.yokey.feature.gif;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapbi.spark.yokey.databinding.ItemRcvCategoryGifBinding;
//import com.giphy.sdk.core.models.Category;

import java.util.ArrayList;

public class GifCategoryAdapter extends RecyclerView.Adapter<GifCategoryAdapter.GifCategoryViewHolder> {
	private ArrayList<String> categories;

	private int pos;
	private int color = Color.BLACK;
	private int colorNotUse = Color.BLACK;

	public GifCategoryAdapter(ArrayList<String> categories) {
		this.categories = categories;
	}

	@NonNull
	@Override
	public GifCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		return new GifCategoryViewHolder(ItemRcvCategoryGifBinding.inflate(LayoutInflater.from(viewGroup.getContext())));
	}

	public void changeColor(int colorUse, int colorNotUse){
		this.color = color;
		this.colorNotUse  = colorNotUse;
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(@NonNull GifCategoryViewHolder gifViewHolder, final int position) {
		gifViewHolder.bind(position,categories.get(position), position == pos);

	}

	public void setPos(int pos) {
		notifyItemChanged(this.pos);
		this.pos = pos;
		notifyItemChanged(pos);
	}

	public void setOnItemGifCategoryClickListener(OnItemGifCategoryClickListener onItemGifCategoryClickListener) {
		this.onItemGifCategoryClickListener = onItemGifCategoryClickListener;
	}

	public OnItemGifCategoryClickListener onItemGifCategoryClickListener;

	public interface OnItemGifCategoryClickListener {
		void onItemGifCategoryClick(int position,String category);
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}
	public class GifCategoryViewHolder extends RecyclerView.ViewHolder {
           ItemRcvCategoryGifBinding binding;

		public GifCategoryViewHolder(ItemRcvCategoryGifBinding binding) {
			super(binding.tvCategoryName);
			this.binding = binding;
		}

		public void bind(int position,final String name, boolean isEnabled) {
			binding.tvCategoryName.setText(name);
			binding.tvCategoryName.setEnabled(isEnabled);
			if(isEnabled){
				binding.tvCategoryName.setTextColor(color);
			}
			else{
				binding.tvCategoryName.setTextColor(colorNotUse);
			}
			binding.tvCategoryName.setOnClickListener(v -> {
				if (onItemGifCategoryClickListener != null) {
					onItemGifCategoryClickListener.onItemGifCategoryClick(position,categories.get(position));
				}
			});
		}
	}
}