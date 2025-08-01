package com.android.inputmethod.keyboard.translate;

import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_IN_PUT;
import static com.tapbi.spark.yokey.util.Constant.CODE_LANGUAGE_OUT_PUT;
import static com.tapbi.spark.yokey.util.Constant.TYPE_LANGUAGE_IN_PUT;
import static com.tapbi.spark.yokey.util.Constant.TYPE_LANGUAGE_OUT_PUT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.databinding.ItemChooseLanguageTranslateBinding;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.model.LanguageTranslate;
import com.tapbi.spark.yokey.util.DisplayUtils;

import java.util.ArrayList;

public class LanguageTranslateAdapter extends RecyclerView.Adapter<LanguageTranslateAdapter.Holder> {

    private Context context;
    private ArrayList<LanguageTranslate> list = new ArrayList<>();
    private ArrayList<LanguageTranslate> listCurrent = new ArrayList<>();
    private int countItemUsedToChoose = 0;
    private int typeAdapter = TYPE_LANGUAGE_IN_PUT;
    private ILanguageChooseListener mListener;

    public LanguageTranslateAdapter(Context context, ArrayList<LanguageTranslate> list, int countItemUsedToChoose,
                                    ILanguageChooseListener listener, int typeAdapter) {
        this.context = context;
        this.list = list;
        this.countItemUsedToChoose = countItemUsedToChoose;
        mListener = listener;
        this.typeAdapter = typeAdapter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeList(ArrayList<LanguageTranslate> list, int countItemUsedToChoose) {
        this.list = list;
        this.countItemUsedToChoose = countItemUsedToChoose;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemChooseLanguageTranslateBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        int pos = holder.getAdapterPosition();
        holder.bind(pos);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ItemChooseLanguageTranslateBinding binding;

        public Holder(@NonNull ItemChooseLanguageTranslateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int pos) {
            LanguageTranslate item = list.get(pos);
            binding.vLine.setVisibility(countItemUsedToChoose - 1 == pos ? View.VISIBLE : View.GONE);
            if (typeAdapter == TYPE_LANGUAGE_IN_PUT) {
                if (App.getInstance().mPrefs.getString(CODE_LANGUAGE_IN_PUT, list.get(0).getCodeLanguage()).equals(list.get(pos).getCodeLanguage())) {
                    binding.rBtnLanguage.setButtonTintList(ContextCompat.getColorStateList(context, R.color.color_tint_radio));
                    binding.rBtnLanguage.setChecked(true);
                } else {
                    binding.rBtnLanguage.setButtonTintList(ContextCompat.getColorStateList(context, R.color.color_8F90A6));
                    binding.rBtnLanguage.setChecked(false);
                }
            } else if (typeAdapter == TYPE_LANGUAGE_OUT_PUT) {
                if (App.getInstance().mPrefs.getString(CODE_LANGUAGE_OUT_PUT, list.get(0).getCodeLanguage()).equals(list.get(pos).getCodeLanguage())) {
                    binding.rBtnLanguage.setButtonTintList(ContextCompat.getColorStateList(context, R.color.color_tint_radio));
                    binding.rBtnLanguage.setChecked(true);
                } else {
                    binding.rBtnLanguage.setButtonTintList(ContextCompat.getColorStateList(context, R.color.color_8F90A6));
                    binding.rBtnLanguage.setChecked(false);
                }
            }
            //binding.rBtnLanguage.setButtonTintList(ContextCompat.getColorStateList(context, pos == 0 ? R.color.color_tint_radio : R.color.color_8F90A6));
            // binding.rBtnLanguage.setChecked(pos == 0);
            binding.rBtnLanguage.setText(item.getNameLanguage());

            binding.vClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onChooseLanguage(pos, item);
                }
            });

            if (pos == 0) {
                binding.container.setPadding(0, DisplayUtils.dp2px(10), 0, 0);
            } else if (pos == list.size() - 1) {
                binding.container.setPadding(0, 0, 0, DisplayUtils.dp2px(10));
            } else {
                binding.container.setPadding(0, 0, 0, 0);
            }
        }
    }

    public interface ILanguageChooseListener {
        void onChooseLanguage(int pos, LanguageTranslate languageTranslate);
    }

}
