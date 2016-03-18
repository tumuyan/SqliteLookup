package com.darcye.sqlitelookup.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.darcye.sqlite.DaoFactory;
import com.darcye.sqlite.DbSqlite;
import com.darcye.sqlite.IBaseDao;
import com.darcye.sqlitelookup.R;
import com.darcye.sqlitelookup.adapter.SimpleListAdapter;
import com.darcye.sqlitelookup.model.DbModel;

public class DbActivity extends BaseActivity implements View.OnClickListener {

	private TextView mTvTitle;
	private ImageView mIvAddDb;
	private RecyclerView mRvDbList;
	private DbHistoryAdapter mHistoryAdapter;
	private List<DbModel> mHistoryData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_db);
		mTvTitle = findView(R.id.tv_title);
		mIvAddDb = findView(R.id.iv_right);
		mRvDbList = findView(R.id.list_db);
		mRvDbList.setLayoutManager(new LinearLayoutManager(this));
		mIvAddDb.setVisibility(View.VISIBLE);
		mIvAddDb.setImageResource(R.drawable.ic_add_db);
		mIvAddDb.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refleshDbHistory();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_right:
			performPickDb();
			break;
		}
	}
	
	private void refleshDbHistory(){
		new GetHistoryListTask().execute();
	}
	
	private void performPickDb() {
		Intent pickIntent = new Intent(this, PickDbActivity.class);
		startActivity(pickIntent);
	}

	class GetHistoryListTask extends AsyncTask<Void, Void, List<DbModel>> {

		@Override
		protected List<DbModel> doInBackground(Void... params) {
			SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME,
					MODE_PRIVATE, null);
			DbSqlite dbSqlite = new DbSqlite(DbActivity.this, db);
			IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite,DbModel.class);
			List<DbModel> historyList = dbDao.queryAll();
			dbSqlite.closeDB();
			return historyList;
		}

		@Override
		protected void onPostExecute(List<DbModel> result) {
			super.onPostExecute(result);
			if (result != null) {
				if(mHistoryAdapter == null){
					mHistoryData = new ArrayList<DbModel>();
					mHistoryData.addAll(result);
					mHistoryAdapter = new DbHistoryAdapter(DbActivity.this, mHistoryData);
					mRvDbList.setAdapter(mHistoryAdapter);
				}else{
					mHistoryData.clear();
					mHistoryData.addAll(result);
					mHistoryAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	class DbHistoryAdapter extends SimpleListAdapter<DbModel> {
		
		List<DbModel> dbHistory;
		
		public DbHistoryAdapter(Context context, List<DbModel> data) {
			super(context, data);
			dbHistory = data;
		}

		@Override
		public void onBindViewHolder(SimpleItemViewHodler viewHolder,
				int position) {
			final DbModel dbModel = dbHistory.get(position);
			viewHolder.ivIcon.setImageResource(R.drawable.ic_db);
			viewHolder.tvText.setText(dbModel.dbName);
			viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					File dbFile = new File(dbModel.dbPath);
					if(dbFile.exists()){
						Intent dbTablesIntent = new Intent(DbActivity.this,DbTablesActivity.class);
						dbTablesIntent.putExtra(DbTablesActivity.EXTRA_DB_PATH, dbModel.dbPath);
						startActivity(dbTablesIntent);
					}else{
						SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME,MODE_PRIVATE, null);
						DbSqlite dbSqlite = new DbSqlite(DbActivity.this, db);
						IBaseDao<DbModel> dbDao = DaoFactory.createGenericDao(dbSqlite,DbModel.class);
						dbDao.delete("db_id=?", String.valueOf(dbModel.dbId));
						dbSqlite.closeDB();
						v.setBackgroundColor(getResources().getColor(R.color.disable_color));
						Toast.makeText(DbActivity.this, getString(R.string.db_remove_error), Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}
}
