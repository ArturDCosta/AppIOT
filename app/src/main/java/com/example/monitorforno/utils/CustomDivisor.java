package com.example.monitorforno.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitorforno.R;

public class CustomDivisor extends RecyclerView.ItemDecoration {

    private final Drawable divider;

    public CustomDivisor(Context context) {
        divider = ContextCompat.getDrawable(
                context,
                R.drawable.divisor_alerta
        );
    }

    @Override
    public void onDrawOver(
            @NonNull Canvas canvas,
            @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {

        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {

            android.view.View child =
                    parent.getChildAt(i);

            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams)
                            child.getLayoutParams();

            int top =
                    child.getBottom() +
                            params.bottomMargin;

            int bottom =
                    top +
                            divider.getIntrinsicHeight();

            divider.setBounds(
                    left,
                    top,
                    right,
                    bottom
            );

            divider.draw(canvas);
        }
    }
}