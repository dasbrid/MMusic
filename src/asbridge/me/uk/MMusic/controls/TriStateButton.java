package asbridge.me.uk.MMusic.controls;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import asbridge.me.uk.MMusic.R;

/**
 * Created by AsbridgeD on 21/01/2016.
 */
public class TriStateButton extends Button
{
    private String DEBUGTAG = "TriStateButton";

    // Keeps track of the current state, 0, 1, or 2
    private int _state;

    // Get the attributes created in attrs.xml
    private static final int[] STATE_NONE_SET =
            {
                    R.attr.none
            };

    private static final int[] STATE_SOME_SET =
            {
                    R.attr.some
            };

    private static final int[] STATE_All_SET =
            {
                    R.attr.all
            };

    // Constructors
    public TriStateButton(Context context)
    {
        super(context);

        // Set the default state and text
        _state = 0;
        setButtonText();
    }

    public TriStateButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Set the default state and text
        _state = 0;
        setButtonText();
    }

    public TriStateButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        // Set the default state and text
        _state = 0;
        setButtonText();
    }

    @Override
    public boolean performClick()
    {
        // Move to the next state
        nextState();

        return super.performClick();
    }

    // Generate the drawable needed for the current state
    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        // Add the number of states you have
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);

        if(_state == 0)
        {
            mergeDrawableStates(drawableState, STATE_NONE_SET);
        }
        else if(_state == 1)
        {
            mergeDrawableStates(drawableState, STATE_SOME_SET);
        }
        else if(_state == 2)
        {
            mergeDrawableStates(drawableState, STATE_All_SET);
        }

        return drawableState;
    }

    // Set current state, 0-2
    public void setState(int state)
    {
        if((state > -1) && (state < 3))
        {
            _state = state;
            setButtonText();
        }
    }

    // Returns current state
    public int getState()
    {
        return _state;
    }

    // Increases state, or loops to 0
    public void nextState()
    {
        _state++;

        // Loop around if at the last state
        if(_state > 2)
        {
            _state = 0;
        }

        setButtonText();
    }

    // Decreases state, or loops to 2
    public void previousState()
    {
        _state--;

        // Loop around if at the first state
        if(_state < 0)
        {
            _state = 2;
        }

        setButtonText();
    }

    // Set the text displayed on the button
    private void setButtonText()
    {
        switch(_state)
        {
            case 0:
                this.setBackgroundResource(R.drawable.selected_none);
                break;
            case 1:
                this.setBackgroundResource(R.drawable.selected_some);
                break;
            case 2:
                this.setBackgroundResource(R.drawable.selected_all);
                break;
            default: this.setText("N/A"); // Should never happen, but just in case
                break;
        }
    }
}