package com.darcye.sqlitelookup.app;

import android.app.Activity;
import android.view.View;

abstract class BaseActivity extends Activity{
	
	@SuppressWarnings("unchecked")
	public <T extends View> T findView(int id) {
		return (T) findViewById(id);
	}
}