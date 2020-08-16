package com.xiangzi.video.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.xiangzi.video.R;

public class XzIconFontView extends AppCompatTextView {
    public XzIconFontView(@NonNull Context context) {
        super(context);
    }

    public XzIconFontView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public XzIconFontView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }


    private void initView(AttributeSet attrs){
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs,R.styleable.XzIconFontView);
        String path = typedArray.getString(R.styleable.XzIconFontView_xz_icon_font_file);
        typedArray.recycle();
        if (path == null){
            path = "fonts/ct_font_tour.ttf";
        }
        Typeface iconFont = Typeface.createFromAsset(getContext().getAssets(),path);
        setTypeface(iconFont);
    }


}
