package weka_predictor;

import java.util.ArrayList;

//sql
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

//csv
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class SqlConnect {
	
	private String db_name_;
	private String server_name_;
	private String port_;
	private String user_;
	private String password_;
	
	private String cur_sql_cmd_;
	
	private Connection connect_ = null;
	private Statement statement_ = null;
	private ResultSet result_set_ = null;
	
	private PreparedStatement sql_update_statement_ = null;
	
	private ArrayList<Integer> str_columns_id_;
	
	//-----------------------------------------------------------------
	public SqlConnect(String server_name,String port,String user,String db_name, String password) throws SQLException, ClassNotFoundException
	{
		db_name_ = db_name;
		server_name_ = server_name;
		port_ = port;
		user_ = user;
		password_ = password;
		
		str_columns_id_ = new ArrayList<Integer>();
		
		 // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        
        if(password_.length() > 0)
        {
        	connect_ = DriverManager.getConnection("jdbc:mysql://" + server_name_ + ":" +  port_ + "/" + db_name_ + "?user=" + user_+"&password=" + password_);
        }
        else
        {
        	connect_ = DriverManager.getConnection("jdbc:mysql://" + server_name_ + ":" +  port_ + "/" + db_name_ + "?user=" + user_);
        }
	}
	
	//-----------------------------------------------------------------
	public void clearTable(String tbl_name) throws SQLException
	{
		String sql_cmd = "TRUNCATE TABLE " + tbl_name;
		sql_update_statement_ = connect_.prepareStatement(sql_cmd);
		sql_update_statement_.executeUpdate();
	}
	
	//-----------------------------------------------------------------
	public void deleteCvFromTable(String tbl_name, ArrayList<Integer> cv_num) throws SQLException
	{
		String cv_num_readable = "";
		for (int cur_cv:cv_num)
		{
			cv_num_readable += Integer.toString(cur_cv) + ",";
		}
		
		//compare selected CVs with them in database
		ArrayList<Integer> comp_cv = new ArrayList<Integer>();
		String sql_cv_num = "SELECT file_testdata FROM " +tbl_name+ " WHERE file_testdata REGEXP '^[0-9]+$' group by file_testdata";
		sql_update_statement_ = connect_.prepareStatement(sql_cv_num);
		ResultSet rs = sql_update_statement_.executeQuery(sql_cv_num);
		while(rs.next())
		{
			comp_cv.add(rs.getInt("file_testdata"));
		}
		
		for(int cur_cv_comp:comp_cv)
		{
			if (!cv_num.contains(cur_cv_comp))
			{
				System.out.println("WARNING: CV " + Integer.toString(cur_cv_comp) + " exists in " + tbl_name + " but not in config! --> will not be deleted, dublicated db entry might exists!");
			}
		}
		
		cv_num_readable = cv_num_readable.substring(0, cv_num_readable.length()-1);
		String sql_cmd = "delete from " + tbl_name + " where file_testdata in (" + cv_num_readable + ")";
		sql_update_statement_ = connect_.prepareStatement(sql_cmd);
		sql_update_statement_.executeUpdate();
	}

	//-----------------------------------------------------------------
	private void getStrColumId(String tbl_name) throws SQLException {
		// Statements allow to issue SQL queries to the database
        statement_ = connect_.createStatement();
        // Result set get the result of the SQL query
        String sql_get_column_str_id = "SELECT Ordinal_position FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '"  +  tbl_name +  "' and DATA_TYPE like '%varchar%'";
        result_set_ = statement_.executeQuery(sql_get_column_str_id);   
        
        if (str_columns_id_.size() > 0)
        {
        	str_columns_id_.clear();
        }
        
        while(result_set_.next())
        {
        	str_columns_id_.add(result_set_.getInt(1));
        }
	}


	public void importIntoDb(String tbl_name, String filepath) throws IOException, SQLException
	{
		System.out.println("Start to import " + filepath + " into tbl: " + tbl_name);
		
		//get number of lines first
		LineNumberReader line_num_reader = new LineNumberReader(new FileReader(filepath));
		while ((line_num_reader.readLine()) != null);
        int num_lines = line_num_reader.getLineNumber();
        line_num_reader.close();
		
		BufferedReader csv_read = new BufferedReader(new FileReader(filepath));
		String cur_line = null;
		Boolean first_line = true;
		
		getStrColumId(tbl_name);
		
		PercentageReportHandler percentage_perf_report = new PercentageReportHandler(num_lines,"imports");
		
		while ((cur_line = csv_read.readLine()) != null)
		{
			percentage_perf_report.reportPercentage(1);
			
			//skip header
			if (first_line)
			{
				first_line = false;
				continue;
			}
			
			try
			{
				//String value_str = cur_line.replace(';', ',');
				String [] values = cur_line.split(";");
				
				for (int cur_pos:str_columns_id_)
				{
					values[cur_pos-1] = "'" + values[cur_pos-1] + "'";
				}
				
				String value_str = "";
				for (String cur_value:values)
				{
					value_str += cur_value + ",";
				}
				
				cur_sql_cmd_ = "INSERT INTO " + tbl_name + " VALUES (" + value_str.substring(0,value_str.length()-1) + ")";
				//sql_insert_cmd = sql_insert_cmd.replaceAll("NaN", "NULL");
	
				sql_update_statement_ = connect_.prepareStatement(cur_sql_cmd_);
				sql_update_statement_.executeUpdate();
			}
			catch (SQLException e)
			{
				String e_message = e.getMessage().toString();
				//System.out.println(e_message);
				if (e_message.contains("NaN"))
				{
					try
					{
						cur_sql_cmd_ = cur_sql_cmd_.replaceAll("NaN", "NULL");
						sql_update_statement_ = connect_.prepareStatement(cur_sql_cmd_);
						sql_update_statement_.executeUpdate();
					}
					catch (Exception ee)
					{
						System.out.println(ee.getMessage());
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
			
		}
		
		csv_read.close();
		System.out.println("---------- DONE");
	}
	
	
}
