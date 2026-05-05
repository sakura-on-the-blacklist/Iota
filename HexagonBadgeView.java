package ph.edu.mobdevfinal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class HexagonBadgeView extends View {
    private Paint polyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path polyPath = new Path();
    private String number = "1";
    private boolean isLocked = true;

    public HexagonBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        polyPaint.setPathEffect(new CornerPathEffect(20));
    }

    public void setData(String number, boolean isLocked) {
        this.number = number;
        this.isLocked = isLocked;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float centerX = w / 2f;
        float centerY = h / 2f;
        float radius = (Math.min(w, h) / 2f) - 10;

        polyPath.reset();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 90);
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            if (i == 0) polyPath.moveTo(x, y);
            else polyPath.lineTo(x, y);
        }
        polyPath.close();

        Shader gradient;
        if (!isLocked) {
            gradient = new LinearGradient(0, 0, 0, h,
                    Color.parseColor("#FFE082"),
                    Color.parseColor("#FFB300"),
                    Shader.TileMode.CLAMP);
        } else {
            gradient = new LinearGradient(0, 0, 0, h,
                    Color.parseColor("#F5F7FA"),
                    Color.parseColor("#E4E7EB"),
                    Shader.TileMode.CLAMP);
        }
        polyPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(polyPath, polyPaint);

        if (number.equals("21")) {
            textPaint.setColor(Color.parseColor("#800000"));
        } else {
            textPaint.setColor(Color.parseColor("#8D6E63"));
        }

        textPaint.setTextSize(getHeight() * 0.40f);
        float textBaseLine = (getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(number, getWidth() / 2f, textBaseLine, textPaint);
    }
}