package com.mmscn.mmsdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mmscn.widgets.R;

public class MMSDialog implements DialogInterface {
	public final static int ITEM_HEIGHT_DEFAULT = 0;
	public final static int ITEM_HEIGHT_MATCH_PARENT = -1;
	public final static int ITEM_HEIGHT_WRAP_CONTENT = -2;

	private Context mContext;
	private Dialog mDialog;

	private View mTitleBar;
	private ImageView mIcon;
	private TextView mTitleTv;

	private ViewGroup mExtraBtPanel;

	private ListView mItemLv;

	private View mView;

	private ViewGroup mContentContainer;

	private View mButtonBar;
	private Button mBtNegative;
	private Button mBtNeutral;
	private Button mBtPositive;

	private boolean mForceWidthMatchParent = false;

	private OnShowListener mOnShowListener;

	public MMSDialog(Context context) {
		this(context, 0);
	}

	public MMSDialog(Context context, int theme) {
		this.mContext = context;
		this.mDialog = new Dialog(context, theme);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mView = View.inflate(context, R.layout.dialog, null);

		mDialog.setContentView(mView);

		this.mTitleBar = mView.findViewById(R.id.title_bar);
		this.mIcon = (ImageView) mView.findViewById(R.id.dialog_icon);
		this.mTitleTv = (TextView) mView.findViewById(android.R.id.title);

		this.mExtraBtPanel = (ViewGroup) mView.findViewById(R.id.dialog_extra_button_container);

		this.mItemLv = (ListView) mView.findViewById(android.R.id.list);
		this.mContentContainer = (ViewGroup) mView.findViewById(R.id.content_container);

		this.mButtonBar = mView.findViewById(R.id.dialog_button_bar);
		this.mBtNegative = (Button) mView.findViewById(R.id.dialog_button_negative);
		this.mBtNeutral = (Button) mView.findViewById(R.id.dialog_button_neutral);
		this.mBtPositive = (Button) mView.findViewById(R.id.dialog_button_positive);

		mDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				View extraContainer = mView.findViewById(R.id.dialog_extra_button_scroll_view);
				int maxHeight = mContext.getResources().getDimensionPixelSize(R.dimen.dialog_extra_item_max_height);
				if (extraContainer.getHeight() > maxHeight) {
					((RelativeLayout.LayoutParams) extraContainer.getLayoutParams()).height = maxHeight;
				}
				if (mForceWidthMatchParent) {
					LayoutParams params = mDialog.getWindow().getAttributes();

					params.width = LayoutParams.MATCH_PARENT;
					mDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
				}

				// extraContainer.measure(-1,
				// MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
				if (null != mOnShowListener) {
					mOnShowListener.onShow(mDialog);
				}
			}
		});
	}

	public MMSDialog setForceWidthMatchParent(boolean forceWidthMatchParent) {
		this.mForceWidthMatchParent = forceWidthMatchParent;
		return this;
	}

	public View getView() {
		return mView;
	}

	public MMSDialog setCancelable(boolean flag) {
		mDialog.setCancelable(flag);
		return this;
	}

	public MMSDialog setCanceledOnTouchOutside(boolean cancel) {
		mDialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	public MMSDialog setContentView(View v, LayoutParams layoutParams) {
		mContentContainer.setVisibility(View.VISIBLE);
		mContentContainer.removeAllViews();
		mContentContainer.addView(v, layoutParams);
		return this;
	}

	public MMSDialog setContentBackgroundResource(int resId) {
		mContentContainer.setBackgroundResource(resId);
		return this;
	}

	public MMSDialog setContentBackgroundColor(int color) {
		mContentContainer.setBackgroundColor(color);
		return this;
	}

	public ViewGroup getContentContainer() {
		return mContentContainer;
	}

	/**
	 * same as setItems(items, null, l, ITEM_HEIGHT_DEFAULT)
	 * 
	 * @see MMSDialog#setItems(CharSequence[], int[],
	 *      DialogInterface.OnClickListener, int)
	 */
	public MMSDialog setItems(CharSequence[] items, DialogInterface.OnClickListener l) {
		return setItems(items, null, l, ITEM_HEIGHT_DEFAULT);
	}

	/**
	 * @param items
	 *            可选项目
	 * @param highlightedId
	 *            高亮项目位置，可为null
	 * @param l
	 *            选择项目后回调接口
	 * @param listViewHeight
	 *            listView高度
	 * @return 返回MMSDialog实例以便串联调用
	 */
	public MMSDialog setItems(final CharSequence[] items, final int[] highlightedId,
			final DialogInterface.OnClickListener l, int listViewHeight) {

		final int count = null == items ? 0 : items.length;
		if (count == 0)
			return this;
		Resources res = mContext.getResources();
		final float textSize = res.getDimension(R.dimen.dialog_item_list_text);
		final int paddingLeft = res.getDimensionPixelSize(R.dimen.dialog_item_list_padding_left);
		final int paddingTop = res.getDimensionPixelSize(R.dimen.dialog_item_list_padding_top);
		final int paddingRight = res.getDimensionPixelSize(R.dimen.dialog_item_list_padding_right);
		final int paddingBottom = res.getDimensionPixelSize(R.dimen.dialog_item_list_padding_bottom);

		final int highlightTokenWidth = res.getDimensionPixelSize(R.dimen.dialog_item_list_highlight_token_width);
		final ColorStateList textColor = res.getColorStateList(R.color.dialog_clickable_text);
		BaseAdapter adapter = null;
		mItemLv.setVisibility(View.VISIBLE);
		mItemLv.setAdapter(adapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = null;
				TextView tv = null;
				if (null == convertView) {
					v = new LinearLayout(mContext);
					v.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					// v.setPadding(paddingLeft, paddingTop, paddingRight,
					// paddingBottom);
					v.setBackgroundResource(R.drawable.dialog_top_left_button_bg);
					if (getItemViewType(position) == 1) {
						View highlightToken = new View(mContext);
						highlightToken.setBackgroundResource(R.color.dialog_button_seperator);
						highlightToken.setLayoutParams(new LinearLayout.LayoutParams(highlightTokenWidth,
								LayoutParams.MATCH_PARENT));
						((LinearLayout) v).addView(highlightToken);
					}
					tv = new TextView(mContext);
					tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
					tv.setTextColor(textColor);
					LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					tvLp.setMargins(paddingLeft, paddingTop, paddingRight, paddingBottom);
					tv.setLayoutParams(tvLp);
					((LinearLayout) v).addView(tv);
					v.setTag(tv);
				} else {
					v = convertView;
					tv = (TextView) v.getTag();
				}
				tv.setText(items[position]);
				return v;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return items[position];
			}

			@Override
			public int getCount() {
				return count;
			}

			@Override
			public int getViewTypeCount() {
				return 2;
			}

			@Override
			public int getItemViewType(int position) {
				if (null == highlightedId)
					return 0;
				for (int id : highlightedId)
					if (id == position)
						return 1;
				return 0;
			}
		});

		mItemLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				l.onClick(mDialog, arg2);
				mDialog.dismiss();
			}
		});
		RelativeLayout.LayoutParams params = null;
		switch (listViewHeight) {
		case ITEM_HEIGHT_DEFAULT:
			if (adapter.getCount() > 4) {
				params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						(int) (res.getDimensionPixelSize(R.dimen.dialog_item_list_max_height)));

				break;
			}
		case ITEM_HEIGHT_WRAP_CONTENT:
			params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			break;
		case ITEM_HEIGHT_MATCH_PARENT:
			params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			break;
		default:
			params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, listViewHeight);
			break;
		}

		mItemLv.setLayoutParams(params);
		return this;
	}

	public MMSDialog setContentView(View v) {
		mContentContainer.setVisibility(View.VISIBLE);
		mContentContainer.removeAllViews();
		mContentContainer.addView(v);
		return this;
	}

	public MMSDialog setContentView(int resId) {
		View v = View.inflate(mContext, resId, null);
		return setContentView(v);
	}

	public MMSDialog setNegativeButton(CharSequence title, final DialogInterface.OnClickListener l) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtNegative.setVisibility(View.VISIBLE);
		mBtNegative.setText(title);
		mBtNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				l.onClick(MMSDialog.this, BUTTON_NEGATIVE);
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setNegativeButton(CharSequence title, final Message msg) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtNegative.setVisibility(View.VISIBLE);
		mBtNegative.setText(title);
		mBtNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				msg.sendToTarget();
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setNeutralButton(CharSequence title, final DialogInterface.OnClickListener l) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtNeutral.setVisibility(View.VISIBLE);
		mBtNeutral.setText(title);
		mBtNeutral.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				l.onClick(MMSDialog.this, BUTTON_NEUTRAL);
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setNeutralButton(CharSequence title, final Message msg) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtNeutral.setVisibility(View.VISIBLE);
		mBtNeutral.setText(title);
		mBtNeutral.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				msg.sendToTarget();
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setPositiveButton(CharSequence title, final DialogInterface.OnClickListener l) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtPositive.setVisibility(View.VISIBLE);
		mBtPositive.setText(title);
		mBtPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				l.onClick(MMSDialog.this, BUTTON_POSITIVE);
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setPositiveButton(CharSequence title, final Message msg) {
		mButtonBar.setVisibility(View.VISIBLE);
		mBtPositive.setVisibility(View.VISIBLE);
		mBtPositive.setText(title);
		mBtPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				msg.sendToTarget();
				dismiss();
			}
		});
		updateButtonBg();
		return this;
	}

	public MMSDialog setExtraButtons(String[] items, final OnClickListener l) {
		final View extraContainer = mView.findViewById(R.id.dialog_extra_button_scroll_view);
		extraContainer.setVisibility(View.VISIBLE);
		mExtraBtPanel.removeAllViews();
		int rowCount = (int) Math.ceil((float) items.length / 3);
		for (int i = 0; i < rowCount; i++) {
			ViewGroup container = (ViewGroup) View.inflate(mContext, R.layout.dialog_extra_button_bar, null);
			for (int j = 0; j < Math.min(items.length - i * 3, 3); j++) {
				final int id = i * 3 + j;
				Button b = (Button) container.getChildAt(j);
				b.setEnabled(true);
				b.setText(items[id]);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						l.onClick(mDialog, id);
					}
				});
			}
			mExtraBtPanel.addView(container);
		}
		return this;
	}

	public Button getButton(int which) {
		Button b = null;
		switch (which) {
		case BUTTON_POSITIVE:
			b = mBtPositive;
			break;
		case BUTTON_NEUTRAL:
			b = mBtNeutral;
			break;
		case BUTTON_NEGATIVE:
			b = mBtNegative;
		default:
			break;
		}
		if (null != b && b.getVisibility() == View.VISIBLE)
			return b;
		return null;
	}

	private void updateButtonBg() {
		mBtNeutral
				.setBackgroundResource(mBtNegative.getVisibility() == View.VISIBLE ? R.drawable.dialog_bottom_right_button_bg
						: R.drawable.dialog_bottom_left_button_bg);

		mBtPositive.setBackgroundResource(mBtNegative.getVisibility() == View.VISIBLE
				|| mBtNeutral.getVisibility() == View.VISIBLE ? R.drawable.dialog_bottom_right_button_bg
				: R.drawable.dialog_bottom_left_button_bg);
	}

	public MMSDialog setTitle(int resId) {
		return setTitle(mContext.getText(resId));
	}

	public MMSDialog setTitle(CharSequence title) {
		mTitleBar.setVisibility(View.VISIBLE);
		mTitleTv.setText(title);
		return this;
	}

	public MMSDialog setIcon(Drawable d) {
		mTitleBar.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.VISIBLE);
		mIcon.setImageDrawable(d);
		return this;
	}

	public MMSDialog setIcon(int resId) {
		mTitleBar.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.VISIBLE);
		mIcon.setImageResource(resId);
		return this;
	}

	public MMSDialog setOnShowListener(OnShowListener l) {
		this.mOnShowListener = l;
		return this;
	}

	public Dialog getDialog() {
		return mDialog;
	}

	public Dialog show() {
		if (!mDialog.isShowing())
			mDialog.show();
		return mDialog;
	}

	@Override
	public void cancel() {
		mDialog.cancel();
	}

	@Override
	public void dismiss() {
		mDialog.dismiss();
	}

}
