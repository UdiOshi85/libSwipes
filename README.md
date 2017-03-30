# Swipe view!
## A simple android library providing an abstract view supports left & right gestures

![](https://github.com/UdiOshi85/GlobalFiles/blob/master/0_9-example.gif)

## Usage
#### First, Compile library into your code
```javascript
compile 'com.github.udioshi85:libSwipe:0.8.5'
```
#### Make A simple view and extend BaseSwipeActionView as following
```javascript
public class SimpleSwipeActionView extends BaseSwipeActionView {

    public SimpleSwipeActionView(@NonNull Context context) {
        super(context);
    }

    public SimpleSwipeActionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleSwipeActionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLeftIconResId() {
        return R.drawable.ic_call_black_24dp;
    }

    @Override
    public int getRightIconResId() {
        return R.drawable.ic_sms_black_24dp;
    }

    @Override
    public int getOverlayLayoutResId() {
        return R.layout.view_simple_swipe_action;
    }

    @Override
    public boolean isSwipeEnabled() {
        return true;
    }
}
```
#### Important notes
* Your xml (R.layout.view_simple_swipe_action in the example) must have Background color.
* Found a Bug? please report in our [Issues section](https://github.com/UdiOshi85/libSwipes/issues)
* License under [Apache 2.0](https://github.com/UdiOshi85/libSwipes/blob/master/LICENSE)

