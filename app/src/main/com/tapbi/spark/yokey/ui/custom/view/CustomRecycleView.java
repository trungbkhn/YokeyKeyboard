package com.tapbi.spark.yokey.ui.custom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.tapbi.spark.yokey.App;

import org.jetbrains.annotations.NotNull;

public class CustomRecycleView extends RecyclerView  implements RecyclerView.OnItemTouchListener{
    GestureDetector gestureDetector;
    public CustomRecycleView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context){
        gestureDetector = new GestureDetector(context, new GestureListener(this, false));
        addOnItemTouchListener(this);
    }

    public void setInverse(boolean inverse){
        gestureDetector = new GestureDetector(App.getInstance(), new GestureListener(this, inverse));
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityY *= 0.7;
        return super.fling(velocityX, velocityY);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(e);
        }
        return false;
    }
    @Override
    public void onTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}
