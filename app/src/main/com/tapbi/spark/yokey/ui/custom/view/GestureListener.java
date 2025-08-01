package com.tapbi.spark.yokey.ui.custom.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;

public  class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private final int Y_BUFFER = 10;
        ViewGroup recyclerView;
        private boolean isInverse = false;

        public GestureListener(ViewGroup recyclerView, boolean isInverse) {
            this.recyclerView = recyclerView;
            this.isInverse = isInverse;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // Prevent ViewPager from intercepting touch events as soon as a DOWN is detected.
            // If we don't do this the next MOVE event may trigger the ViewPager to switch
            // tabs before this view can intercept the event.
            recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(!isInverse) {
                condition(Math.abs(distanceX) > Math.abs(distanceY),Math.abs(distanceY) > Y_BUFFER);
            }else{
                condition(Math.abs(distanceY) > Math.abs(distanceX),Math.abs(distanceX) > Y_BUFFER);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        private void condition(boolean v1, boolean v2){
            if (v1) {
                // Detected a horizontal scroll, allow the viewpager from switching tabs
                recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
            } else if (v2) {
                // Detected a vertical scroll prevent the viewpager from switching tabs
                recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
            }
        }

    }