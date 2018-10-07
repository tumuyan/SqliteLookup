package com.darcye.sqlitelookup.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.darcye.sqlite.DBTransaction;
import com.darcye.sqlite.DaoFactory;
import com.darcye.sqlite.DbSqlite;
import com.darcye.sqlite.IBaseDao;
import com.darcye.sqlitelookup.R;
import com.darcye.sqlitelookup.adapter.SimpleListAdapter;
import com.darcye.sqlitelookup.model.DbModel;
import com.darcye.sqlitelookup.utils.FilePath;

public class DbActivity extends BaseActivity implements View.OnClickListener {

	private ImageView mIvAddDb;
	private RecyclerView mRvDbList;
	private DbHistoryAdapter mHistoryAdapter;
	private List<DbModel> mHistoryData;
	private View mVEmptyAddDb;


	protected static final int FILE_SELECT_CODE = 0;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private   static final int REQUESTCODE_FROM_ACTIVITY = 1000;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

	public static void verifyStoragePermissions(Activity activity) {
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_db);
		mIvAddDb = findView(R.id.iv_right);
		mRvDbList = findView(R.id.list_db);
		mVEmptyAddDb = findView(R.id.iv_add_db);
		mRvDbList.setLayoutManager(new LinearLayoutManager(this));
		mIvAddDb.setVisibility(View.VISIBLE);
		mIvAddDb.setImageResource(R.drawable.ic_add_db);
		mIvAddDb.setOnClickListener(this);
		verifyStoragePermissions(this);


		Intent intentg= getIntent();
String input=		getFileListFromIntent(intentg);
if(input!=null){
	File in=new File(input);
		if(in.exists() && in.isFile()){
			selectDb(input);
		}


}
	}

	// 来自PickDBActivity的函数
	private void selectDb(final String dbPath){
		SQLiteDatabase db = openOrCreateDatabase(AppContext.DB_NAME, MODE_PRIVATE, null);
		DbSqlite dbSqlite = new DbSqlite(this, db);
		final IBaseDao<DbModel> dbDao =DaoFactory.createGenericDao(dbSqlite, DbModel.class);
		DBTransaction.transact(dbSqlite, new DBTransaction.DBTransactionInterface() {
			@Override
			public void onTransact() {
				DbModel record = dbDao.queryFirstRecord("db_path=?", dbPath);
				if(record == null){
					DbModel dbModel = new DbModel();
					File dbFile = new File(dbPath);
					dbModel.dbName = dbFile.getName();
					dbModel.dbPath = dbFile.getAbsolutePath();
					dbDao.insert(dbModel);
				}
			}
		});
		dbSqlite.closeDB();

		Intent dbTablesIntent = new Intent(this,DbTablesActivity.class);
		dbTablesIntent.putExtra(DbTablesActivity.EXTRA_DB_PATH, dbPath);
		startActivity(dbTablesIntent);
		finish();
	}

	// 初始化时获取选择的文件列表
	public String getFileListFromIntent(Intent intentg) {
		  String get_action ;  //分享的action都是Intent.ACTION_SEND
		  String get_type ;

			get_action = intentg.getAction();
			get_type=intentg.getType();
			String get_text="";

			FilePath my_img_path=new FilePath(this);

			String path="";
			ArrayList<String> paths=new ArrayList<>();

		if (Intent.ACTION_SEND.equals(get_action) && get_type != null) {
			Uri imageUri=intentg.getParcelableExtra(Intent.EXTRA_STREAM);
			if (imageUri != null) {
				path=my_img_path.getPath(imageUri);
			}
		}else if (Intent.ACTION_SEND_MULTIPLE.equals(get_action) && get_type != null) {

			ArrayList<Uri> imageUris = intentg.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			if (imageUris != null) {

				for(int imgi = 0; imgi < imageUris.size();imgi++) {
					// path=my_img_path.get_all_path(imageUris.get(imgi));
					path=my_img_path.getPath(imageUris.get(imgi));
					paths.add(path);
					get_text=get_text+"\n"+path;
				}
				return get_text;
			}
		}else    if (Intent.ACTION_VIEW.equals(get_action)) {
			//按照“打开”来识别。然后发现了所有的图片查看器都和文件管理器都无法打开以标准方式“content://”提供uri的根目录下的文件
			//相当欣慰地放弃了
			Uri uri = intentg.getData();
			Log.w("init open", uri.toString());
			if (uri != null) {
				path = my_img_path.getPath(uri);
			}

		}
		return path;
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
		case R.id.iv_add_db:
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
				mVEmptyAddDb.setVisibility(View.GONE);
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
			}else{
				mVEmptyAddDb.setVisibility(View.VISIBLE);
				mVEmptyAddDb.setOnClickListener(DbActivity.this);
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
