package cz.muni.fi.smartlib;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.muni.fi.smartlib.model.Review;
import cz.muni.fi.smartlib.service.RESTServiceHelper;
import cz.muni.fi.smartlib.utils.UIUtils;
import cz.muni.fi.smartlib.utils.Utils;
import cz.muni.fi.smartlib.utils.Validator;

public class RateBookActivity extends SherlockActivity implements OnClickListener {
	public static final String TAG = RateBookActivity.class.getSimpleName();
	
	private String mSysno;
	private EditText mReview;
	private RatingBar mRating;
	private Button mRateBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_rate);
		
		Intent incoming = getIntent();
		
		if (incoming != null) {
			mSysno = incoming.getStringExtra(RESTServiceHelper.PARAM_SYSNO);
		}
		
		ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		mReview = (EditText) findViewById(R.id.rate_review);
		mReview.setTextColor(Color.parseColor("#686868"));
		mRating = (RatingBar) findViewById(R.id.rate_rating_bar);
		mRateBtn = (Button) findViewById(R.id.rate_button);
		mRateBtn.setOnClickListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				Utils.startMainActivity(this);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.rate_button) {
			rate();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
	}
	

	private void rate() {
		if (!Validator.isNullorEmpty(mReview.getText().toString()) && Validator.isRatingOK(mRating)) {
			disableUI();
			Review r = new Review();
			r.setRating((int)mRating.getRating());
			r.setText(mReview.getText().toString());
			RESTServiceHelper.getInstance(this).sendReview(new RateBookReceiver(), r, mSysno);
		} else {
			UIUtils.makeToast(this, R.string.toast_empty_fields);			
		}
	}

	private void disableUI() {
		mRating.setEnabled(false);
		mRateBtn.setEnabled(false);
		mReview.setEnabled(false);
	}
	
	private void enableUI() {
		mRating.setEnabled(true);
		mRateBtn.setEnabled(true);
		mReview.setEnabled(true);
	}
	
	public class RateBookReceiver extends ResultReceiver {
		private boolean enabled;
		
		public RateBookReceiver() {
			super(new Handler());
			this.enabled = true;
		}

		public void unregister() {
			enabled = false;
		}
		
		@Override
	    protected void onReceiveResult (int resultCode, Bundle resultData) {
			if (enabled) {
				Log.i(TAG, "Result from REST "+ resultCode + "received.");
				boolean result = resultData.getBoolean(RESTServiceHelper.PARAM_RESULT);
				if (result) {
					UIUtils.makeToast(RateBookActivity.this, R.string.toast_rate_succ);
					finish();
				} else {
					UIUtils.makeToast(RateBookActivity.this, R.string.toast_rate_fail);
					enableUI();
				}
			}
	    }
		
	}
	
}
