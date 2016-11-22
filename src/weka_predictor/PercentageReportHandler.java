package weka_predictor;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class PercentageReportHandler {
	
	private int items_to_cover_;
	private double percentage_per_item_;
	private double percentage_coverd_;
	private double percentage_last_report_;
	private int items_coverd_since_report_;
	private String item_name_; 
	
	public PercentageReportHandler(int items_to_cover, String item_name)
	{
		items_to_cover_ = items_to_cover;
		percentage_per_item_ = 100.0 / items_to_cover_;
		percentage_coverd_ = 0.0;
		percentage_last_report_ = 0.0;
		items_coverd_since_report_ = 0;
		item_name_ = item_name;
		
	}
	
	//-----------------------------------------------------------------
	public void reportPercentage(int new_items_coverd)
	{
		percentage_coverd_ += (percentage_per_item_ * new_items_coverd);
		items_coverd_since_report_ += new_items_coverd;
		if((percentage_coverd_ - percentage_last_report_) > 5.0)
		{
			percentage_last_report_ = percentage_coverd_;
			DecimalFormat df = new DecimalFormat("###.##");
			df.setRoundingMode(RoundingMode.CEILING);
			System.out.println(" ---> performed " + items_coverd_since_report_ + " " + item_name_ + ", in total " + df.format(percentage_coverd_) + "%");
			items_coverd_since_report_ = 0;
		}
	}

}
