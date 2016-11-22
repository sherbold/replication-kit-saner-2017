package weka_predictor;

//weka core
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.WekaPackageManager;

// classifiers
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.Logistic;

//resampling
import weka.filters.Filter;

//java 
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Arrays;
//cli parsing
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

// evaluation
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;

//reporting
import weka_predictor.PerfReport;
import weka_predictor.WekaClassifiers;
import weka_predictor.SqlConnect;

public class SfpCpfpEvaluation {
	
	//cli names
	static String cmd_run = "run";
	static String cmd_import = "import";
	static String cmd_basepath = "basepath";
	static String cmd_settingspath = "settings";
	static String cmd_help = "help";
	
	//main method
	public static void main(String[] args) throws Throwable
	{
		if (args.length > 0)
		{
			RunSettings cur_settings = null;
			
			// create Options object
			Options options = new Options();
			
			// add t option
			options.addOption("r",cmd_run, false, "run the evaluation");
			options.addOption("i",cmd_import,false,"import results into database");
			options.addOption("b",cmd_basepath,true,"base path to look for input files");
	        options.addOption("s",cmd_settingspath, true, "path to settings file");
	        options.addOption("?",cmd_help, false, "print this message" );
			
	        CommandLineParser parser = new DefaultParser();
	        
        	String base_file_path = null;
			String settings_file = null;
	        
	        try
	        {
	        	CommandLine cmd = parser.parse( options, args);
	        	if (cmd.hasOption(cmd_help))
		        {
			        handleHelpMessage(options);
			        System.exit(0);
		        }
		        base_file_path = cmd.getOptionValue(cmd_basepath);
		        settings_file = cmd.getOptionValue(cmd_settingspath);
		        
		        if(base_file_path == null)
		        {
		        	System.err.print("NO bae path specified, can not start! Please use "+cmd_basepath+" to specify.");
		        	handleHelpMessage(options);
		        	System.exit(-1);
		        }
		        if(settings_file == null)
		        {
		        	System.err.print("NO settings file specified, can not start! Please use "+cmd_settingspath+" to specify.");
		        	handleHelpMessage(options);
		        	System.exit(-1);
		        }
		        
		        System.out.println("------------------");
		        System.out.println("CLI settings: ");
		        
				boolean perf_run = false;
		        if(cmd.hasOption(cmd_run))
		        {
		        	perf_run = true;
		        	System.out.println("--> evaluation run activated");
		        }
		        else
		        {
		        	System.out.println("--> NO evaluation run activated");
		        }
		        
		        boolean perf_import = false;
		        if (cmd.hasOption(cmd_import))
		        {
		        	perf_import = true;
		        	System.out.println("--> import enabled");
		        }
		        else
		        {
		        	System.out.println("--> NO import enabled");
		        }
				cur_settings = new RunSettings(base_file_path,settings_file,";",perf_run,perf_import);
	        }
	        catch (Exception e)
	        {
	        	System.out.println(e.getMessage());
	        	handleHelpMessage(options);
	        	System.exit(-1);
	        }
	        
			try
			{
				System.out.println("------------------");
				System.out.println("Load settings:");
				cur_settings.loadSettings();
				System.out.println("------------------");
				System.out.println("CLASSIFIERS: ");
				cur_settings.listClassifiers();
				System.out.println("------------------");
				
				if(cur_settings.perf_run_)
				{
					run(cur_settings);
				}
				
				//database import
				if(cur_settings.perf_import_)
				{
					if(cur_settings.sql_db_name_.length() > 0 &&
							cur_settings.sql_server_adress_.length() > 0 &&
							cur_settings.sql_server_port_ > 0)
					{
						importIntoDb(cur_settings,cur_settings.clear_sql_befor_import_);
					}
					else
					{
						System.out.println("no import du to missing sql settings");
					}
				}
				
				//output split and upsampled test/train datasets
				if(cur_settings.output_arrf_)
				{
					generateArffFiles(cur_settings);
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
			
			System.exit(0); //eod
			
		}
		else
		{
			System.err.print("no Startupfolder defined! Please use ARGS[0] to set a path");
			System.exit(-1);
		}
	}

	private static void handleHelpMessage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "SfpCpfPEvluation", options );
	}
	
	//-----------------------------------------------------------------
	public static void importIntoDb(RunSettings cur_settings, Boolean clear_table) throws ClassNotFoundException, SQLException, IOException
	{
		SqlConnect mysql_connect = new SqlConnect(cur_settings.sql_server_adress_,Integer.toString(cur_settings.sql_server_port_),cur_settings.sql_user_,cur_settings.sql_db_name_,cur_settings.sql_password_);
		
		if(clear_table)
		{
			if (cur_settings.run_sfp_)
			{
				mysql_connect.clearTable(cur_settings.sql_tbl_name_sfp_);
			}
			if(cur_settings.run_cv_)
			{
				mysql_connect.deleteCvFromTable(cur_settings.sql_tbl_name_sfp_, cur_settings.cv_num_);
			}
			if (cur_settings.run_cpfp_)
			{
				mysql_connect.clearTable(cur_settings.sql_tbl_name_cpfp_);
			}
		}
		
		if(cur_settings.run_sfp_)
		{
			mysql_connect.importIntoDb(cur_settings.sql_tbl_name_sfp_, cur_settings.csv_output_file_path_sfp_);
		}
		if (cur_settings.run_cv_)
		{
			mysql_connect.importIntoDb(cur_settings.sql_tbl_name_sfp_, cur_settings.csv_output_file_path_cv_);
		}
		if (cur_settings.run_cpfp_)
		{
			mysql_connect.importIntoDb(cur_settings.sql_tbl_name_cpfp_, cur_settings.csv_output_file_path_cpfp_);
		}
	}

	//-----------------------------------------------------------------	
	public static void generateArffFiles(RunSettings cur_settings) throws Throwable
	{
		System.out.println("#############################################");
		for (double cur_split_percentage: cur_settings.percentage_split_)
		{
			for (String cur_file: cur_settings.file_names_)
			{
				System.out.println("------------------------------------");
				System.out.println(cur_split_percentage + "% (train), " + (100.0 - cur_split_percentage) + "% (test)");
				System.out.println("file: " + cur_file);
				
				String file_path = cur_settings.base_file_path_ + "\\" + cur_file;
				Instances data = readArrf(file_path);
				data.setClassIndex(data.numAttributes() - 1); //set last column as class --> bug/no bug
				
				int total_instances = data.numInstances();
				int num_train_instances = (int) Math.round(total_instances * cur_split_percentage / 100.0);
				int num_test_instances = total_instances - num_train_instances;
				
				Instances train_data = new Instances(data, 0, num_train_instances);
				Instances test_data = new Instances(data, num_train_instances, num_test_instances); 
				
				String cur_file_name = (cur_file.split("\\."))[0];
				writeArff(cur_settings.base_file_path_ + "\\" + cur_file_name + "_" + Double.toString(cur_split_percentage) + "_train.arff",train_data);
				writeArff(cur_settings.base_file_path_ + "\\" + cur_file_name + "_" + Double.toString(cur_split_percentage) + "_test.arff",test_data);
				
				for (String cur_resample_settings:cur_settings.upsample_settings_)
				{
					if(cur_resample_settings.contains("weka.filters."))
					{
						String [] options = cur_resample_settings.split(" ");
						int resample_sign = Integer.parseInt(options[0]);
						String filter_class = options[1]; 
						String [] filter_options = Arrays.copyOfRange(options, 2, options.length);
						
						Instances upsampled_data = performGeneralSample(train_data,cur_settings.pos_class_index_,filter_class,filter_options);
						writeArff(cur_settings.base_file_path_ + "\\" + cur_file_name + "_" + Double.toString(cur_split_percentage) + "_upsample_" + 
								Integer.toString(resample_sign) + "_bias_" + Double.toString(0) +  ".arff",upsampled_data);
					}	
					else
					{
						for (double cur_bias: cur_settings.upsample_bias_)
						{
							System.out.println("--------------------------------------");
							
							Instances upsampled_data = train_data;
							{
								for (int cur_resample: cur_settings.upsample_percentage_)
								{
									System.out.println("resample: " + cur_resample + "%" + " Bias: " + Double.toString(cur_bias));
									upsampled_data = performResampleUpDown(train_data, cur_resample, cur_bias, cur_resample_settings);
									
									writeArff(cur_settings.base_file_path_ + "\\" + cur_file_name + "_" + Double.toString(cur_split_percentage) + "_upsample_" + 
											Integer.toString(cur_resample) + "_bias_" + Double.toString(cur_bias) +  ".arff",upsampled_data);
								}
							}
						}
					}
				}
			}
		}
		System.out.println("######################  EOD  ######################");	
	}

	//-----------------------------------------------------------------	
	public static void run(RunSettings cur_settings) throws Throwable
	{			
		//data storage
		ArrayList<PerfReport> all_reports_sfp = new ArrayList<PerfReport>();
		ArrayList<PerfReport> all_reports_cv = new ArrayList<PerfReport>();
		ArrayList<PerfReport> all_reports_cpfp = new ArrayList<PerfReport>();
		
		//----------- START Processing -----------//
		
		TimeMeasurement time_sfp = null;
		TimeMeasurement time_cpfp = null;
		TimeMeasurement time_cv = null;
		
		if (cur_settings.run_sfp_)
		{
			CsvWriter sfp_csv_report = new CsvWriter(cur_settings.csv_output_file_path_sfp_);
			time_sfp = new TimeMeasurement();
				cur_settings.prepareWekaClassifiers();
				handleWekaEvaluationSFP(cur_settings, all_reports_sfp, sfp_csv_report);
			time_sfp.stopMeasure();
			
			writeReport(all_reports_sfp, sfp_csv_report);
		}
		
		if (cur_settings.run_cv_)
		{
			CsvWriter cv_csv_report = new CsvWriter(cur_settings.csv_output_file_path_cv_);
			if (cur_settings.cv_num_.size() > 0)
			{
				time_cv = new TimeMeasurement();
				cur_settings.prepareWekaClassifiers();
				handleWekaEvaluationCV(cur_settings, all_reports_cv, cv_csv_report);
				time_cv.stopMeasure();
				writeReport(all_reports_cv, cv_csv_report);
			}
			else
			{
				System.err.println("Perfrom CV selected, but no number of CVs given --> check settings file!");
			}
		}		
		
		if(cur_settings.run_cpfp_)
		{
			//remove NASA Dataset as it is incompatible with others
			ArrayList<String> cpfp_datasets = new ArrayList<String>();
			for (String cur_file:cur_settings.file_names_)
			{
				if (!cur_file.contains("NASA"))
				{
					cpfp_datasets.add(cur_file);
				}
			}
			CsvWriter cpfp_csv_report = new CsvWriter(cur_settings.csv_output_file_path_cpfp_);
			
			time_cpfp = new TimeMeasurement();
				cur_settings.prepareWekaClassifiers();
				handleWekaEvaluationCPFP(cur_settings, all_reports_cpfp, cpfp_datasets, cpfp_csv_report);
			time_cpfp.stopMeasure();
			
			writeReport(all_reports_cpfp, cpfp_csv_report);
		}
		
		//elapsed time
		if (cur_settings.run_sfp_)
		{
			System.out.println("Time to perform SPF:   " + time_sfp.getEvolvedTime() + "sec");
		}
		if (cur_settings.run_cv_)
		{
			System.out.println("Time to perform CV" + (cur_settings.cv_num_) + ":  " + time_cv.getEvolvedTime() + "sec");
		}
		if(cur_settings.run_cpfp_)
		{
			System.out.println("Time to perform CPFP:  " + time_cpfp.getEvolvedTime() + "sec");
		}
		
		//eod
		System.out.println("######################  EOD  ######################");	
	}

	//-----------------------------------------------------------------
	private static void writeReport(ArrayList<PerfReport> all_reports_cv, CsvWriter cv_csv_report)	throws IOException, Throwable
	{
		cv_csv_report.writeToCsv(all_reports_cv);
		cv_csv_report.finalize();
		cv_csv_report = null;
		all_reports_cv.clear();
	}

	//-----------------------------------------------------------------
	private static void handleWekaEvaluationCPFP(RunSettings cur_settings, ArrayList<PerfReport> all_reports, ArrayList<String> file_names, CsvWriter cpfp_csv_report) throws Exception
	{
		cur_settings.calcMaxNumExperiments();
		PercentageReportHandler percentage_perf_report = new PercentageReportHandler(cur_settings.num_experiments_cpfp_,"experiments");

		System.out.println("XXXXXX   CPFP   XXXXXX (" + cur_settings.num_experiments_cpfp_ + " Experiments)");
		
		//create test and train pairs
		for (String cur_file: file_names)
		{
			System.out.println("#############################################");
			System.out.println("file: " + cur_file);
			
			String file_path = cur_settings.base_file_path_ + "\\" + cur_file;
			Instances train_data = readArrf(file_path);
			train_data.setClassIndex(train_data.numAttributes() - 1); //set last column as class --> bug/no bug
			
			for (String cur_file_comp: file_names)
			{
				//skip identical files
				if (cur_file_comp == cur_file)
				{
					continue;
				}
				System.out.println("comp_file: " + cur_file_comp);
				
				String file_path_comp = cur_settings.base_file_path_ + "\\" + cur_file_comp;
				Instances test_data = readArrf(file_path_comp);
				test_data.setClassIndex(test_data.numAttributes() - 1); //set last column as class --> bug/no bug
				
				handleEvaluation(all_reports, cur_settings.pos_class_index_, cur_settings.neg_class_index_, cur_settings.upsample_percentage_,
						cur_settings.upsample_bias_,cur_settings.upsample_settings_, 0, 
						cur_file, train_data, test_data,cur_file_comp,cpfp_csv_report,
						cur_settings.weka_classifiers_,cur_settings.num_experiments_cpfp_,percentage_perf_report);	
			}	
		}
	}
	
	//-----------------------------------------------------------------
	private static void handleWekaEvaluationCV(RunSettings cur_settings, ArrayList<PerfReport> all_reports, CsvWriter sfp_csv_report) throws Exception
	{
		cur_settings.calcMaxNumExperiments();
		System.out.println("XXXXXX   SFP VC  XXXXXX (" + cur_settings.num_experiments_cv_ + " Experiments)");
		PercentageReportHandler percentage_perf_report = new PercentageReportHandler(cur_settings.num_experiments_cv_,"experiments");
		
		for (String cur_file: cur_settings.file_names_)
		{
			System.out.println("#############################################");
			System.out.println("file: " + cur_file);
			
			String file_path = cur_settings.base_file_path_ + "\\" + cur_file;
			Instances data = readArrf(file_path);
			data.setClassIndex(data.numAttributes() - 1); //set last column as class --> bug/no bug
			
			for (int cur_cv:cur_settings.cv_num_)
			{
			
				Random rand = new Random(10);     			// create seeded number generator
				Instances randData = new Instances(data);   // create copy of original data
				randData.randomize(rand);         			// randomize data with number generator
			
				ArrayList<PerfReport> cv_reports = new ArrayList<PerfReport>();
				
				//perform fold cross validation
				for (int n = 0; n < cur_cv; n++)
				{
					Instances train_data = randData.trainCV(cur_cv, n);
					Instances test_data = randData.testCV(cur_cv, n);
					
					performEvaluation(cv_reports, cur_settings.pos_class_index_, cur_settings.neg_class_index_,
							cur_file, test_data, 0, 0.0,train_data, 0.0, "", Integer.toString(cur_cv), cur_settings.weka_classifiers_);
					
					percentage_perf_report.reportPercentage(cur_settings.weka_classifiers_.size());
				}
				
				HashMap<String, ArrayList<PerfReport>> perf_report_sorted = new HashMap<String,ArrayList<PerfReport>>();
				//initialize Hashmap
				for (WekaClassifiers cur_classifier:cur_settings.weka_classifiers_)
				{
					perf_report_sorted.put(cur_classifier.classifier_name_, new ArrayList<PerfReport>());		
				}
				//fill up
				for (PerfReport cur_report:cv_reports)
				{
					perf_report_sorted.get(cur_report.weka_classifier_.classifier_name_).add(cur_report);
				}
				
				//ArrayList<PerfReport> accumulated_reports = new ArrayList<PerfReport>();
				for (WekaClassifiers cur_classifier:cur_settings.weka_classifiers_)
				{
					ArrayList<PerfReport> perf_reports = perf_report_sorted.get(cur_classifier.classifier_name_);
					PerfReport accumulated_report = null;
					for(PerfReport cur_report_per_classifier:perf_reports)
					{
						if(accumulated_report == null)
						{
							accumulated_report = cur_report_per_classifier;
							continue;
						}
						accumulated_report.addAccumulateReport(cur_report_per_classifier);
					}
					accumulated_report.calcAccumulation();
					all_reports.add(accumulated_report);
				}
			}
		}
	}
	
	//-----------------------------------------------------------------
	private static void handleWekaEvaluationSFP(RunSettings cur_settings, ArrayList<PerfReport> all_reports, CsvWriter sfp_csv_report) throws Exception
	{
		cur_settings.calcMaxNumExperiments();
		PercentageReportHandler percentage_perf_report = new PercentageReportHandler(cur_settings.num_experiments_sfp_,"experiments");
		
		System.out.println("XXXXXX   SFP   XXXXXX (" + cur_settings.num_experiments_sfp_ + " Experiments)");
		
		for (double cur_split_percentage: cur_settings.percentage_split_)
		{
			for (String cur_file: cur_settings.file_names_)
			{
				try
				{
					System.out.println("#############################################");
					System.out.println(cur_split_percentage + "% (train), " + (100.0 - cur_split_percentage) + "% (test)");
					System.out.println("file: " + cur_file);
					
					String file_path = cur_settings.base_file_path_ + "\\" + cur_file;
					Instances data = readArrf(file_path);
					data.setClassIndex(data.numAttributes() - 1); //set last column as class --> bug/no bug
					
					int total_instances = data.numInstances();
					int num_train_instances = (int) Math.round(total_instances * cur_split_percentage / 100.0);
					int num_test_instances = total_instances - num_train_instances;
					
					Instances train_data = new Instances(data, 0, num_train_instances);
					Instances test_data = new Instances(data, num_train_instances, num_test_instances); 
					
					handleEvaluation(all_reports, cur_settings.pos_class_index_, cur_settings.neg_class_index_,
									cur_settings.upsample_percentage_, cur_settings.upsample_bias_, cur_settings.upsample_settings_,
									cur_split_percentage, cur_file, train_data, test_data,cur_file,
									sfp_csv_report,cur_settings.weka_classifiers_,cur_settings.num_experiments_sfp_,percentage_perf_report);
				}
				catch (Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
	}

	//-----------------------------------------------------------------	
	private static void handleEvaluation(ArrayList<PerfReport> all_reports, int pos_class_index, int neg_class_index,
			ArrayList<Integer> upsample_percentage, ArrayList<Double> upsample_bias, ArrayList<String> upsample_settings, double cur_split_percentage,
			String cur_file, Instances train_data, Instances test_data, String name_testdata, CsvWriter sfp_csv_report, ArrayList<WekaClassifiers> settings_classifier,int max_num_experiments,PercentageReportHandler percentage_perf_report) throws Exception {
		
		for (String cur_resample_settings:upsample_settings)
		{			
			if(cur_resample_settings.contains("weka.filters."))
			{
				String [] options = cur_resample_settings.split(" ");
				int resample_sign = Integer.parseInt(options[0]);
				String filter_class = options[1]; 
				String [] filter_options = Arrays.copyOfRange(options, 2, options.length);
				
				Instances upsampled_data = performGeneralSample(train_data,pos_class_index,filter_class,filter_options);
				performEvaluation(all_reports, pos_class_index, neg_class_index, cur_file, 
						test_data, resample_sign, 0, upsampled_data,cur_split_percentage,cur_resample_settings,name_testdata,settings_classifier);
			}
			else
			{
				for (int cur_resample: upsample_percentage)
				{
					//System.out.println("--------------------------------------");
					performPercentageEvaluation(all_reports, pos_class_index, neg_class_index, upsample_bias,
							cur_split_percentage, cur_file, train_data, test_data, name_testdata, settings_classifier,
							cur_resample_settings, cur_resample);
				}
			}
			//save calculations to csv
			sfp_csv_report.writeToCsv(all_reports);
			
			percentage_perf_report.reportPercentage(all_reports.size());
			
			//clear calculations
			all_reports.clear();
		}
	}

	//-----------------------------------------------------------------	
	private static void performPercentageEvaluation(ArrayList<PerfReport> all_reports, int pos_class_index,
			int neg_class_index, ArrayList<Double> upsample_bias, double cur_split_percentage, String cur_file,
			Instances train_data, Instances test_data, String name_testdata,
			ArrayList<WekaClassifiers> settings_classifier, String cur_resample_settings, int cur_resample)
			throws Exception {
		if (cur_resample == 0)
		{
			//System.out.println("no resampling --> just Split");
			performEvaluation(all_reports, pos_class_index, neg_class_index, cur_file,
					test_data, cur_resample, 0, train_data,cur_split_percentage,"",name_testdata, settings_classifier);
		}
		else
		{
			for (double cur_bias: upsample_bias)
			{
				try
				{
					{
						Instances upsampled_data = performResampleUpDown(train_data, cur_resample, cur_bias, cur_resample_settings);
						performEvaluation(all_reports, pos_class_index, neg_class_index, cur_file, 
							test_data, cur_resample, cur_bias, upsampled_data,cur_split_percentage,cur_resample_settings,name_testdata,settings_classifier);
					}
				}
				catch (Exception e)
				{
					// will occure if supervised resampling is selected and bias is specified
					System.out.println(e.getMessage());
				}
			}
		}
	}

	//-----------------------------------------------------------------
	private static Instances performGeneralSample(Instances train_data,int pos_class_index, String filter_class, String [] filter_options) throws Exception
	{
		Filter upsample_train_data = (Filter) Class.forName(filter_class).newInstance();
		upsample_train_data.setOptions(filter_options);
		upsample_train_data.setInputFormat(train_data);
		return Filter.useFilter(train_data, upsample_train_data);
	} 
	
	//-----------------------------------------------------------------------
	private static Instances performResampleUpDown(Instances train_data, int cur_resample,
			double cur_bias, String cur_resample_settings)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, Exception
	{
		Instances upsampled_data;
		String resample_options;
		
		resample_options = "-Z " + Integer.toString(cur_resample);
		Filter upsample_train_data = null;
		if (cur_bias != 0)
		{
			//WARNING: only allowed if using normal distributed input data!
			resample_options += " -B " + Double.toString(cur_bias);
			upsample_train_data = (Filter) Class.forName("weka.filters.supervised.instance.Resample").newInstance();
		}
		else
		{
			upsample_train_data = (Filter) Class.forName("weka.filters.unsupervised.instance.Resample").newInstance();
		}
		if (cur_resample_settings.length() > 0)
		{
			resample_options += " " + cur_resample_settings;
		}

		upsample_train_data.setOptions(resample_options.split(" "));
		upsample_train_data.setInputFormat(train_data);
		upsampled_data = Filter.useFilter(train_data, upsample_train_data);
		return upsampled_data;
	}

	//-----------------------------------------------------------------------	
	private static void performEvaluation(ArrayList<PerfReport> all_reports, int pos_class_index, int neg_class_index,
			String cur_file, Instances test_data, int cur_resample, double cur_bias,
			Instances upsampled_data, double cur_split_percentage, String cur_resample_settings, String name_testdata, ArrayList<WekaClassifiers> settings_classifier) throws Exception {
		
		//static settings if no classifiers read from setting file
		if (settings_classifier.size() == 0)
		{
			settings_classifier = prepareDefaultClassifier(upsampled_data);
			System.out.println("----------------------");
			System.out.println("WARNING: default classifiers loaded.");
			for(WekaClassifiers cur_default_classifier:settings_classifier)
			{
				System.out.println(cur_default_classifier.classifier_name_);
			}
			System.out.println("----------------------");
		}
		else
		{
			try //if a single classifier fails
			{
				//prepare classifiers
				for (WekaClassifiers cur_classifier:settings_classifier)
				{
					cur_classifier.weka_classifier_.buildClassifier(upsampled_data);
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		
		for (WekaClassifiers cur_classifier:settings_classifier)
		{
			try //if a single classifier fails
			{
				PerfReport tmp_report = evaluateClassifier(upsampled_data, test_data, cur_classifier, pos_class_index,neg_class_index);
				tmp_report.bias_ = cur_bias;
				tmp_report.resample_ = cur_resample;
				tmp_report.filepath_ = cur_file;
				tmp_report.split_percentage_ = cur_split_percentage;
				tmp_report.setResampleSettings(cur_resample_settings);
				tmp_report.filepath_testdata_ = name_testdata;
				all_reports.add(tmp_report);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
			
		}
	}
	
	//-----------------------------------------------------------------------
	private static ArrayList<WekaClassifiers> prepareDefaultClassifier(Instances train_data) throws Exception
	{
		NaiveBayes nb_classifier = new NaiveBayes();
		String nb_option_string = " -D";
		String nb_options [] = nb_option_string.trim().split(" ");
		nb_classifier.setOptions(nb_options);
		nb_classifier.buildClassifier(train_data);
		
		NaiveBayes nb_ada_classifier = new NaiveBayes();
		nb_ada_classifier.setOptions(nb_options);
		AdaBoostM1 ada_classifier = new AdaBoostM1();
		String ada_option_string = " -I 100 -Q";
		String ada_options [] = ada_option_string.trim().split(" ");
		ada_classifier.setOptions(ada_options);
		ada_classifier.setClassifier(nb_ada_classifier);
		ada_classifier.buildClassifier(train_data);
		
		RandomForest rf_classifier = new RandomForest();
		String rf_option_string = " -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
		String rf_options [] = rf_option_string.trim().split(" ");
		rf_classifier.setOptions(rf_options);
		rf_classifier.buildClassifier(train_data);
		
		RandomForest rf_ada_classifier = new RandomForest();
		rf_ada_classifier.setOptions(rf_options);
		AdaBoostM1 ada_rf_classifier = new AdaBoostM1();
		ada_rf_classifier.setOptions(ada_options);
		ada_rf_classifier.setClassifier(rf_ada_classifier);
		ada_rf_classifier.buildClassifier(train_data);
		
		WekaPackageManager.loadPackages( false, true, false );
		AbstractClassifier svm_classifier = ( AbstractClassifier ) Class.forName(
		            "weka.classifiers.functions.LibSVM" ).newInstance();
		String svm_option_string = " -S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -Z -seed 1";
		String svm_options [] = svm_option_string.trim().split(" ");
		svm_classifier.setOptions(svm_options);
		svm_classifier.buildClassifier(train_data);
		
		Logistic logistic_classifier = new Logistic();
		String logistic_options_string = " -R 1.0E-8 -M -1 -num-decimal-places 4";
		String logistic_options[] = logistic_options_string.trim().split(" ");
		logistic_classifier.setOptions(logistic_options);
		logistic_classifier.buildClassifier(train_data);
		
		AbstractClassifier mlr_xgboost = ( AbstractClassifier ) Class.forName(
	            "weka.classifiers.mlr.MLRClassifier" ).newInstance();
		String mlr_xgboost_option_string = " -learner classif.xgboost -batch 100 -S 1";
		mlr_xgboost.setOptions(mlr_xgboost_option_string.trim().split(" "));
		mlr_xgboost.buildClassifier(train_data);
		
		ArrayList<WekaClassifiers> return_val = new ArrayList<WekaClassifiers>();
		//return_val.add(new WekaClassifiers("NaiveBayes",nb_option_string,nb_classifier));
		return_val.add(new WekaClassifiers("Ada Boost with Naive Bayes", ada_option_string, ada_classifier));
		return_val.add(new WekaClassifiers("Random Forest", rf_option_string, rf_classifier));
		return_val.add(new WekaClassifiers("Ada Boost with Random Forests", ada_option_string, ada_rf_classifier));
		return_val.add(new WekaClassifiers("SVM", svm_option_string, svm_classifier));
		return_val.add(new WekaClassifiers("Logistic regression", logistic_options_string, logistic_classifier));
		return_val.add(new WekaClassifiers("XGBOOST", mlr_xgboost_option_string, mlr_xgboost));
		
		return return_val;
	}

	//-----------------------------------------------------------------------
	private static PerfReport evaluateClassifier(Instances train_data, Instances test_data, WekaClassifiers weka_classifier, int pos_class, int neg_class)
			throws Exception {
		Evaluation eval_class = new Evaluation(train_data);
		//String[] eval_options = {"-t ", file_path, "-no-cv", "-split-percentage", "60", "-preserve-order" };
		//String[] eval_options = {"-no-cv", "-preserve-order" };

		AbstractOutput predict_output = null;
		eval_class.evaluateModel(weka_classifier.weka_classifier_, test_data, predict_output);
		
		/*
		System.out.println("------------------------------------");
		System.out.println("AUPRC pos: " + eval_class.areaUnderPRC(pos_class));
		System.out.println("AUPRC neg: " + eval_class.areaUnderPRC(neg_class));
		System.out.println("AUROC pos: " + eval_class.areaUnderROC(pos_class));
		System.out.println("AUROC neg: " + eval_class.areaUnderROC(neg_class));
		System.out.println("------------------------------------");
		*/
		
		return new PerfReport(
				weka_classifier,
				eval_class.areaUnderROC(pos_class),
				eval_class.recall(pos_class),
				eval_class.precision(pos_class),
				eval_class.falseNegativeRate(pos_class),
				eval_class.truePositiveRate(pos_class),
				eval_class.numTruePositives(pos_class),
				eval_class.numTrueNegatives(pos_class),
				eval_class.numFalsePositives(pos_class),
				eval_class.numFalseNegatives(pos_class),
				eval_class.correct(),
				eval_class.incorrect(),
				eval_class.weightedRecall(),
				eval_class.weightedPrecision(),
				eval_class.weightedAreaUnderROC()
				);
	}
	
	//-----------------------------------------------------------------------
	public static Instances readArrf(String path_to_arff)
	{
		try
		{
			DataSource source = new DataSource(path_to_arff);
			Instances data = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
			
			return data;
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	//-----------------------------------------------------------------------
	public static void writeArff(String path_to_arff, Instances data) throws IOException
	{
		 // save labeled data
		 BufferedWriter writer = new BufferedWriter(new FileWriter(path_to_arff));
		 writer.write(data.toString());
		 writer.newLine();
		 writer.flush();
		 writer.close();
	}
}




