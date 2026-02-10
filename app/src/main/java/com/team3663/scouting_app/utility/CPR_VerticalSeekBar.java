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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.team3663.scouting_app.R;

// =============================================================================================
// Class:       CPR_VerticalSeekBar
// Description: Creates a new WIDGET to be used when designing the layout of an activity.
//              Implements a VERTICAL seek bar with extra bells and whistles to allow the
//              customization of nearly all attributes.  See attrs.xml for a list of attributes
//              that can be set.
// =============================================================================================
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
    private String textSuffix = "";

    OnSeekBarChangeListener listener;

    // Constructor
    public CPR_VerticalSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    // Constructor
    public CPR_VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Constructor
    public CPR_VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // Member function: initialize the attributes of the vertical seek bar
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

        // If there's a set of attributes passed in, override the defaults to what the user wanted.
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CPR_VerticalSeekBar);

            barBackgroundColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_barBackgroundColor, barBackgroundColor);
            progressColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_progressColor, progressColor);
            barWidth = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_barWidth, barWidth);
            maxProgress = ta.getInt(R.styleable.CPR_VerticalSeekBar_vsb_max, 100);
            currentProgress = ta.getInt(R.styleable.CPR_VerticalSeekBar_vsb_progress, 0);
            thumbWidth = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_thumbWidth, thumbWidth);
            thumbHeight = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_thumbHeight, thumbHeight);
            progressFromBottom = ta.getBoolean(R.styleable.CPR_VerticalSeekBar_vsb_progressFromBottom, progressFromBottom);
            textEnabled = ta.getBoolean(R.styleable.CPR_VerticalSeekBar_vsb_textEnabled, textEnabled);
            textColor = ta.getColor(R.styleable.CPR_VerticalSeekBar_vsb_textColor, textColor);
            textSize = ta.getDimension(R.styleable.CPR_VerticalSeekBar_vsb_textSize, textSize);
            textSuffix = ta.getString(R.styleable.CPR_VerticalSeekBar_vsb_textSuffix);

            if (textSuffix == null) textSuffix = "";

            if (ta.hasValue(R.styleable.CPR_VerticalSeekBar_vsb_thumb))
                thumbDrawable = ta.getDrawable(R.styleable.CPR_VerticalSeekBar_vsb_thumb);

            ta.recycle();
        }

        barBackgroundPaint.setColor(barBackgroundColor);
        progressPaint.setColor(progressColor);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        barCornerRadius = barWidth / 2;

        if (thumbDrawable == null)
            thumbDrawable = ContextCompat.getDrawable(context, R.drawable.scrollbar_style);
    }

    // Member function: Convert dp to px
    private float dpToPx(float in_dp) {
        return in_dp * getResources().getDisplayMetrics().density;
    }

    // Member function: Required override function to set the size of the view
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int)Math.max(getPaddingLeft() + getPaddingRight() + thumbWidth, barWidth);
        int desiredHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    // Member function: Required override function to set the size of the view if changed
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

    // Member function: Update the position of the thumb
    private void updateThumbPosition() {
        thumbY = getProgressY();
        invalidate();
    }

    // Member function: Required override function to draw the view
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawBarBackground(canvas);
        drawProgressBar(canvas);
        drawThumb(canvas);

        if (textEnabled)
            drawProgressText(canvas);
    }

    // Member function: Draw the text on the thumb
    private void drawProgressText(Canvas canvas) {
        String textToDraw = currentProgress + textSuffix;
        Rect textBounds = new Rect();

        textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), textBounds);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float x = barRect.centerX();
        float y = thumbY - ((textPaint.descent() + textPaint.ascent()) / 2f);
        
        canvas.drawText(textToDraw, x, y, textPaint);
    }

    // Member function: Draw the thumb on the seekbar
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

    // Member function: Draw the background bar of the seekbar
    private void drawBarBackground(Canvas canvas) {
        canvas.drawRoundRect(barRect, barCornerRadius, barCornerRadius, barBackgroundPaint);
    }

    // Member function: Draw the progress bar of the seekbar
    private void drawProgressBar(Canvas canvas) {
        float progressY = getProgressY();

        // Draw the progress bar based on the direction we need to report progress from
        if (progressFromBottom)
            canvas.drawRoundRect(barRect.left, progressY, barRect.right, barRect.bottom, barCornerRadius, barCornerRadius, progressPaint);
        else
            canvas.drawRoundRect(barRect.left, barRect.top, barRect.right, progressY, barCornerRadius, barCornerRadius, progressPaint);

    }

    // Member function: Return the Y coordinate of the seekbar based on the current progress
    private float getProgressY() {
        float barDrawableHeight = barRect.height();
        float progressRatio = (float)currentProgress / maxProgress;

        // Return the progress based on the direction we need to report progress from
        if (progressFromBottom)
            return barRect.top + barDrawableHeight * (1 - progressRatio);
        else
            return barRect.top + barDrawableHeight * progressRatio;
    }

    // Member function: Required override function to handle touch events
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

    // Member function: Update the progress based on the touch position
    private void updateProgressFromTouch(float y) {
        // Restrict the Y value to be within the bar
        float clampedY = Math.max(barRect.top, Math.min(y, barRect.bottom));
        float progressRatio;

        // Calculate the progress based on the touch position and the direction we need to report progress from
        if (progressFromBottom)
            progressRatio = 1 - ((clampedY - barRect.top) / barRect.height());
        else
            progressRatio = (clampedY - barRect.top) / barRect.height();

        setProgress((int)(progressRatio * maxProgress));
    }

    // Member function: Set the progress of the seekbar
    public void setProgress(int progress) {
        progress = Math.max(0, Math.min(progress, maxProgress));

        if (this.currentProgress != progress) {
            this.currentProgress = progress;
            updateThumbPosition();

            if (listener != null)
                listener.onProgressChanged(this, currentProgress, isDragging);
        }
    }

    // Member function: Return the current progress of the seekbar
    public int getProgress() {
        return currentProgress;
    }

    // Member function: Return the max progress value of the seekbar
    public int getMax() {
        return maxProgress;
    }

    // Member function: Set the max progress value of the seekbar
    public void setMax(int max) {
        if (max > 0) {
            maxProgress = max;
            setProgress(Math.min(currentProgress, maxProgress));
        }
    }

    // Member function: Set whether the text is enabled or not
    public void setTextEnable(boolean textEnabled){
        this.textEnabled = textEnabled;
        invalidate();
    }

    // Member function: Set the text color
    public void setTextColor(int color){
        textPaint.setColor(color);
        invalidate();
    }

    // Member function: Set the text size
    public void setTextSize(float size){
        textPaint.setTextSize(size);
        invalidate();
    }

    // Member function: Set the color of the progress bar
    public void setProgressColor(int color){
        progressPaint.setColor(color);
        invalidate();
    }

    // Member function: Set the color of the seekbar background
    public void setBarBackgroundColor(int color){
        barBackgroundPaint.setColor(color);
        invalidate();
    }

    // Member function: Set the width of the seekbar
    public void setBarWidth(float size){
        this.barWidth = size;
        this.barCornerRadius = size / 2;
        requestLayout();
        invalidate();
    }

    // Member function: Set the drawable to use for the thumb
    public void setThumb(Drawable drawable){
        this.thumbDrawable = drawable;
        invalidate();
    }

    // Member function: Set the drawable to use for the thumb
    public void setThumb(int drawableResId){
        this.thumbDrawable = ContextCompat.getDrawable(getContext(), drawableResId);
        invalidate();
    }

    // Member function: Set the size to use for the thumb
    public void setThumbSize(float width, float height){
        this.thumbWidth = width;
        this.thumbHeight = height;
        requestLayout();
        invalidate();
    }

    // Member function: Return whether the touch is on the thumb
    private boolean isTouchOnThumb(float y) {
        float thumbTouchRadius = thumbHeight;
        return Math.abs(y - thumbY) <= thumbTouchRadius;
    }

    // Member function: Set the listener for when the progress changes
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    // Member function: Interface for the listener
    public interface OnSeekBarChangeListener {
        void onProgressChanged(CPR_VerticalSeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(CPR_VerticalSeekBar seekBar);
        void onStopTrackingTouch(CPR_VerticalSeekBar seekBar);
    }
}
