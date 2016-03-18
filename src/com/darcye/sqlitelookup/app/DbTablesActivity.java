package com.darcye.sqlitelookup.app;

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

import com.darcye.sqlite.DaoFactory;
import com.darcye.sqlite.DbSqlite;
import com.darcye.sqlite.IBaseDao;
import com.darcye.sqlitelookup.R;
import com.darcye.sqlitelookup.adapter.SimpleListAdapter;
import com.darcye.sqlitelookup.dialog.SimpleSelectDialog;
import com.darcye.sqlitelookup.dialog.SimpleSelectDialog.OnItemSelectedListener;
import com.darcye.sqlitelookup.model.SqliteMaster;

/**
 * show tables of db
 * @author Darcy
 *
 */
public class DbTablesActivity extends BaseActivity implements View.OnClickListener ,OnItemSelectedListener{
	
	public static final String EXTRA_DB_PATH = "db-path";
	
	private static final String[] SELECT_ITEMS = {"Table Design","Table Data"};
	
	private ImageView mIvBack;
	private TextView mTvTitle;
	private RecyclerView mRvTableList;
	private TableListAdapter mTableListAdapter;
	
	private SimpleSelectDialog mDlgSelect;
	private String mSelectTable;
	
	private String mDbPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_db_tables);
		mIvBack = findView(R.id.iv_back);
		mTvTitle = findView(R.id.tv_title);
		mRvTableList = findView(R.id.list_db_tables);
		mIvBack.setOnClickListener(this);
		mRvTableList.setLayoutManager(new LinearLayoutManager(this));
		mDbPath = getIntent().getStringExtra(EXTRA_DB_PATH);
		mDlgSelect = new SimpleSelectDialog(this);
		mDlgSelect.setSelectItems(SELECT_ITEMS, this);
		listTables();
	}

	@Override
	public void onSelected(int position) {
		if(position == 0){
			selectTableDesign(mSelectTable);
		}else{
			selectTableData(mSelectTable);
		}
	}
	
	private void selectTableDesign(String tableName){
		Intent designIntent = new Intent(this,TableDesignActivity.class);
		designIntent.putExtra(TableDesignActivity.EXTRA_DB_PATH, mDbPath);
		designIntent.putExtra(TableDesignActivity.EXTRA_TABLE_NAME, tableName);
		startActivity(designIntent);
	}
	
	private void selectTableData(String tableName){
		Intent dataIntent = new Intent(this,TableDataActivity.class);
		dataIntent.putExtra(TableDataActivity.EXTRA_DB_PATH, mDbPath);
		dataIntent.putExtra(TableDataActivity.EXTRA_TABLE_NAME, tableName);
		startActivity(dataIntent);
	}
	
	@Override
	public void onClick(View v) {
		
	}
	
	private void listTables(){
		new GetDbTablesTask().execute();
	}
	
	class GetDbTablesTask extends AsyncTask<Void, Void, List<SqliteMaster>>{

		@Override
		protected List<SqliteMaster> doInBackground(Void... params) {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null,  SQLiteDatabase.OPEN_READONLY);
			DbSqlite dbSqlite = new DbSqlite(null, db);
			IBaseDao<SqliteMaster> masterDao = DaoFactory.createGenericDao(dbSqlite, SqliteMaster.class);
			List<SqliteMaster> tables = masterDao.query(new String[]{"name"}, "type=?", new String[]{"table"}, null);
			dbSqlite.closeDB();
			return tables;
		}
		
		@Override
		protected void onPostExecute(List<SqliteMaster> result) {
			super.onPostExecute(result);
			mTableListAdapter = new TableListAdapter(DbTablesActivity.this, result);
			mRvTableList.setAdapter(mTableListAdapter);
		}
	}
	
	class TableListAdapter extends SimpleListAdapter<SqliteMaster>{

		private List<SqliteMaster> data;
		
		public TableListAdapter(Context context, List<SqliteMaster> data) {
			super(context, data);
			this.data = data;
		}

		@Override
		public void onBindViewHolder(SimpleItemViewHodler viewHolder, int position) {
			final SqliteMaster table = data.get(position);
			viewHolder.ivIcon.setImageResource(R.drawable.ic_table);
			viewHolder.tvText.setText(table.name);
			viewHolder.itemView.setOnLongClickListener((new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mSelectTable = table.name;
					mDlgSelect.show();
					return true;
				}
			}));
			viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					selectTableDesign(table.name);
				}
			});
		}
	}
	
}
