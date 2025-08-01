package com.tapbi.spark.yokey.feature.gif;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tapbi.spark.yokey.databinding.ItemRcvGifBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.GifViewHolder> {
    private ArrayList<Media> medias;

    private int pos = 0;
    private boolean isReady;

    public GifAdapter(ArrayList<Media> medias) {
        this.medias = medias;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeGifList(ArrayList<Media> medias) {
        this.medias.clear();
        this.medias.addAll(medias);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GifViewHolder(ItemRcvGifBinding.inflate(LayoutInflater.from(viewGroup.getContext())));
    }

    @Override
    public void onBindViewHolder(@NonNull GifViewHolder gifViewHolder, final int position) {
         Timber.d("ducNQonBindViewHolder "+medias.size());
        gifViewHolder.bind(gifViewHolder.getAdapterPosition(), medias.get(gifViewHolder.getAdapterPosition()));

    }

    public void setPos(int pos) {
        notifyItemChanged(this.pos);
        this.pos = pos;
        notifyItemChanged(pos);
    }

    public void setOnItemGifClickListener(OnItemGifClickListener onItemGifClickListener) {
        this.onItemGifClickListener = onItemGifClickListener;
    }

    public OnItemGifClickListener onItemGifClickListener;

    public interface OnItemGifClickListener {
        void onItemGifClick(int position, Media media);
    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {
        private final File cachePath;
        ItemRcvGifBinding binding;

        public GifViewHolder(ItemRcvGifBinding binding) {
            super(binding.rlItemGif);
            this.binding = binding;
            cachePath = new File(binding.ivItemGif.getContext().getCacheDir(), "gifs");
            if (!cachePath.exists()) {
                if (!cachePath.mkdirs()) {
                }
            }
        }

        public void bind(int position, Media media) {
            final Image image = media.getImages().getFixedHeightSmall();
            if (image == null) {
                return;
            }
            final String url = image.getGifUrl();
            binding.rlItemGif.setOnClickListener(view -> {
                if (onItemGifClickListener != null) {
                    onItemGifClickListener.onItemGifClick(position, media);
                }

            });
            binding.rlItemGif.postDelayed(new Runnable() {
                @Override
                public void run() {
                    changeSizeItemView(image);
                }
            }, 200);
            isReady = false;
            final File file = new File(cachePath, "/" + media.getId() + ".gif");
            binding.ivItemGif.setImageResource(0);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.tvLoadError.setVisibility(View.GONE);
            if (file.exists()) {
                Glide.with(binding.ivItemGif.getContext()).asGif().load(file).override(800, 800).listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        if (file.delete()) {
                            // todo: try download again
                            downloadGif(url, file);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.progressBar.setVisibility(View.GONE);
                        isReady = true;
                        changeSizeItemView(image);
                        return false;
                    }
                }).into(binding.ivItemGif);
            } else {
                downloadGif(url, file);
            }

        }

        private void downloadGif(final String url, final File file) {
            Ion.with(binding.progressBar.getContext()).load(url).write(file).setCallback(new FutureCallback<File>() {
                @Override
                public void onCompleted(Exception e, File result) {
                    if (result != null) {
                        Glide.with(binding.ivItemGif.getContext()).asGif().load(file).override(800, 800).listener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.tvLoadError.setVisibility(View.VISIBLE);
                                 Timber.d("ducNQdownloadGif ");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                isReady = true;
                                binding.progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(binding.ivItemGif);
                    } else {
                        binding.tvLoadError.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }

        public boolean isReady() {
            return isReady;
        }

        private void changeSizeItemView(Image image) {
            if (itemView == null || image == null || binding.ivItemGif == null) {
                return;
            }
            if (image.getHeight() == 0) {
                return;
            }
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = binding.ivItemGif.getHeight() * image.getWidth() / image.getHeight();
                itemView.setLayoutParams(layoutParams);
            }

        }
    }


}