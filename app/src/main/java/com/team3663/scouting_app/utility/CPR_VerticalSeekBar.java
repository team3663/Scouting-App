package com.team3663.scouting_app.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.team3663.scouting_app.R;

public class CPR_VerticalSeekBar extends View {
    private Paint barBackgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF barRect;
    private float barWidth;
    private float barCornerRadius;

    private Drawable thumbDrawable;
    private float thumbWidth;
    private float thumbHeight;
    private float thumbX;
    private float thumbY;

    private int maxProgress = 100;
    private int currentProgress = 0;
    private boolean isDragging = false;
    private boolean progressFromBottom = false;
    private boolean textEnabled = false;
    private float textMargin;
    private String textSuffix = "";

    OnSeekBarChangeListener listener;

    public CPR_VerticalSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public CPR_VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CPR_VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CPR_VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        barBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barRect = new RectF();

        int barBackgroundColor = Color.GRAY;
        int progressColor = Color.RED;
        barWidth = dpToPx(20);
        thumbWidth = dpToPx(40);
        thumbHeight = dpToPx(40);

        int textColor = Color.BLACK;
        float textSize = dpToPx(14);
        textMargin = dpToPx(12);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CPR_VerticalSeekBar);

            barBackgroundColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_barBackgroundColor, barBackgroundColor);
            progressColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_progressColor, progressColor);
            barWidth = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_barWidth, barWidth);
            maxProgress = ta.getInt(R.styleable.CPR_VerticalSeekBar_vsb_max, 100);
            currentProgress = ta.getInt(R.styleable.CPR_VerticalSeekBar_vsb_progress, 0);
            thumbWidth = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_thumbWidth, thumbWidth);
            thumbHeight = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_thumbHeight, thumbHeight);
            progressFromBottom = ta.getBoolean(R.styleable.CPR_VerticalSeekBar_vsb_progressFromBottom, true);
            textEnabled = ta.getBoolean(R.styleable.CPR_VerticalSeekBar_vsb_textEnabled, false);
            textColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_textColor, textColor);
            textSize = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_textSize, textSize);
            textMargin = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_textMargin, textMargin);
            textSuffix = ta.getString(R.styleable.CPR_VerticalSeekBar_vsb_textSuffix);

            if (textSuffix == null) textSuffix = "";

            if (ta.hasValue(R.styleable.CPR_VerticalSeekBar_vsb_thumb))
                thumbDrawable = ta.getDrawable(R.styleable.CPR_VerticalSeekBar_vsb_thumb);
        }

        barBackgroundPaint.setColor(barBackgroundColor);
        progressPaint.setColor(progressColor);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        barCornerRadius = barWidth / 2;

        if (thumbDrawable != null)
            thumbDrawable = ContextCompat.getDrawable(context, R.drawable.scrollbar_style);
    }

    private float dpToPx(float in_dp) {
        return in_dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int)Math.max(getPaddingLeft() + getPaddingRight() + thumbWidth, barWidth);
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float thumbHalfHeight = thumbHeight / 2;
        float barLeft = (w - barWidth) / 2;
        float barRight = barLeft + barWidth;
        float barTop = getPaddingTop() + thumbHalfHeight;
        float barBottom = h - getPaddingBottom() - thumbHalfHeight;

        barRect.set(barLeft, barTop, barRight, barBottom);
        thumbX = w / 2f;
        updateThumbPosition();
    }

    private void updateThumbPosition() {
        thumbY = getProgressY();
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBarBackground(canvas);
        drawProgressBar(canvas);
        drawThumb(canvas);

        if (textEnabled)
            drawProgressText(canvas);
    }

    private void drawProgressText(Canvas canvas) {
        String textToDraw = currentProgress + textSuffix;
        Rect textBounds = new Rect();

        textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), textBounds);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float x = barRect.centerX();
        float y = thumbY - ((textPaint.descent() + textPaint.ascent()) / 2f);
        
        canvas.drawText(textToDraw, x, y, textPaint);
    }

    private void drawThumb(Canvas canvas) {
        if (thumbDrawable != null) {
            int thumbHalfWidth = (int)(thumbWidth / 2);
            int thumbHalfHeight = (int)(thumbHeight / 2);

            thumbDrawable.setBounds(
                    (int)(thumbX - thumbHalfWidth),
                    (int)(thumbY - thumbHalfHeight),
                    (int)(thumbX + thumbHalfWidth),
                    (int)(thumbY + thumbHalfHeight)
            );

            thumbDrawable.draw(canvas);
        }
    }

    private void drawBarBackground(Canvas canvas) {
        canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barBackgroundPaint);
    }

    private void drawProgressBar(Canvas canvas) {
        float progressY = getProgressY();

        if (progressFromBottom)
            canvas.drawRoundRect(barRect.left, progressY, barRect.right, barRect.bottom, barCornerRadius, barCornerRadius, progressPaint);
        else
            canvas.drawRoundRect(barRect.left, barRect.top, barRect.right, barRect.bottom, barCornerRadius, barCornerRadius, progressPaint);

    }

    private float getProgressY() {
        float barDrawableHeight = barRect.height();
        float progressRatio = (float)currentProgress / maxProgress;

        if (progressFromBottom)
            return barRect.top + barDrawableHeight * (1 - progressRatio);
        else
            return barRect.bottom - barDrawableHeight * progressRatio;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchOnThumb(y)) {
                    isDragging = true;

                    if (listener != null) listener.onStartTrackingTouch(this);

                    updateProgressFromTouch(y);
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    updateProgressFromTouch(y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    isDragging = false;

                    if (listener != null) listener.onStopTrackingTouch(this);

                    return true;
                }
                break;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isDragging = true;
            if (listener != null) listener.onStartTrackingTouch(this);
            updateProgressFromTouch(y);
            if (listener != null) listener.onStopTrackingTouch(this);
            isDragging = false;
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void updateProgressFromTouch(float y) {
        float clampedY = Math.max(barRect.top, Math.min(y, barRect.bottom));
        float progressRatio;

        if (progressFromBottom)
            progressRatio = 1 - ((clampedY - barRect.bottom) / barRect.height());
        else
            progressRatio = (clampedY - barRect.bottom) / barRect.height();

        setProgress((int)(progressRatio * maxProgress));
    }

    public void setProgress(int progress) {
        progress = Math.max(0, Math.min(progress, maxProgress));

        if (this.currentProgress != progress) {
            this.currentProgress = progress;
            updateThumbPosition();

            if (listener != null)
                listener.onProgressChanged(this, currentProgress, isDragging);
        }
    }

    public int getProgress() {
        return currentProgress;
    }

    public int getMax() {
        return maxProgress;
    }

    public void setMax(int max) {
        if (max > 0) {
            maxProgress = max;
            setProgress(Math.min(currentProgress, maxProgress));
        }
    }

    public void setTextEnable(boolean textEnabled){
        this.textEnabled = textEnabled;
        invalidate();
        requestLayout();
    }

    public void setTextColor(int color){
        textPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public void setTextSize(float size){
        textPaint.setTextSize(size);
        invalidate();
        requestLayout();
    }

    public void setProgressColor(int color){
        progressPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public void setBarBackgroundColor(int color){
        barBackgroundPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public void setBarWidth(float size){
        this.barWidth = size;
        this.barCornerRadius = size / 2;
        requestLayout();
        invalidate();
    }

    public void setThumb(Drawable drawable){
        this.thumbDrawable = drawable;
        invalidate();
        requestLayout();
    }

    public void setThumb(int drawableResId){
        this.thumbDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
        requestLayout();
    }

    public void setThumbSize(float width, float height){
        this.thumbWidth = width;
        this.thumbHeight = height;
        requestLayout();
        invalidate();
    }

    private boolean isTouchOnThumb(float y) {
        float thumbTouchRadius = thumbHeight;
        return Math.abs(y - thumbY) <= thumbTouchRadius;
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(CPR_VerticalSeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(CPR_VerticalSeekBar seekBar);
        void onStopTrackingTouch(CPR_VerticalSeekBar seekBar);
    }
}
