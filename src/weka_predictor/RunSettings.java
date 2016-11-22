package weka_predictor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.nio.file.Paths;

import weka.classifiers.AbstractClassifier;
import weka.core.WekaPackageManager;

public class RunSettings
{
	//at constructor call
	public String base_file_path_;
	private String settings_file_;
	ArrayList<String> read_settings_;
	private String settings_file_seperator_;
	private String settings_param_seperator_;
	private FileWriter out_file_;
	public static Integer invalide_int_value_ = -1;
	public static String invalide_str_value_ = "";
	public static Boolean default_bool_value_ = false;
	
	//from settings file
	public String csv_output_file_sfp_;
	public String csv_output_file_cpfp_;
	public String csv_output_file_cv_;
	
	public String csv_output_file_path_sfp_;
	public String csv_output_file_path_cpfp_;
	public String csv_output_file_path_cv_;
	
	public String sql_tbl_name_sfp_;
	public String sql_tbl_name_cpfp_;
	
	public ArrayList<String> file_names_;
	public ArrayList<Double> percentage_split_;
	public ArrayList<Integer> upsample_percentage_;
	public ArrayList<Double> upsample_bias_;
	public ArrayList<String> upsample_settings_;
	public ArrayList<WekaClassifiers> weka_classifiers_;
	
	public int pos_class_index_;  //buggy
	public int neg_class_index_;  //fault free
	
	public String sql_server_adress_;
	public int sql_server_port_;
	public String sql_user_;
	public String sql_db_name_;
	public String sql_password_;
	
	public int num_experiments_sfp_;
	public int num_experiments_cpfp_;
	public int num_experiments_cv_;
	
	public ArrayList<Integer> cv_num_;
	
	public boolean run_sfp_;
	public boolean run_cpfp_;
	public boolean run_cv_;
	
	public boolean clear_sql_befor_import_;
	public boolean output_arrf_;
	
	public boolean perf_run_;
	public boolean perf_import_;
	
	RunSettings(String base_file_path, String settings_file, String settings_file_seperator, boolean perf_run, boolean perf_import)
	{
		base_file_path_ = base_file_path;
		settings_file_ = settings_file;
		settings_file_seperator_ = settings_file_seperator;
		settings_param_seperator_ = ",";
		read_settings_ = new ArrayList<String>();
		weka_classifiers_ = new ArrayList<WekaClassifiers>();
		perf_run_ = perf_run;
		perf_import_ = perf_import;
	}
	
	public void listClassifiers()
	{
		String cur_weka_call = null;
		ArrayList<String> weka_classifier_names = new ArrayList<String>();
		try
		{
			ArrayList<String> settings_weka = getSettingMultiple(read_settings_, "weka_classifier");
			
			for (String cur_weka: settings_weka)
			{
				cur_weka_call = cur_weka;
				ArrayList<String> settings = settingToStringList(cur_weka);
				String weka_classifier_class_name = settings.get(0).trim() + " (" + settings.get(1).trim() + ")";
				weka_classifier_names.add(weka_classifier_class_name);
			}
		}
		catch (Exception e)
		{
			System.err.println("ERROR: can not parse weka classifier settings --> error in Settings file!");
			System.err.println("check for: " + cur_weka_call);
			System.out.println(e.getMessage());
		}
		
		if(weka_classifier_names.size() == 0)
		{
			System.err.println("ERROR: no classifiers specified --> check settings file");
		}
		
		for (String weka_classifier_class_name:weka_classifier_names)
		{
			System.out.println(weka_classifier_class_name);
		}
	}
	
	//-----------------------------------------------------------------
	public void calcMaxNumExperiments()
	{
		int weka_num_classifiers = 1;
		if (weka_classifiers_.size() > 0)
		{
			weka_num_classifiers = weka_classifiers_.size();
		}
		else
		{
			System.out.println("WARNING: no classifiers initialized jet!");
		}
		
		if (percentage_split_ != null & file_names_ != null & upsample_percentage_!= null & upsample_bias_!= null & upsample_settings_ != null)
		{
			num_experiments_sfp_ = percentage_split_.size() * file_names_.size()
					* upsample_percentage_.size() * upsample_bias_.size()
					* upsample_settings_.size() * weka_num_classifiers;
			
			num_experiments_cpfp_ = ((file_names_.size()-1)*file_names_.size()) * upsample_percentage_.size() 
					*  upsample_bias_.size() * upsample_settings_.size() * weka_num_classifiers;
			
			int cv_nums = 1;
			for (int cur_cv:cv_num_)
			{
				cv_nums += cur_cv;
			}
			num_experiments_cv_ = cv_nums * weka_classifiers_.size() * file_names_.size();
		}
		else
		{
			System.out.println("ERROR: no settings loaded jet, can not calc number of experiments!");
		}
	}

	//-----------------------------------------------------------------
	public void loadStaticSettings()
	{
		reportSettings();
		runtimeSettings();
	}
	
	//-----------------------------------------------------------------	
	public void prepareWekaClassifiers() throws Exception
	{
		if (read_settings_.size() > 0)
		{
			weka_classifiers_.clear();
			weka_classifiers_ = getWekaClassifiers(read_settings_, "weka_classifier");
			calcMaxNumExperiments();
		}
		else
		{
			throw new GenericStringException("ERROR: no settings loaded, can not initialize Weka Classifiers!");
		}
	}
	
	//-----------------------------------------------------------------	
	public void loadSettings()
	{
		if (settings_file_.length() > 0)
		{
			try
			{
				//read from file into list
				read_settings_=(ArrayList<String>) Files.readAllLines(Paths.get(settings_file_), Charset.forName("UTF-8"));
				
				//Assign values
				csv_output_file_sfp_ = getSetting(read_settings_,"csv_output_file_sfp",invalide_str_value_);
				csv_output_file_cpfp_ = getSetting(read_settings_,"csv_output_file_cpfp",invalide_str_value_);
				csv_output_file_cv_ = getSetting(read_settings_,"csv_output_file_cv",invalide_str_value_);
				
				csv_output_file_path_sfp_ = base_file_path_ + '\\' + csv_output_file_sfp_;
				csv_output_file_path_cpfp_ = base_file_path_ + '\\' + csv_output_file_cpfp_; 
				csv_output_file_path_cv_ = base_file_path_ + '\\' + csv_output_file_cv_;
				
				sql_tbl_name_sfp_ = getSetting(read_settings_,"sql_tbl_name",invalide_str_value_);
				sql_tbl_name_cpfp_ = getSetting(read_settings_,"sql_tbl_name_cpfp",invalide_str_value_);
				
				file_names_ = settingToStringList(getSetting(read_settings_,"file_names",invalide_str_value_));
				percentage_split_ = settingToDoubleList(getSetting(read_settings_,"percentage_split",invalide_str_value_));
				upsample_percentage_ = settingToIntList(getSetting(read_settings_,"upsample_percentage",invalide_str_value_));
				upsample_bias_ = settingToDoubleList(getSetting(read_settings_,"upsample_bias",invalide_str_value_));
				upsample_settings_ = settingToStringList(getSetting(read_settings_,"upsample_settings",invalide_str_value_));
				
				pos_class_index_ = Integer.parseInt(getSetting(read_settings_,"pos_class_index",invalide_int_value_.toString()));
				neg_class_index_ = Integer.parseInt(getSetting(read_settings_,"neg_class_index",invalide_int_value_.toString()));

				sql_server_adress_ = getSetting(read_settings_,"sql_server_adress",invalide_str_value_);
				sql_server_port_ = Integer.parseInt(getSetting(read_settings_,"sql_server_port",invalide_int_value_.toString()));
				sql_user_ = getSetting(read_settings_,"sql_user",invalide_str_value_);
				sql_password_ = getSetting(read_settings_,"sql_password",invalide_str_value_);
				
				sql_db_name_ = getSetting(read_settings_,"sql_db_name",invalide_str_value_);
				
				cv_num_ = settingToIntList(getSetting(read_settings_,"cv_num",invalide_int_value_.toString()));
				
				run_sfp_ = getBoolSettings(getSetting(read_settings_,"run_sfp",default_bool_value_.toString()));
				run_cpfp_ = getBoolSettings(getSetting(read_settings_,"run_cpfp",default_bool_value_.toString()));
				run_cv_ = getBoolSettings(getSetting(read_settings_,"run_cv",default_bool_value_.toString()));
				
				clear_sql_befor_import_ = getBoolSettings(getSetting(read_settings_,"clear_sql_befor_import",default_bool_value_.toString()));
				output_arrf_ = getBoolSettings(getSetting(read_settings_,"output_arrf",default_bool_value_.toString()));
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
	
	//-----------------------------------------------------------------
	private ArrayList<WekaClassifiers> getWekaClassifiers(ArrayList<String> read_settings, String variable_name) throws Exception
	{
		ArrayList<WekaClassifiers> return_val = new ArrayList<WekaClassifiers>();
		ArrayList<String> settings_weka = getSettingMultiple(read_settings, "weka_classifier");
		
		WekaPackageManager.loadPackages( false, true, false );
		
		for (String cur_weka: settings_weka)
		{
			try
			{
				ArrayList<String> settings = settingToStringList(cur_weka);
				String weka_classifier_class_name = settings.get(1).trim();
				AbstractClassifier abstract_weka_classifier = ( AbstractClassifier ) Class.forName(weka_classifier_class_name).newInstance();
				abstract_weka_classifier.setOptions((settings.get(2)).trim().split(" "));
				WekaClassifiers tmp_class = new WekaClassifiers(settings.get(0), settings.get(2), abstract_weka_classifier, weka_classifier_class_name);
				return_val.add(tmp_class);
			}
			catch (Exception e)
			{
				System.err.println("ERROR: can not loead weka classifier, check settings file for: " + cur_weka);
				System.out.println(e.getMessage());
			}
		}
		return return_val;
	}
	
	//-----------------------------------------------------------------
	private boolean getBoolSettings(String setting_in)
	{
		if (setting_in.toLowerCase().contains("false"))
		{
			return false;
		}
		if (setting_in.toLowerCase().contains("true"))
		{
			return true;
		}
		return default_bool_value_;
	}

	//-----------------------------------------------------------------	
	private String getSetting(ArrayList<String> read_settings, String variable_name, String default_val_)
	{
		ArrayList<String> return_val = getSettingMultiple(read_settings, variable_name);
		if (return_val.size() > 0)
		{
			return return_val.get(0);
		}
		System.out.println("Variable: " + variable_name + " not found in settings file!");
		return default_val_;
	}
	
	//-----------------------------------------------------------------	
	private ArrayList<String> getSettingMultiple(ArrayList<String> read_settings, String variable_name)
	{
		ArrayList<String> return_val = new ArrayList<String>();
		for (String cur_line:read_settings)
		{
			String [] line_entry = cur_line.split(settings_file_seperator_);
			if ((line_entry[0]).equals(variable_name))
			{
				return_val.add(line_entry[1]);
			}
		}
		return return_val;
	}

	//-----------------------------------------------------------------	
	private ArrayList<Double> settingToDoubleList(String setting_in)
	{
		
		ArrayList<Double> return_val = new ArrayList<Double>(); 
		for(String value: setting_in.split(settings_param_seperator_))
		{
			return_val.add(Double.parseDouble(value));
		}
		return return_val;
	}
	
	//-----------------------------------------------------------------	
	private ArrayList<Integer> settingToIntList(String setting_in)
	{
		ArrayList<Integer> return_val = new ArrayList<Integer>();
		if (setting_in.length() > 0) 
		{
			for(String value: setting_in.split(settings_param_seperator_))
			{
				return_val.add(Integer.parseInt(value));
			}
		}
		return return_val;
	}
	
	//-----------------------------------------------------------------	
	private ArrayList<String> settingToStringList(String setting_in)
	{
		ArrayList<String> return_val = new ArrayList<String>(); 
		if (setting_in.length() > 0)
		{
			for(String value: setting_in.split(settings_param_seperator_))
			{
				return_val.add(value.trim());
			}
		}
		return return_val;
	}
	
	//-----------------------------------------------------------------	
	public void saveSettings()
	{
		if (settings_file_.length() > 0)
		{
			try
			{
				out_file_ = new FileWriter(settings_file_);
				
				variableToCsvString("csv_output_file",csv_output_file_sfp_);
				variableToCsvString("csv_output_file_cpfp",csv_output_file_cpfp_);
				variableToCsvString("csv_output_file_path",csv_output_file_path_sfp_);
				variableToCsvString("csv_output_file_path_cpfp",csv_output_file_path_cpfp_);
				variableToCsvString("sql_tbl_name",sql_tbl_name_sfp_);
				variableToCsvString("sql_tbl_name_cpfp",sql_tbl_name_cpfp_);
				
				arraylistToCsvString("file_names",file_names_);
				arraylistToCsvString("percentage_split",percentage_split_);
				arraylistToCsvString("upsample_percentage",upsample_percentage_);
				arraylistToCsvString("upsample_bias",upsample_bias_);
				arraylistToCsvString("upsample_settings",upsample_settings_);
				
				variableToCsvString("pos_class_index",Integer.toString(pos_class_index_));
				variableToCsvString("neg_class_index",Integer.toString(neg_class_index_));
				
				variableToCsvString("sql_server_adress",sql_server_adress_);
				variableToCsvString("sql_server_port",Integer.toString(sql_server_port_));
				variableToCsvString("sql_user",sql_user_);
				variableToCsvString("sql_db_name",sql_db_name_);
				
				arraylistToCsvString("cv_num",cv_num_);
				variableToCsvString("run_sfp",Boolean.toString(run_sfp_));
				variableToCsvString("run_cpfp",Boolean.toString(run_cpfp_));
				variableToCsvString("run_cv",Boolean.toString(run_cv_));
				variableToCsvString("clear_sql_befor_import",Boolean.toString(clear_sql_befor_import_));
				variableToCsvString("output_arrf",Boolean.toString(output_arrf_));
				
				
				out_file_.flush();
				out_file_.close();
				System.out.println("Saved settings to " + settings_file_);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
	}

	//-----------------------------------------------------------------	
	private void variableToCsvString(String variable_name, String variable_value) throws IOException
	{
		out_file_.write(variable_name + settings_file_seperator_ + variable_value);
		out_file_.write(System.getProperty( "line.separator" ));
	}

	//-----------------------------------------------------------------	
	private void arraylistToCsvString(String variable_name, ArrayList<?> variable_value) throws IOException
	{
		String params_values = "";
		for (Object cur_val:variable_value)
		{
			params_values += cur_val.toString() + settings_param_seperator_;
		}
		out_file_.write(variable_name + settings_file_seperator_ + params_values.substring(0,params_values.length()-1));
		out_file_.write(System.getProperty( "line.separator" ));
	}

	//-----------------------------------------------------------------	
	private void reportSettings()
	{
		csv_output_file_sfp_ = new String ("report_sfp.csv");
		csv_output_file_cpfp_ = new String ("report_cpfp.csv");
		csv_output_file_path_sfp_      = base_file_path_ + "\\" + csv_output_file_sfp_;
		csv_output_file_path_cpfp_ = base_file_path_ + "\\" + csv_output_file_cpfp_;

		sql_tbl_name_sfp_ = "tbl_performance";
		sql_tbl_name_cpfp_ = "tbl_performance_cpfp";
		
		sql_server_adress_ = "127.0.0.1";
		sql_server_port_ = 3306;
		sql_user_ = "root";
		sql_db_name_ = "sfp";
	}

	//-----------------------------------------------------------------	
	private void runtimeSettings()
	{
		pos_class_index_ = 1;
		neg_class_index_ = 0;
		
		file_names_ = new ArrayList<String>();
		file_names_.add("A.arff");
		file_names_.add("K.arff");
		file_names_.add("L.arff");
		file_names_.add("NASA_PC1_lang_c_.arff");

		percentage_split_ = new ArrayList<Double>();
		percentage_split_.add(50.0);
		percentage_split_.add(60.0);
		percentage_split_.add(70.0);
		percentage_split_.add(80.0);
		percentage_split_.add(90.0);

		upsample_percentage_ = new ArrayList<Integer>();
		upsample_percentage_.add(0);
		upsample_percentage_.add(50);
		upsample_percentage_.add(60);
		upsample_percentage_.add(70);
		upsample_percentage_.add(80);
		upsample_percentage_.add(90);
		upsample_percentage_.add(100);
		upsample_percentage_.add(200);
		upsample_percentage_.add(300);
		upsample_percentage_.add(400);
		upsample_percentage_.add(500);
		upsample_percentage_.add(600);
		upsample_percentage_.add(700);
		upsample_percentage_.add(800);
		upsample_percentage_.add(900);
		upsample_percentage_.add(1000);

		upsample_bias_ = new ArrayList<Double>();
		upsample_bias_.add(0.0);
		upsample_bias_.add(0.1);
		upsample_bias_.add(0.2);
		upsample_bias_.add(0.3);
		upsample_bias_.add(0.4);
		upsample_bias_.add(0.5);
		upsample_bias_.add(1.0);

		upsample_settings_ = new ArrayList<String>();
		upsample_settings_.add("");
		upsample_settings_.add("-no-replacement");
	}
}