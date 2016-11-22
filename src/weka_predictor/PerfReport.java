package weka_predictor;

import java.io.FileWriter;
import java.io.IOException;

import weka_predictor.WekaClassifiers;

public class PerfReport {

	double auc_; 
	double recall_; 
	double precision_; 
	double fnrate_; 
	double tprate_; 
	double tp_; 
	double tn_; 
	double fp_; 
	double fn_;
	double correct_classified_;
	double incorrect_classified_;
	
	double accuracy_;
	double type_i_;
	double type_ii_;
	double f1_;
	WekaClassifiers weka_classifier_;
	public String filepath_ = "";
	public String filepath_testdata_ = "";
	public double resample_;
	public double bias_;
	public double split_percentage_; //train vs. test data
	public int replace_resample_;
	
	public double weighted_recall_;
	public double weighted_precission_;
	public double weighted_auc_;
	
	public double g_measure_;
	public double error_measure_;
	public double mcc_;
	public double balance_;
	public double pf_;
	
	private int added_accumulated_;
	
	
	//-----------------------------------
	public PerfReport(WekaClassifiers weka_classifier, double auc, double recall, double precision, double fnrate, double tprate, double tp, double tn, double fp, double fn, double correct_classified, double incorrect_classified, double weighted_recall, double weighted_precission, double weighted_auc)
	{
		auc_ = auc;
		recall_ = recall;
		precision_ = precision	;
		fnrate_ = fnrate;
		tprate_ = tprate;
		tp_ = tp;
		tn_ = tn;
		fp_ = fp;
		fn_ = fn;
		correct_classified_ = correct_classified;
		incorrect_classified_ = incorrect_classified;
		
		weighted_recall_ = weighted_recall;
		weighted_precission_= weighted_precission;
		weighted_auc_ = weighted_auc;
		
		weka_classifier_ = weka_classifier;
		added_accumulated_ = 1;
		
		calcPerfValues();
	}
	
	//-----------------------------------
	public void addAccumulateReport(PerfReport add_report)
	{
		auc_ += add_report.auc_;
		recall_ += add_report.recall_;
		precision_ += add_report.precision_;
		fnrate_ += add_report.fnrate_;
		tprate_ += add_report.tprate_;
		tp_ += add_report.tp_;
		tn_ += add_report.tn_;
		fp_ += add_report.fp_;
		fn_ += add_report.fn_;
		correct_classified_ += add_report.correct_classified_;
		incorrect_classified_ += add_report.incorrect_classified_;
		weighted_recall_ += add_report.weighted_recall_;
		weighted_precission_ += add_report.weighted_precission_;
		weighted_auc_ += add_report.weighted_auc_;
		
		added_accumulated_++;
	}
	
	//-----------------------------------
	public void calcAccumulation()
	{
		auc_ /= added_accumulated_;
		recall_ /= added_accumulated_;
		precision_ /= added_accumulated_;
		fnrate_ /= added_accumulated_;
		tprate_ /= added_accumulated_;
		tp_ /= added_accumulated_;
		tn_ /= added_accumulated_;
		fp_ /= added_accumulated_;
		fn_ /= added_accumulated_;
		correct_classified_ /= added_accumulated_;
		incorrect_classified_ /= added_accumulated_;
		
		weighted_recall_ /= added_accumulated_;
		weighted_precission_ /= added_accumulated_;
		weighted_auc_  /= added_accumulated_;
		
		calcPerfValues();
	}
	
	//-----------------------------------
	public void setResampleSettings(String settings)
	{
		if(settings.contains("no-replacement"))
		{
			replace_resample_ = 1;
		}
		else
		{
			replace_resample_ = 0;
		}
	}
	
	//-----------------------------------
	//call additional performance values
	private void calcPerfValues()
	{
		double all_samples = tp_+tn_+fp_+fn_;
		accuracy_ = (tp_ + tn_)/(all_samples);
		type_i_ = fp_/all_samples;
		type_ii_ = fn_/all_samples;
		f1_ = 2 * precision_ * recall_ / (precision_ + recall_);
		
		pf_ = fp_/(tn_+fp_); //false positive rate
		g_measure_ = 2 * (recall_*(1-pf_)) / (recall_+(1-pf_));
		error_measure_ = (fp_ + fn_) / all_samples;
		mcc_ = (tp_*tn_-fp_*fn_)/(Math.sqrt((tp_+fp_)*(tp_+fn_)*(tn_+fp_)*(tn_+fn_)));
		balance_ = 1-(Math.sqrt((Math.pow(1-recall_,2)+Math.pow(pf_,2))) / Math.sqrt(2)) ;
	}

	//-----------------------------------
	public double[] getAsDoubleArray()
	{
		double[] return_val = {
				auc_,
				accuracy_,
				recall_,
				precision_,
				type_i_,
				type_ii_,
				f1_,
				fnrate_,
				tprate_,
				tp_,
				tn_,
				fp_,
				fn_,
				correct_classified_,
				incorrect_classified_,
				weighted_recall_ ,
				weighted_precission_,
				weighted_auc_,
				pf_,
				g_measure_,
				error_measure_,
				mcc_,
				balance_};
		
		return return_val;
	}

	//-----------------------------------
	public String getAsString(String seperator_char)
	{
		
		if (filepath_testdata_ == "")
		{
			filepath_testdata_ = filepath_;
		}
		
		String return_str = "";
		
		return_str += weka_classifier_.classifier_name_ + seperator_char;
		
		for (double cur_val: getAsDoubleArray())
		{
			return_str += Double.toString(cur_val) + seperator_char;
		}
		
		return_str += split_percentage_ + seperator_char;
		return_str += resample_ + seperator_char;
		return_str += Integer.toString(replace_resample_) + seperator_char;
		return_str += bias_ + seperator_char;
		return_str += weka_classifier_.weka_call_ + seperator_char;
		return_str += filepath_ + seperator_char;
		return_str += filepath_testdata_;
		
		return return_str;
	}
	
	//-----------------------------------
	public static void writeCSVHeader(FileWriter csv_writer) throws IOException
	{
		String seperator_string = ";";
		csv_writer.write("Classifier" + seperator_string
				+ "auc" + seperator_string 
				+ "accuracy" + seperator_string 
				+ "recall" + seperator_string 
				+ "precision" + seperator_string 
				+ "type_i" + seperator_string 
				+ "type_ii" + seperator_string 
				+ "f1" + seperator_string 
				+ "fnrate" + seperator_string 
				+ "tprate" + seperator_string 
				+ "tp" + seperator_string 
				+ "tn" + seperator_string 
				+ "fp" + seperator_string 
				+ "fn" + seperator_string
				+ "correct_classified" + seperator_string
				+ "incorrect_classified" + seperator_string
				+ "weighted_recall" + seperator_string
				+ "weighted_precission" + seperator_string
				+ "weighted_auc" + seperator_string
				+ "pf_" + seperator_string
				+ "g_measure_" + seperator_string
				+ "error_measure_" + seperator_string
				+ "mcc_" + seperator_string
				+ "balance_" + seperator_string
				+ "train_test_split" + seperator_string
				+ "resample" + seperator_string
				+ "replace_resample" + seperator_string
				+ "bias_to_uniform" + seperator_string 
				+ "clasifier_options" + seperator_string 
				+ "file_traindata" + seperator_string
				+ "file_testdata"
				);
	}

	//-----------------------------------	
	public void writeCsv(FileWriter csv_writer) throws IOException
	{
		csv_writer.write(getAsString(";"));
	}
	
	//-----------------------------------
	public void printData()
	{
		//System.out.println(eval_class.toSummaryString());
		System.out.println("Classifier: " + weka_classifier_.classifier_name_);
		System.out.println("AUC:        " + auc_);
		System.out.println("Recall:     " + recall_);
		System.out.println("Precision:  " + precision_);
		System.out.println("FNrate:     " + fnrate_);
		System.out.println("TPrate:     " + tprate_);
		System.out.println("confusionMatrix: TN=" + tn_ + " FP="  + fp_ + " FN=" + fn_ + " TP=" + tp_);
	}
	
}