package com.mmscn.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class StatusEditText extends EditText {

	private static final int[] HAS_CONTENT_STATE_SET = { R.attr.state_has_content };

	public StatusEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		refreshDrawableState();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		try {
			if (length() > 0) {
				mergeDrawableStates(drawableState, HAS_CONTENT_STATE_SET);
			}
		} catch (NullPointerException e) {
			// we have to wait for mText
		}
		return drawableState;
	}

}
