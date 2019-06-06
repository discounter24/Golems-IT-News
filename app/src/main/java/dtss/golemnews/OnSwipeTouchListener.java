package dtss.golemnews;
import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;
    private final GestureListener gestureListener;


    public OnSwipeTouchListener(Context ctx){

        gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(ctx, gestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }


    private final class GestureListener extends SimpleOnGestureListener {



        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return super.onContextClick(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            ListView v;

                            onSwipeRight(e1,e2);
                        } else {
                            onSwipeLeft(e1,e2);
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom(e1, e2);
                    } else {
                        onSwipeTop(e1,e2);
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return result;
        }



    }




    public void onSwipeRight(MotionEvent e1, MotionEvent e2) {

    }

    public void onSwipeLeft(MotionEvent e1, MotionEvent e2) {
    }

    public void onSwipeTop(MotionEvent e1, MotionEvent e2) {
    }

    public void onSwipeBottom(MotionEvent e1, MotionEvent e2) {
    }
}
